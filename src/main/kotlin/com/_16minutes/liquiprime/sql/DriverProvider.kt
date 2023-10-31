package com._16minutes.liquiprime.sql

import java.sql.Driver
import java.sql.DriverManager
import java.util.*

interface DriverProvider {
    fun getRegisteredDrivers(): Enumeration<Driver>

    fun getDriverFor(jdbcUrl: String?): Driver
}
