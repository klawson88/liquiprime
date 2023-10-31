package com._16minutes.liquiprime.util.spec

import io.kotest.core.spec.style.DescribeSpec

class ConnectionUtilsSpec: DescribeSpec({
    describe("createConnection") {
        it("""creates a connection to a database using the first driver that
            | DriverManager determines is associated with the supplied JDBC URL, 
            | the URL itself, and the supplied database connection properties,
            | if the supplied driver class name is null""".trimMargin()) {

        }

        it("""creates a connection to a database using a driver of the type 
            | represented by the supplied driver class name, the supplied JDBC URL,
            | and the supplied database connection properties, if the supplied driver
            | class name is not null""".trimMargin()) {

        }
    }
})
