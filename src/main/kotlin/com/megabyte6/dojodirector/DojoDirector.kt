package com.megabyte6.dojodirector

import org.bukkit.configuration.InvalidConfigurationException
import org.bukkit.configuration.serialization.ConfigurationSerialization
import org.bukkit.plugin.java.JavaPlugin
import java.io.File
import java.io.FileNotFoundException
import java.io.IOException


class DojoDirector : JavaPlugin() {

    companion object {
        val settings = DojoSettings()
    }

    override fun onEnable() {
        ConfigurationSerialization.registerClass(DojoSettings::class.java)
        ConfigurationSerialization.registerClass(DojoSettings.AutoKick::class.java)
        ConfigurationSerialization.registerClass(DojoSettings.AutoResetDay::class.java)
        try {
            config.load(File(dataFolder, "config.yml"))
            settings.load(config)
        } catch (e: FileNotFoundException) {
            logger.warning("Could not find config file. Creating a new one.")
        } catch (e: IOException) {
            logger.warning("Could not read config file.")
            e.printStackTrace()
        } catch (e: InvalidConfigurationException) {
            logger.warning("Could not parse config file. Invalid syntax.")
            e.printStackTrace()
        }

        if (settings.autoKick.enabled) {
            queueKickTimes()
            if (settings.autoKick.showWarning) {
                queueWarningTimes()
            }
            if (settings.autoKick.enableWhiteListOnKick) {
                queueWhitelistTimes()
            }
        }

        if (settings.autoResetDay.enabled) {
            queueResetDayTimes()
        }

        startScheduler(plugin = this)
    }

    override fun onDisable() {
        settings.writeToConfig(config)
        try {
            config.save(File(dataFolder, "config.yml"))
        } catch (e: IOException) {
            logger.severe("Could not save config.yml")
            e.printStackTrace()
        }
    }
}
