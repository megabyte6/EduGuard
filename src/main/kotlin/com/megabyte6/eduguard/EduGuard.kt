package com.megabyte6.eduguard

import com.megabyte6.eduguard.settings.v3_0.SettingsManager
import org.bukkit.configuration.InvalidConfigurationException
import org.bukkit.plugin.java.JavaPlugin
import org.yaml.snakeyaml.error.YAMLException
import java.io.File
import java.io.FileNotFoundException
import java.io.IOException


class EduGuard : JavaPlugin() {

    companion object {
        val version = Version(3, 0, 1)
        val settings
            get() = SettingsManager.settings

        private var configVersion: Version? = null
    }

    override fun onEnable() {
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
            SettingsManager.load(config, configVersion ?: version)
        } catch (e: FileNotFoundException) {
            logger.warning("Could not find config file. Creating a new one.")
        } catch (e: IOException) {
            logger.warning("Could not read config file.")
            e.printStackTrace()
        } catch (e: InvalidConfigurationException) {
            logger.warning("Could not parse config file. Invalid syntax.")
            e.printStackTrace()
        } catch (e: YAMLException) {
            logger.warning("Could not deserialize YAML object.")
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

        if (settings.resetDay.enabled) {
            queueResetDayTimes()
        }

        startScheduler(plugin = this)

        server.pluginManager.registerEvents(PlayerChatListener(), this)
        server.pluginManager.registerEvents(PlayerJoinListener(), this)
    }

    override fun onDisable() {
        SettingsManager.writeToConfig(config)
        try {
            config.save(File(dataFolder, "config.yml"))
        } catch (e: IOException) {
            logger.severe("Could not save config.yml")
            e.printStackTrace()
        }

        if (configVersion == null || (configVersion as Version) < version) {
            val versionFile = File(dataFolder, "version")
            if (!versionFile.exists()) {
                versionFile.createNewFile()
            }
            versionFile.writeText("${version.major}.${version.minor}.${version.patch}")
        }
    }
}
