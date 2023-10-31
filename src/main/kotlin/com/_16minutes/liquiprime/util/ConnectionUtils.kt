package com._16minutes.liquiprime.util

import java.security.AccessController
import java.security.PrivilegedAction
import java.sql.Connection
import java.sql.DriverManager
import java.util.*

fun createConnection(jdbcUrl: String?, driverClassName: String?, driverProperties: Properties?): Connection {
    /* Invoke a driver retrieval method in order to trigger the driver loading mechanism (some
     * implementations do this on class load as stated in the documentation, but others do so
     * at the beginning of their driver retrieval methods, so we invoke such a method to cover
     * both cases). This is necessary because there appears to be a bug which causes the first
     * invocation of any such method to not reflect the state of the registered driver collection
     * after the driver loading mechanism has run (in other words, the methods operate as if there
     * are no registered drivers). Subsequent invocations succeed, so this first invocation is carried
     * out to "prime" the driver collection before subsequent retrieval method invocations operate on it.
     */
    DriverManager.getDrivers()

    val driver = if (driverClassName != null) {
        DriverManager.getDrivers().asSequence().first { it.javaClass.name == driverClassName }
    } else {
        DriverManager.getDriver(jdbcUrl)
    }

    return driver.connect(jdbcUrl, driverProperties)
}
