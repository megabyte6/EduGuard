package com.megabyte6.dojodirector

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.title.Title
import org.bukkit.Bukkit
import org.bukkit.plugin.Plugin
import org.bukkit.scheduler.BukkitTask
import java.time.DayOfWeek
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.temporal.ChronoUnit
import java.time.temporal.TemporalAdjusters

data class DayOfWeekTime(val day: DayOfWeek, val time: LocalTime)

private val queue = mutableListOf<Pair<DayOfWeekTime, () -> Unit>>()
private fun MutableList<Pair<DayOfWeekTime, () -> Unit>>.sort() = sortBy { timeUntilEvent(it.first) }

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
    val (event, callback) = queue.first()
    currentQueuedTask = Bukkit.getScheduler().runTaskLater(plugin, Runnable {
        // Reschedule the event so the server can continue running for more than a week.
        with(queue) { add(removeFirst()) }
        callback()
        queueNextEvent(plugin)
    }, timeUntilEvent(event).inWholeTicks)
}

private fun nextEventDateTime(event: DayOfWeekTime): LocalDateTime {
    val now = LocalDateTime.now()
    // Calculate the next occurrence of the event.
    var nextEventDate = now.with(TemporalAdjusters.nextOrSame(event.day))
    // Check if the current day is the same as the event day and the event time has already passed.
    if (now.dayOfWeek == event.day && now.toLocalTime().isAfter(event.time)) {
        nextEventDate = nextEventDate.plusWeeks(1)
    }

    return nextEventDate.with(event.time)
}

private fun timeUntilEvent(event: DayOfWeekTime) =
    ChronoUnit.SECONDS.between(LocalDateTime.now(), nextEventDateTime(event)).ticks

fun queueKickTimes() {
    generateKickTimes().forEach { dayOfWeekTime ->
        queue.add(dayOfWeekTime to {
            Bukkit.getOnlinePlayers().forEach { player ->
                player.kick(Component.text(DojoDirector.settings.autoKick.message))
            }
        })
    }
}

fun queueWarningTimes() {
    generateKickTimes().forEach { dayOfWeekTime ->
        queue.add(DayOfWeekTime(dayOfWeekTime.day, dayOfWeekTime.time.minusSeconds(60)) to {
            Bukkit.getOnlinePlayers().forEach { player ->
                player.sendMessage(Component.text("60 seconds until class is over!", NamedTextColor.YELLOW))
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
            val event = DayOfWeekTime(dayOfWeekTime.day, dayOfWeekTime.time.minusSeconds(secondsLeft))
            queue.add(event to {
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
        queue.add(
            DayOfWeekTime(
                it.day,
                it.time.plusSeconds(DojoDirector.settings.autoKick.disableWhitelistAfter.inWholeSeconds)
            ) to {
                Bukkit.setWhitelist(false)
            })
    }
}

private fun generateClassEndTimes(): List<DayOfWeekTime> {
    val endOfClasses = mutableListOf<DayOfWeekTime>()

    for (day in DayOfWeek.entries) {
        // No class on Sundays.
        if (day == DayOfWeek.SUNDAY) continue

        for (block in 0..3) {
            // Each class lasts for an hour. The first class ends at 4:30pm aka 16:30.
            var time = LocalTime.of(16 + block, 30, 0)
            if (day == DayOfWeek.SATURDAY) {
                // Each class on Saturday last for an hour. The first class ends at 11:00am.
                time = LocalTime.of(11 + block, 0, 0)
            }
            endOfClasses.add(DayOfWeekTime(day, time))
        }
    }

    return endOfClasses
}

private fun generateKickTimes() = generateClassEndTimes().map {
    DayOfWeekTime(it.day, it.time.minusSeconds(DojoDirector.settings.autoKick.beforeEndOfClass.inWholeSeconds))
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
    DayOfWeekTime(it.day, it.time.minusMinutes(DojoDirector.settings.autoResetDay.beforeEndOfClass.inWholeMinutes))
}
