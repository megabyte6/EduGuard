package com.megabyte6.eduguard.settings.v4_0

import com.megabyte6.eduguard.Version
import com.megabyte6.eduguard.isAfterMinorVersion
import com.megabyte6.eduguard.isBeforeMinorVersion
import org.bukkit.configuration.file.FileConfiguration

object SettingsManager {
    var settings = Settings()

    private val version = Version(4, 0)

    fun load(config: FileConfiguration, configVersion: Version) {
        if (configVersion isAfterMinorVersion version) {
            throw IllegalArgumentException("Invalid version. Latest supported version is $version. If you are sure this is the correct version, please update the plugin.")
        }

        if (configVersion isBeforeMinorVersion version) {
            // Load the previous version's settings.
            // In this case, many breaking changes were made in 4.0 and
            // therefore old SettingsManager cannot be converted.
            throw IllegalArgumentException("Many breaking changes were made in version 4.0. Config versions before 4.0 are not supported and cannot be converted. Please manually copy configuration settings.")
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

    fun writeToConfig(config: FileConfiguration) {
        config.createSection("auto-kick", settings.autoKick.serialize())
        config.createSection("reset-day", settings.resetDay.serialize())
        config.createSection("end-of-class-times", settings.endOfClassTimes.serialize())
        config.createSection("profanity-filter", settings.profanityFilter.serialize())
    }
}
