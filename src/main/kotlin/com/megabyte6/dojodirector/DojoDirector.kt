package com.megabyte6.dojodirector

import com.megabyte6.dojodirector.settings.v2_1.SettingsManager
import org.bukkit.configuration.InvalidConfigurationException
import org.bukkit.plugin.java.JavaPlugin
import java.io.File
import java.io.FileNotFoundException
import java.io.IOException


class DojoDirector : JavaPlugin() {

    companion object {
        val version = Version(2, 1)
        val settings
            get() = SettingsManager.settings
    }

    override fun onEnable() {
        var configVersion = version
        try {
            File(dataFolder, "version").readLines().firstOrNull()?.let {
                val versionParts = it.split(".").map { part -> part.toInt() }
                if (versionParts.size == 3) {
                    configVersion = Version(versionParts[0], versionParts[1], versionParts[2])
                }
            }
        } catch (e: FileNotFoundException) {
            logger.warning("Could not find version file. Creating a new one.")
        } catch (e: IOException) {
            logger.warning("Could not read version file.")
            e.printStackTrace()
        } catch (e: NumberFormatException) {
            logger.warning("Could not parse version file.")
            e.printStackTrace()
        }

        SettingsManager.registerClasses()
        try {
            config.load(File(dataFolder, "config.yml"))
            SettingsManager.load(config, configVersion)
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
        SettingsManager.writeToConfig(config)
        try {
            config.save(File(dataFolder, "config.yml"))
        } catch (e: IOException) {
            logger.severe("Could not save config.yml")
            e.printStackTrace()
        }

        val versionFile = File(dataFolder, "version")
        if (!versionFile.exists()) {
            versionFile.createNewFile()
        }
        versionFile.writeText("${version.major}.${version.minor}.${version.patch}")

    }
}
