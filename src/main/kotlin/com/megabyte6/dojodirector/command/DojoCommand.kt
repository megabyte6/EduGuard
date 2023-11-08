package com.megabyte6.dojodirector.command

import com.megabyte6.dojodirector.DojoDirector
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.command.TabExecutor

class DojoCommand : TabExecutor {
    companion object {
        val argOptions = mapOf(
            "help" to null,
            "config" to mapOf(
                "kick_message" to null
            ),
        )
    }

    override fun onTabComplete(
        sender: CommandSender, command: Command, label: String, args: Array<out String>?
    ): MutableList<String>? = when (args?.size) {
        0 -> argOptions.keys.toMutableList()
        1 -> argOptions.keys.filter { it.startsWith(args[0]) }.toMutableList()
        2 -> argOptions[args[0]]?.keys?.filter { it.startsWith(args[1]) }?.toMutableList()
        else -> null
    }

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>?): Boolean {
        if (args == null || args.isEmpty()) {
            showHelp(sender)
            return true
        }
        if (args[0] !in argOptions.keys) {
            showHelp(sender)
            return false
        }

        when (args[0].lowercase()) {
            "help" -> showHelp(sender)
            "config" -> {
                if (args.size < 2) {
                    sender.sendMessage(
                        Component.text("You must specify a configuration property", NamedTextColor.RED)
                    )
                    return false
                }
                if (args[1] !in (argOptions["config"]?.keys ?: emptySet())) {
                    sender.sendMessage(
                        Component.text("'${args[1]}' is not a recognized configuration property", NamedTextColor.RED)
                    )
                    return false
                }

                when (args[1]) {
                    "kick_message" -> {
                        if (args.size >= 3) {
                            // The user passed a new message to use, so replace the old message with it.
                            val newMessage = args.slice(2 until args.size).joinToString(" ")
                            DojoDirector.config.set("kick_message", newMessage)
                            sender.sendMessage(
                                Component.text()
                                    .append(Component.text("Set kick message to: "))
                                    .append(Component.text(newMessage, NamedTextColor.AQUA))
                                    .build()
                            )
                        } else {
                            // The user didn't pass a new message, so show the current message.
                            val message = DojoDirector.config.getString("kick_message")
                                ?: DojoDirector.defaultConfig.getString("kick_message")!!
                            sender.sendMessage(
                                Component.text()
                                    .append(Component.text("Kick message: "))
                                    .append(Component.text(message, NamedTextColor.AQUA))
                                    .build()
                            )
                        }
                        return true
                    }
                }
            }
        }

        return true
    }

    private fun showHelp(sender: CommandSender) {
        sender.sendMessage(
            Component.text()
                .append(Component.text("DojoDirector Help", NamedTextColor.GOLD))
                .appendNewline()

                .append(Component.text("/dojo help", NamedTextColor.YELLOW))
                .append(Component.text(" - Show this help message", NamedTextColor.AQUA))
                .appendNewline()

                .append(Component.text("/dojo config <config property>", NamedTextColor.YELLOW))
                .append(Component.text(" - Configure the plugin", NamedTextColor.AQUA))
                .appendNewline()

                .append(Component.text("/dojo config <config property> <new value>", NamedTextColor.YELLOW))
                .append(Component.text(" - Set a configuration property", NamedTextColor.AQUA))
                .appendNewline()

                .build()
        )
    }
}
