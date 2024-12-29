package com.megabyte6.eduguard.settings.v4_1

import com.megabyte6.eduguard.EduGuard.Companion.version
import com.megabyte6.eduguard.isAfterMinorVersion
import com.megabyte6.eduguard.isBeforeMinorVersion
import com.megabyte6.eduguard.settings.v4_0.SettingsManager
import com.megabyte6.eduguard.toVersionOrNull
import org.bukkit.configuration.file.FileConfiguration

object SettingsManager {
    var settings = Settings()

    fun load(config: FileConfiguration) {
        val configVersion = config.getString("version")?.toVersionOrNull() ?: version

        if (configVersion isAfterMinorVersion settings.version) {
            throw IllegalArgumentException("Invalid version. Latest supported version is ${settings.version}. If you are sure this is the correct version, please update the plugin.")
        }

        if (configVersion isBeforeMinorVersion settings.version) {
            // Load the previous version's settings.
            SettingsManager.load(config, configVersion)
            settings = convert(SettingsManager.settings)
            return
        }

        config.getConfigurationSection("auto-kick")
            ?.let { settings.autoKick = Settings.AutoKick.deserialize(it.getValues(false)) }
        config.getConfigurationSection("reset-day")
            ?.let { settings.resetDay = Settings.ResetDay.deserialize(it.getValues(false)) }
        config.getConfigurationSection("end-of-class-times")
            ?.let { settings.endOfClassTimes = Settings.EndOfClassTimes.deserialize(it.getValues(false)) }
        config.getConfigurationSection("profanity-filter")
            ?.let { settings.profanityFilter = Settings.ProfanityFilter.deserialize(it.getValues(false)) }
    }

    private fun convert(oldSettings: com.megabyte6.eduguard.settings.v4_0.Settings) = Settings().apply {
        autoKick.enabled = oldSettings.autoKick.enabled
        autoKick.message = oldSettings.autoKick.message
        autoKick.beforeEndOfClass = oldSettings.autoKick.beforeEndOfClass
        autoKick.showWarning = oldSettings.autoKick.showWarning
        autoKick.enableWhiteListOnKick = oldSettings.autoKick.enableWhiteListOnKick
        autoKick.disableWhitelistAfter = oldSettings.autoKick.disableWhitelistAfter

        resetDay.enabled = oldSettings.resetDay.enabled
        resetDay.beforeEndOfClass = oldSettings.resetDay.beforeEndOfClass
        resetDay.minecraftWorldName = oldSettings.resetDay.minecraftWorldName
        resetDay.minecraftTime = oldSettings.resetDay.minecraftTime
        resetDay.useAbsoluteTime = oldSettings.resetDay.useAbsoluteTime

        endOfClassTimes.monday = oldSettings.endOfClassTimes.monday
        endOfClassTimes.tuesday = oldSettings.endOfClassTimes.tuesday
        endOfClassTimes.wednesday = oldSettings.endOfClassTimes.wednesday
        endOfClassTimes.thursday = oldSettings.endOfClassTimes.thursday
        endOfClassTimes.friday = oldSettings.endOfClassTimes.friday
        endOfClassTimes.saturday = oldSettings.endOfClassTimes.saturday
        endOfClassTimes.sunday = oldSettings.endOfClassTimes.sunday

        profanityFilter.filterChat = oldSettings.profanityFilter.filterChat
        profanityFilter.filterUsernames = oldSettings.profanityFilter.filterUsernames
        profanityFilter.prohibitedWords = oldSettings.profanityFilter.prohibitedWords
    }

    fun writeToConfig(config: FileConfiguration) {
        config.set("version", version.toString())
        config.createSection("auto-kick", settings.autoKick.serialize())
        config.createSection("reset-day", settings.resetDay.serialize())
        config.createSection("end-of-class-times", settings.endOfClassTimes.serialize())
        config.createSection("profanity-filter", settings.profanityFilter.serialize())
    }
}
