package com.megabyte6.dojodirector.settings

import com.megabyte6.dojodirector.Version
import org.bukkit.configuration.file.FileConfiguration

interface DojoSettingsManager {
    val settings: DojoSettings
    fun registerClasses()
    fun load(config: FileConfiguration, configVersion: Version)
    fun writeToConfig(config: FileConfiguration)
}
