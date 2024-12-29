package com.megabyte6.eduguard

import com.megabyte6.eduguard.settings.v4_1.SettingsManager
import org.bukkit.configuration.InvalidConfigurationException
import org.bukkit.plugin.java.JavaPlugin
import org.yaml.snakeyaml.error.YAMLException
import java.io.File
import java.io.FileNotFoundException
import java.io.IOException

class EduGuard : JavaPlugin() {

    companion object {
        val version = Version(4, 1, 0)
        val settings
            get() = SettingsManager.settings
    }

    override fun onEnable() {
        try {
            config.load(File(dataFolder, "config.yml"))
            SettingsManager.load(config)
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
    }
}
