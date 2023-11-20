package com.megabyte6.dojodirector.settings.v2_0

import com.megabyte6.dojodirector.inWholeTicks
import com.megabyte6.dojodirector.settings.DojoSettings
import com.megabyte6.dojodirector.ticks
import org.bukkit.configuration.serialization.ConfigurationSerializable
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds


data class Settings(
    var autoKick: AutoKick = AutoKick(),
    var autoResetDay: AutoResetDay = AutoResetDay(),
) : DojoSettings, ConfigurationSerializable {
    companion object {
        @JvmStatic
        fun deserialize(args: Map<String, Any>) = Settings()
    }

    override fun serialize() = mutableMapOf<String, Any>()

    data class AutoKick(
        var enabled: Boolean = true,
        var message: String = "Server is now closed. Time to exit the Dojo!",
        var beforeEndOfClass: Duration = 30.seconds,
        var showWarning: Boolean = true,
        var enableWhiteListOnKick: Boolean = true,
        var disableWhitelistAfter: Duration = 330.seconds,
    ) : ConfigurationSerializable {
        companion object {
            @JvmStatic
            fun deserialize(args: Map<String, Any>) = AutoKick().apply {
                args["enabled"]?.let { enabled = it as Boolean }
                args["message"]?.let { message = it as String }
                args["before-end-of-class"]?.let { beforeEndOfClass = (it as Int).seconds }
                args["show-warning"]?.let { showWarning = it as Boolean }
                args["enable-whitelist-on-kick"]?.let { enableWhiteListOnKick = it as Boolean }
                args["disable-whitelist-after"]?.let { disableWhitelistAfter = (it as Int).seconds }
            }
        }

        override fun serialize() = mutableMapOf(
            "enabled" to enabled,
            "message" to message,
            "before-end-of-class" to beforeEndOfClass.inWholeSeconds,
            "show-warning" to showWarning,
            "enable-whitelist-on-kick" to enableWhiteListOnKick,
            "disable-whitelist-after" to disableWhitelistAfter.inWholeSeconds,
        )
    }

    data class AutoResetDay(
        var enabled: Boolean = true,
        var time: Duration = 6000.ticks,
        var useAbsoluteTime: Boolean = false,
        var beforeEndOfClass: Duration = 10.minutes,
        var worldName: String = "world",
    ) : ConfigurationSerializable {
        companion object {
            @JvmStatic
            fun deserialize(args: Map<String, Any>) = AutoResetDay().apply {
                args["enabled"]?.let { enabled = it as Boolean }
                args["time"]?.let { time = (it as Int).ticks }
                args["use-absolute-time"]?.let { useAbsoluteTime = it as Boolean }
                args["before-end-of-class"]?.let { beforeEndOfClass = (it as Int).minutes }
                args["world-name"]?.let { worldName = it as String }
            }
        }

        override fun serialize() = mutableMapOf(
            "enabled" to enabled,
            "time" to time.inWholeTicks,
            "use-absolute-time" to useAbsoluteTime,
            "before-end-of-class" to beforeEndOfClass.inWholeMinutes,
            "world-name" to worldName,
        )
    }
}
