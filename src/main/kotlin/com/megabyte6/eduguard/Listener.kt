package com.megabyte6.eduguard

import com.megabyte6.eduguard.EduGuard.Companion.settings
import io.papermc.paper.ban.BanListType
import io.papermc.paper.event.player.AsyncChatEvent
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Bukkit
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import java.time.Duration

class PlayerChatListener : Listener {
    @EventHandler(ignoreCancelled = true)
    fun onChat(event: AsyncChatEvent) {
        if (settings.profanityFilter.filterChat) {
            if (settings.profanityFilter.prohibitedWords.any {
                    event.message().toString().contains(it, ignoreCase = true)
                }) {
                event.isCancelled = true
                event.player.sendMessage(
                    Component.text(
                        "Your message contains inappropriate language. Please consider a better choice of words.",
                        NamedTextColor.RED
                    )
                )
            }
        }
    }
}

class PlayerJoinListener : Listener {
    @EventHandler
    fun onPlayerJoin(event: PlayerJoinEvent) {
        if (settings.profanityFilter.filterUsernames) {
            val player = event.player
            if (settings.profanityFilter.prohibitedWords.any { player.name.contains(it, ignoreCase = true) }) {
                Bukkit.getBanList(BanListType.PROFILE)
                    .addBan(player.playerProfile, "Inappropriate username", null as Duration?, "Moderation Plugin")
                player.kick(Component.text("You have been banned for using an inappropriate username."))
            }
        }
    }
}
