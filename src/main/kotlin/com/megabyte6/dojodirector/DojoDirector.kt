package com.megabyte6.dojodirector

import org.bukkit.Bukkit
import org.bukkit.configuration.file.FileConfiguration
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.plugin.java.JavaPlugin

class DojoDirector : JavaPlugin() {

    companion object {
        val defaultConfig = YamlConfiguration()
        val config: FileConfiguration by lazy {
            Bukkit.getPluginManager().getPlugin("DojoDirector")?.config ?: defaultConfig
        }
    }

    override fun onEnable() {
        saveDefaultConfig()
        defaultConfig.load(javaClass.getResourceAsStream("/config.yml")!!.bufferedReader())

        queueKickTimes()
        queueWarningTimes()
        queueWhitelistTimes()
        startScheduler(plugin = this)
    }

    override fun onDisable() {
        saveConfig()
    }
}
