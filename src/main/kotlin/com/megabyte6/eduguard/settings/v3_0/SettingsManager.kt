package com.megabyte6.eduguard.settings.v3_0

import com.megabyte6.eduguard.Version
import com.megabyte6.eduguard.isAfterMinorVersion
import com.megabyte6.eduguard.isBeforeMinorVersion
import com.megabyte6.eduguard.settings.v2_1.SettingsManager
import org.bukkit.configuration.file.FileConfiguration
import org.bukkit.configuration.serialization.ConfigurationSerialization

object SettingsManager {
    var settings = Settings()

    private val version = Version(3, 0)

    fun registerClasses() {
        SettingsManager.registerClasses()
        ConfigurationSerialization.registerClass(Settings::class.java)
        ConfigurationSerialization.registerClass(Settings.AutoKick::class.java)
        ConfigurationSerialization.registerClass(Settings.ResetDay::class.java)
        ConfigurationSerialization.registerClass(Settings.ProfanityFilter::class.java)
    }

    fun load(config: FileConfiguration, configVersion: Version) {
        if (configVersion isAfterMinorVersion version) {
            throw IllegalArgumentException("Invalid version. Latest supported version is $version. If you are sure this is the correct version, please update the plugin.")
        }

        if (configVersion isBeforeMinorVersion version) {
            // Load the previous version's settings.
            SettingsManager.load(config, configVersion)
            settings = convert(SettingsManager.settings)
            return
        }

        config.getSerializable("auto-kick", Settings.AutoKick::class.java)?.let { settings.autoKick = it }
        config.getSerializable("reset-day", Settings.ResetDay::class.java)?.let { settings.resetDay = it }
        config.getSerializable("end-of-class-times", Settings.EndOfClassTimes::class.java)
            ?.let { settings.endOfClassTimes = it }
        config.getSerializable("profanity-filter", Settings.ProfanityFilter::class.java)
            ?.let { settings.profanityFilter = it }
    }

    private fun convert(oldSettings: com.megabyte6.eduguard.settings.v2_1.Settings) = Settings().apply {
        autoKick.enabled = oldSettings.autoKick.enabled
        autoKick.message = oldSettings.autoKick.message
        autoKick.beforeEndOfClass = oldSettings.autoKick.beforeEndOfClass
        autoKick.showWarning = oldSettings.autoKick.showWarning
        autoKick.enableWhiteListOnKick = oldSettings.autoKick.enableWhiteListOnKick
        autoKick.disableWhitelistAfter = oldSettings.autoKick.disableWhitelistAfter

        resetDay.enabled = oldSettings.autoResetDay.enabled
        resetDay.beforeEndOfClass = oldSettings.autoResetDay.beforeEndOfClass
        resetDay.minecraftWorldName = oldSettings.autoResetDay.worldName
        resetDay.minecraftTime = oldSettings.autoResetDay.time
        resetDay.useAbsoluteTime = oldSettings.autoResetDay.useAbsoluteTime

        endOfClassTimes.monday = oldSettings.endOfClassTimes.monday
        endOfClassTimes.tuesday = oldSettings.endOfClassTimes.tuesday
        endOfClassTimes.wednesday = oldSettings.endOfClassTimes.wednesday
        endOfClassTimes.thursday = oldSettings.endOfClassTimes.thursday
        endOfClassTimes.friday = oldSettings.endOfClassTimes.friday
        endOfClassTimes.saturday = oldSettings.endOfClassTimes.saturday
        endOfClassTimes.sunday = oldSettings.endOfClassTimes.sunday
    }

    fun writeToConfig(config: FileConfiguration) {
        config.set("auto-kick", settings.autoKick)
        config.set("reset-day", settings.resetDay)
        config.set("end-of-class-times", settings.endOfClassTimes)
        config.set("profanity-filter", settings.profanityFilter)
    }
}