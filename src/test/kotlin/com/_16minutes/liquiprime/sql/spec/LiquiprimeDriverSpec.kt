package com._16minutes.liquiprime.sql.spec

import com._16minutes.liquiprime.assets.PSEUDO_DATABASE_FILE_NAME_PREFIX
import com._16minutes.liquiprime.sql.LiquiprimeConnection
import com._16minutes.liquiprime.sql.LiquiprimeConnection.LiquiprimeConnectionProperties
import com._16minutes.liquiprime.sql.LiquiprimeDriver
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.NamedTag
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.equals.shouldBeEqual
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import java.nio.file.Files
import java.sql.Connection
import java.sql.SQLException
import java.util.*

class LiquiprimeDriverSpec: DescribeSpec({
    val doesRequireValidDriverUrl = NamedTag("doRequireValidDriverUrl")
    var connection: Connection? = null
    lateinit var validDriverUrl: String

    beforeEach {
        if (it.config.tags.contains(doesRequireValidDriverUrl)) {
            val tempFilePath = Files.createTempFile(PSEUDO_DATABASE_FILE_NAME_PREFIX, "txt")
            validDriverUrl = String.format(LiquiprimeDriver.URL_TEMPLATE, tempFilePath.toString())
        }
    }

    afterEach {
        if (it.a.config.tags.contains(doesRequireValidDriverUrl)) {
            connection?.close()
        }
    }

    describe("connect") {
        it("creates a LiquiprimeConnection to the location specified in the argument URL in" +
                " the absence of connection properties specified either in the URL or as an argument"
        ).config(tags = setOf(doesRequireValidDriverUrl)) {
            val cut = LiquiprimeDriver()

            connection = cut.connect(validDriverUrl, null)

            connection!!::class.shouldBe(LiquiprimeConnection::class)
        }

        it("creates a LiquiprimeConnection to the location specified in the" +
                " argument URL with the connection properties specified in the URL"
        ).config(tags = setOf(doesRequireValidDriverUrl)) {
            val cut = LiquiprimeDriver()
            val connectionProperties = LiquiprimeConnectionProperties()
            val validDriverUrlWithProperties = "${validDriverUrl}?${connectionProperties}"

            connection = cut.connect(validDriverUrlWithProperties, null)

            connection!!::class.shouldBe(LiquiprimeConnection::class)
        }

        it("creates a LiquiprimeConnection to the location specified in the argument URL" +
                " with the connection properties specified argument Properties object"
        ).config(tags = setOf(doesRequireValidDriverUrl)) {
            val cut = LiquiprimeDriver()
            val connectionProperties = LiquiprimeConnectionProperties()

            connection = cut.connect(validDriverUrl, connectionProperties.toGenericProperties())

            connection!!::class.shouldBe(LiquiprimeConnection::class)
        }

        it("creates a LiquiprimeConnection to the location specified in the argument URL" +
                " with the connection properties specified in the URL and argument Properties object," +
                " with those in the latter overriding those in the former"
        ).config(tags = setOf(doesRequireValidDriverUrl)) {
            val cut = LiquiprimeDriver()
            val urlConnectionProperties = LiquiprimeConnectionProperties(false)
            val validDriverUrlWithProperties = "${validDriverUrl}?${urlConnectionProperties}"
            val standaloneConnectionProperties = LiquiprimeConnectionProperties(true)

            connection = cut.connect(validDriverUrlWithProperties, standaloneConnectionProperties.toGenericProperties())

            val nonNullableResult = connection!!
            nonNullableResult::class.shouldBe(LiquiprimeConnection::class)
            (nonNullableResult as LiquiprimeConnection).connectionProperties.shouldBeEqual(standaloneConnectionProperties)
        }

        it("returns null if the argument URL does not match LiquiprimeDriver.URL_PATTERN") {
            val cut = LiquiprimeDriver()
            LiquiprimeDriver.URL_PATTERN.matcher(INVALID_DRIVER_URL).matches().shouldBeFalse()

            cut.connect(INVALID_DRIVER_URL, null).shouldBeNull()
        }

        it("""throws an SQLException with LiquiprimeDriver.ExceptionMessages.NULL_URL
            | as the message if the argument URL is null""".trimMargin()) {
            val cut = LiquiprimeDriver()

            val exceptions = listOf (
                shouldThrow<SQLException> {
                    cut.connect(null, null)
                },
                shouldThrow<SQLException> {
                    cut.connect(null, Properties())
                }
            )

            exceptions.all{ it.message == LiquiprimeDriver.ExceptionMessages.NULL_URL }.shouldBeTrue()
        }
    }

    describe("acceptsUrl") {
        it("returns true if the argument URL matches LiquiprimeDriver.URL_PATTERN")
            .config(tags = setOf(doesRequireValidDriverUrl)) {
            val cut = LiquiprimeDriver()
            LiquiprimeDriver.URL_PATTERN.matcher(validDriverUrl).matches().shouldBeTrue()

            cut.acceptsURL(validDriverUrl).shouldBeTrue()
        }

        it("returns false if the argument URL does not match LiquiprimeDriver.URL_PATTERN") {
            val cut = LiquiprimeDriver()
            LiquiprimeDriver.URL_PATTERN.matcher(INVALID_DRIVER_URL).matches().shouldBeFalse()

            cut.acceptsURL(INVALID_DRIVER_URL).shouldBeFalse()
        }

        it("""throws an SQLException with LiquiprimeDriver.ExceptionMessages.NULL_URL
            | as the message if the argument URL is null""".trimMargin()) {
            val cut = LiquiprimeDriver()

            val exception = shouldThrow<SQLException> {
                cut.acceptsURL(null)
            }

            exception.message.shouldBe(LiquiprimeDriver.ExceptionMessages.NULL_URL)
        }
    }
}) {
    companion object {
        const val INVALID_DRIVER_URL = "jdbc:mysql://localhost:9/test"
    }
}
