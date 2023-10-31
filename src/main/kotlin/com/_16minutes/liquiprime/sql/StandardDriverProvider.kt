package com._16minutes.liquiprime.sql

import java.sql.Driver
import java.sql.DriverManager
import java.util.*

class StandardDriverProvider: DriverProvider {
    override fun getRegisteredDrivers(): Enumeration<Driver> {
        return DriverManager.getDrivers()
    }

    override fun getDriverFor(jdbcUrl: String?): Driver{
        return DriverManager.getDriver(jdbcUrl)
    }
}
