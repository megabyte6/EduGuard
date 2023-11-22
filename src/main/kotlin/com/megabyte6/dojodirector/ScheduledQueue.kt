package com.megabyte6.dojodirector

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.title.Title
import org.bukkit.Bukkit
import org.bukkit.plugin.Plugin
import org.bukkit.scheduler.BukkitTask
import java.time.DayOfWeek
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import java.time.temporal.TemporalAdjusters
import kotlin.time.Duration.Companion.milliseconds


private val queue = mutableListOf<Pair<LocalDateTime, () -> Unit>>()
private fun MutableList<Pair<LocalDateTime, () -> Unit>>.sort() = sortBy { it.first }

private lateinit var currentQueuedTask: BukkitTask

fun startScheduler(plugin: Plugin) {
    queue.sort()

    // Remove the current task if it exists before queueing the next one.
    if (::currentQueuedTask.isInitialized) {
        Bukkit.getScheduler().cancelTask(currentQueuedTask.taskId)
    }
    queueNextEvent(plugin)
}

private fun queueNextEvent(plugin: Plugin) {
    if (queue.isEmpty()) return

    val (event, callback) = queue.first()
    val time = if (event < LocalDateTime.now()) 0.ticks else timeUntilEvent(event)
    currentQueuedTask = Bukkit.getScheduler().runTaskLater(plugin, Runnable {
        // Reschedule the event so the server can continue running for more than a week.
        with(queue) { add(removeFirst()) }
        callback()
        queueNextEvent(plugin)
    }, time.inWholeTicks)
}

private fun nextEventDateTime(event: LocalDateTime): LocalDateTime =
    if (event < LocalDateTime.now()) event.plusWeeks(1) else event

private fun timeUntilEvent(event: LocalDateTime) =
    ChronoUnit.MILLIS.between(LocalDateTime.now(), nextEventDateTime(event)).milliseconds

fun queueKickTimes() {
    generateKickTimes().forEach { dateTime ->
        queue.add(dateTime to {
            Bukkit.getOnlinePlayers().forEach { player ->
                player.kick(Component.text(DojoDirector.settings.autoKick.message))
            }
        })
    }
}

fun queueWarningTimes() {
    generateKickTimes().forEach { dateTime ->
        queue.add(dateTime.minusMinutes(1) to {
            Bukkit.getOnlinePlayers().forEach { player ->
                player.sendMessage(Component.text("60 seconds until the dojo is closed!!", NamedTextColor.YELLOW))
            }
        })

        for ((secondsLeft, message) in mapOf(
            10L to Component.text("10 seconds left!", NamedTextColor.YELLOW),
            5L to Component.text("5", NamedTextColor.YELLOW),
            4L to Component.text("4", NamedTextColor.YELLOW),
            3L to Component.text("3", NamedTextColor.RED),
            2L to Component.text("2", NamedTextColor.RED),
            1L to Component.text("1", NamedTextColor.RED),
        )) {
            queue.add(dateTime.minusSeconds(secondsLeft) to {
                Bukkit.getOnlinePlayers().forEach { player ->
                    player.showTitle(Title.title(message, Component.empty()))
                }
            })
        }
    }
}

fun queueWhitelistTimes() {
    generateKickTimes().forEach {
        queue.add(it to {
            Bukkit.setWhitelist(true)
        })
        queue.add(it.plusSeconds(DojoDirector.settings.autoKick.disableWhitelistAfter.inWholeSeconds) to {
            Bukkit.setWhitelist(false)
        })
    }
}

private fun generateClassEndTimes() = DayOfWeek.entries.flatMap { day ->
    DojoDirector.settings.endOfClassTimes.getTimes(day).map { time ->
        LocalDateTime.now().with(
            if (time < LocalDateTime.now().toLocalTime()) {
                // If the class has already ended today, schedule it for next week.
                TemporalAdjusters.next(day)
            } else {
                TemporalAdjusters.nextOrSame(day)
            }
        ).with(time)
    }
}

private fun generateKickTimes() = generateClassEndTimes().map {
    it.minusSeconds(DojoDirector.settings.autoKick.beforeEndOfClass.inWholeSeconds)
}

fun queueResetDayTimes() {
    generateResetDayTimes().forEach {
        queue.add(it to {
            val world = Bukkit.getServer().getWorld(DojoDirector.settings.autoResetDay.worldName)
            val newTime = DojoDirector.settings.autoResetDay.time.inWholeTicks
            if (DojoDirector.settings.autoResetDay.useAbsoluteTime) {
                world?.fullTime = newTime
            } else {
                world?.time = newTime
            }
        })
    }
}

private fun generateResetDayTimes() = generateClassEndTimes().map {
    it.minusMinutes(DojoDirector.settings.autoResetDay.beforeEndOfClass.inWholeMinutes)
}
