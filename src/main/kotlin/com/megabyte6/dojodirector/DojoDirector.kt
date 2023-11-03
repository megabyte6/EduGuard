package com.megabyte6.dojodirector

import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.plugin.java.JavaPlugin

class DojoDirector : JavaPlugin() {

    companion object {
        val defaultConfig = YamlConfiguration()
    }

    override fun onEnable() {
        saveDefaultConfig()
        defaultConfig.load(javaClass.getResourceAsStream("/config.yml")!!.bufferedReader())
    }

    override fun onDisable() {
        saveConfig()
    }
}
