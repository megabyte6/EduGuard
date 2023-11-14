package com.megabyte6.dojodirector

import kotlin.time.Duration
import kotlin.time.DurationUnit
import kotlin.time.toDuration

inline val Int.ticks get() = (this * 50).toDuration(DurationUnit.MILLISECONDS)
inline val Long.ticks get() = (this * 50).toDuration(DurationUnit.MILLISECONDS)

inline val Duration.inWholeTicks get() = inWholeMilliseconds / 50
