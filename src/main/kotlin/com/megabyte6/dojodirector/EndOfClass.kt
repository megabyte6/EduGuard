package com.megabyte6.dojodirector

import net.kyori.adventure.text.Component
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
    }, ticksUntilEvent(event))
}

fun queueKickTimes() {
    generateKickTimes().forEach { dayOfWeekTime ->
        queue.add(dayOfWeekTime to {
            val message =
                DojoDirector.config.getString("kick_message") ?: DojoDirector.defaultConfig.getString("kick_message")!!
            Bukkit.getOnlinePlayers().forEach { player -> player.kick(Component.text(message)) }
        })
    }
}

fun queueWarningTimes() {
    generateKickTimes().forEach { dayOfWeekTime ->
        for (timeLeft in mapOf(
            60L to "60 seconds left!",
            10L to "10 seconds left!",
            5L to "5",
            4L to "4",
            3L to "3",
            2L to "2",
            1L to "1",
        )) {
            val (secondsLeft, message) = timeLeft
            val event = DayOfWeekTime(dayOfWeekTime.day, dayOfWeekTime.time.minusSeconds(secondsLeft))
            queue.add(event to {
                Bukkit.getOnlinePlayers().forEach { player ->
                    player.showTitle(Title.title(Component.text(message), Component.empty()))
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
    }
    generateClassEndTimes().forEach {
        queue.add(DayOfWeekTime(it.day, it.time.plusMinutes(5)) to {
            Bukkit.setWhitelist(false)
        })
    }
}

private fun MutableList<Pair<DayOfWeekTime, () -> Unit>>.sort() = sortBy { ticksUntilEvent(it.first) }

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

private fun ticksUntilEvent(event: DayOfWeekTime) =
    ChronoUnit.SECONDS.between(LocalDateTime.now(), nextEventDateTime(event)) * 20

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
    DayOfWeekTime(it.day, it.time.minusSeconds(30))
}
