package com.megabyte6.dojodirector.settings.v2_1

import com.megabyte6.dojodirector.Version
import com.megabyte6.dojodirector.isAfterMinorVersion
import com.megabyte6.dojodirector.isBeforeMinorVersion
import org.bukkit.configuration.file.FileConfiguration
import org.bukkit.configuration.serialization.ConfigurationSerialization

object SettingsManager {
    var settings = Settings()

    private val version = Version(2, 1)

    fun registerClasses() {
        com.megabyte6.dojodirector.settings.v2_0.SettingsManager.registerClasses()
        ConfigurationSerialization.registerClass(Settings::class.java)
        ConfigurationSerialization.registerClass(Settings.AutoKick::class.java)
        ConfigurationSerialization.registerClass(Settings.AutoResetDay::class.java)
    }

    fun load(config: FileConfiguration, configVersion: Version) {
        if (configVersion isAfterMinorVersion version) {
            throw IllegalArgumentException("Invalid version. Latest is $version")
        }

        if (configVersion isBeforeMinorVersion version) {
            // Load the previous version's settings.
            com.megabyte6.dojodirector.settings.v2_0.SettingsManager.load(config, configVersion)
            settings = convert(com.megabyte6.dojodirector.settings.v2_0.SettingsManager.settings)
        }

        config.getSerializable("auto-kick", Settings.AutoKick::class.java)?.let { settings.autoKick = it }
        config.getSerializable("auto-reset-day", Settings.AutoResetDay::class.java)?.let { settings.autoResetDay = it }
        config.getSerializable("end-of-class-times", Settings.EndOfClassTimes::class.java)
            ?.let { settings.endOfClassTimes = it }
    }

    private fun convert(oldSettings: com.megabyte6.dojodirector.settings.v2_0.Settings) = Settings().apply {
        autoKick.enabled = oldSettings.autoKick.enabled
        autoKick.message = oldSettings.autoKick.message
        autoKick.beforeEndOfClass = oldSettings.autoKick.beforeEndOfClass
        autoKick.showWarning = oldSettings.autoKick.showWarning
        autoKick.enableWhiteListOnKick = oldSettings.autoKick.enableWhiteListOnKick
        autoKick.disableWhitelistAfter = oldSettings.autoKick.disableWhitelistAfter

        autoResetDay.enabled = oldSettings.autoResetDay.enabled
        autoResetDay.time = oldSettings.autoResetDay.time
        autoResetDay.useAbsoluteTime = oldSettings.autoResetDay.useAbsoluteTime
        autoResetDay.beforeEndOfClass = oldSettings.autoResetDay.beforeEndOfClass
        autoResetDay.worldName = oldSettings.autoResetDay.worldName
    }

    fun writeToConfig(config: FileConfiguration) {
        config.set("auto-kick", settings.autoKick)
        config.set("auto-reset-day", settings.autoResetDay)
        config.set("end-of-class-times", settings.endOfClassTimes)
    }
}