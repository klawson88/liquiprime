package com._16minutes.liquiprime.sql.spec

import io.kotest.core.spec.style.DescribeSpec
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import com._16minutes.liquiprime.sql.ConnectionFactory
import com._16minutes.liquiprime.sql.DriverProvider
import com._16minutes.liquiprime.sql.LiquiprimeDriver
import java.sql.Connection
import java.sql.Driver
import java.util.*

class ConnectionFactorySpec: DescribeSpec({
    describe("createConnectionFor") {
        it("""creates a connection to a database using the first driver that the invoking
            | object's driver provider determines is associated with the supplied JDBC URL,
            | the URL itself, and the supplied database connection properties, if the 
            | supplied driver class name is null""".trimMargin()) {
            val jdbcUrl = "liquiprime://localhost:9/test"
            val driverProperties = Properties()
            val driver = mockk<Driver>()
            val connection = mockk<Connection>()
            val driverProvider = mockk<DriverProvider>()
            every { driver.connect(jdbcUrl, driverProperties) } returns connection
            every { driverProvider.getDriverFor(jdbcUrl) } returns driver
            val cut = ConnectionFactory(driverProvider)

            cut.createConnectionFor(jdbcUrl, null, driverProperties)

            verify { driver.connect(jdbcUrl, driverProperties) }
            verify { driverProvider.getDriverFor(jdbcUrl) }
            confirmVerified(driver, driverProvider)
        }

        it("""creates a connection to a database using a driver procured by the invoking 
            | object's driver provider of the type represented by the supplied driver class
            | name, the supplied JDBC URL, and the supplied database connection properties,
            | if the supplied driver class name is not null""".trimMargin()) {
            val jdbcUrl = "liquiprime://localhost:9/test"
            val driverProperties = Properties()
            val driver = mockk<LiquiprimeDriver>()
            val connection = mockk<Connection>()
            val driverProvider = mockk<DriverProvider>()
            val registeredDrivers = Vector<Driver>(listOf(driver))
            every { driver.connect(jdbcUrl, driverProperties) } returns connection
            every { driverProvider.getRegisteredDrivers() } returns registeredDrivers.elements()
            val cut = ConnectionFactory(driverProvider)

            cut.createConnectionFor(jdbcUrl, driver.javaClass.name, driverProperties)

            verify { driver.connect(jdbcUrl, driverProperties) }
            verify { driverProvider.getRegisteredDrivers() }
            confirmVerified(driver, driverProvider)
        }
    }
})
