package com.megabyte6.dojodirector

data class Version(val major: Int = 0, val minor: Int = 0, val patch: Int = 0) : Comparable<Version> {
    override fun compareTo(other: Version): Int {
        return when {
            major > other.major -> 1
            major < other.major -> -1
            minor > other.minor -> 1
            minor < other.minor -> -1
            patch > other.patch -> 1
            patch < other.patch -> -1
            else -> 0
        }
    }

    override fun toString() = "$major.$minor.$patch"
}
