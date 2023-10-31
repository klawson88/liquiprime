package com._16minutes.liquiprime.sql

import java.sql.Connection
import java.util.*

class ConnectionFactory(private val driverProvider: DriverProvider = StandardDriverProvider()) {
    fun createConnectionFor(jdbcUrl: String?, driverClassName: String?, driverProperties: Properties?): Connection {
        val driver = if (driverClassName != null) {
            driverProvider.getRegisteredDrivers().asSequence().first { it.javaClass.name == driverClassName }
        } else {
            driverProvider.getDriverFor(jdbcUrl)
        }

        return driver.connect(jdbcUrl, driverProperties)
    }
}
