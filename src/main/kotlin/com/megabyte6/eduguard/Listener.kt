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

fun containsProhibitedWord(str: String) = settings.profanityFilter.prohibitedWords.any {
    str.contains(it, ignoreCase = true)
}

class PlayerChatListener : Listener {
    @EventHandler(ignoreCancelled = true)
    fun onChat(event: AsyncChatEvent) {
        if (settings.profanityFilter.filterChat && containsProhibitedWord(event.message().toString())) {
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

class PlayerJoinListener : Listener {
    @EventHandler
    fun onPlayerJoin(event: PlayerJoinEvent) {
        if (settings.profanityFilter.filterUsernames && containsProhibitedWord(event.player.name)) {
            Bukkit.getBanList(BanListType.PROFILE)
                .addBan(event.player.playerProfile, "Inappropriate username", null as Duration?, "Moderation Plugin")
            event.player.kick(Component.text("You have been banned for using an inappropriate username."))
        }
    }
}
