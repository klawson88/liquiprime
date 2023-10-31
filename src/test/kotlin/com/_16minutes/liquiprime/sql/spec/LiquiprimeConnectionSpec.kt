package com._16minutes.liquiprime.sql.spec

import com._16minutes.liquiprime.assets.PSEUDO_DATABASE_FILE_NAME_PREFIX
import com._16minutes.liquiprime.sql.LiquiprimeConnection
import com._16minutes.liquiprime.sql.LiquiprimeConnection.LiquiprimeConnectionStatement
import com._16minutes.liquiprime.sql.LiquiprimeConnection.LiquiprimeConnectionProperties
import io.kotest.assertions.throwables.shouldNotThrow
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.equals.shouldBeEqual
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldBeEmpty
import io.kotest.matchers.string.shouldEndWith
import io.mockk.every
import java.io.File
import java.io.FileOutputStream
import java.nio.file.Files
import java.sql.SQLException
import java.util.*

class LiquiprimeConnectionSpec: DescribeSpec({
    lateinit var cut: LiquiprimeConnection

    fun createConnectionForTest(
        outputFile:File = Files.createTempFile(PSEUDO_DATABASE_FILE_NAME_PREFIX, "txt").toFile(),
        connectionProperties: LiquiprimeConnectionProperties = LiquiprimeConnectionProperties()
    ):LiquiprimeConnection {
        return LiquiprimeConnection(outputFile, connectionProperties)
    }

    beforeEach {
        cut = createConnectionForTest()
    }

    afterEach {
        cut.close()
    }

    describe("LiquiprimeStatement") {
        describe("execute") {
            it("""writes the argument to the file used to create the associated connection,
                | if the auto-commit setting of the connection is true, with no System.lineSeparator()
                | preceding the argument if no prior invocations were made""".trimMargin()) {
                cut.autoCommit = true
                val statementCut = cut.createStatement()
                val textStatement = "CREATE DATABASE foo;"

                statementCut.execute(textStatement)

                cut.outputFile.readText().shouldBe(textStatement)
            }

            it("""writes the argument to the file used to create the associated connection,
                | if the auto-commit setting of the connection is true, with System.lineSeparator()
                | preceding the argument if prior invocations were made""".trimMargin()) {
                cut.autoCommit = true
                val statementCut = cut.createStatement()
                val textStatements = listOf("CREATE DATABASE foo;", "CREATE DATABASE bar;")
                val validationText ="${System.lineSeparator()}${textStatements[1]}"

                textStatements.forEach {
                    statementCut.execute(it)
                }

                cut.outputFile.readText().shouldEndWith(validationText)
            }

            it("""buffers the argument if the auto-commit setting of the associated connection is false,
                | with System.lineSeparator() preceding the argument if prior invocations were made, 
                | to be written to the file used to create the connection 
                | when its 'commit' method is invoked""".trimMargin()) {
                cut.autoCommit = false
                val statementCut = cut.createStatement()
                val textStatements = listOf("CREATE DATABASE foo;", "CREATE DATABASE bar;")
                val validationText ="${System.lineSeparator()}${textStatements[1]}"

                textStatements.forEach {
                    statementCut.execute(it)
                }

                val outputFile = cut.outputFile
                outputFile.readText().shouldBeEmpty()
                cut.commit()
                outputFile.readText().shouldEndWith(validationText)
            }

            it("""buffers the argument if the auto-commit setting of the associated connection is false,
                | with no System.lineSeparator() preceding the argument if prior invocations were not made, 
                | to be written to the file used to create the connection 
                | when its 'commit' method is invoked""".trimMargin()) {
                cut.autoCommit = false
                val statementCut = cut.createStatement()
                val textStatement = "CREATE DATABASE test;"

                statementCut.execute(textStatement)

                val outputFile = cut.outputFile
                outputFile.readText().shouldBeEmpty()
                cut.commit()
                outputFile.readText().shouldBe(textStatement)
            }

            it("returns false if the argument is null") {
                val statementCut = cut.createStatement()

                statementCut.execute(null).shouldBeFalse()
            }

            it("""throws an SQLException with
            | LiquiprimeConnection.ExceptionMessages.EXCEPTION_THROW_CONNECTION_PROPERTY_TRUTHINESS
            | as the message if this@LiquiprimeConnection.connectionProperties.doThrowExceptionsForAllOperations
            | is true""".trimMargin()) {
                cut = createConnectionForTest(connectionProperties = LiquiprimeConnectionProperties(true))
                val statementCut = cut.createStatement()

                val exception = shouldThrow<SQLException> {
                    statementCut.execute("CREATE DATABASE test;")
                }

                exception
                    .message
                    .shouldBe(
                        LiquiprimeConnection
                            .ExceptionMessages
                            .DO_THROW_EXCEPTIONS_FOR_ALL_OPERATIONS_PROPERTY_TRUTHINESS
                    )
            }
        }

        describe("close") {
            it("does not throw a NotImplementedError") {
                val statementCut = cut.createStatement()

                shouldNotThrow<NotImplementedError> {
                    statementCut.close()
                }
            }
        }
    }

    describe("LiquiprimeConnectionProperties") {
        describe("toString") {
            val doThrowExceptionsForAllOperations = false
            val expectedResult = "doThrowExceptionsForAllOperations=$doThrowExceptionsForAllOperations"
            val propertiesCut = LiquiprimeConnectionProperties(doThrowExceptionsForAllOperations)

            val actualResult = propertiesCut.toString()

            actualResult.shouldBe(expectedResult)
        }

        describe("toGenericProperties") {
            val doThrowExceptionsForAllOperations = false
            val expectedResult = Properties()
            expectedResult.setProperty("doThrowExceptionsForAllOperations", doThrowExceptionsForAllOperations.toString())
            val propertiesCut = LiquiprimeConnectionProperties(doThrowExceptionsForAllOperations)

            val actualResult = propertiesCut.toGenericProperties()

            actualResult.shouldBeEqual(expectedResult)
        }
    }

    describe("createStatement") {
        it("returns a LiquiprimeConnectionStatement") {
            val result = cut.createStatement()

            result::class.shouldBe(LiquiprimeConnectionStatement::class)
        }
    }

    describe("commit") {
        it("""throws an SQLException with 
            | LiquiprimeConnection.ExceptionMessages.EXCEPTION_THROW_CONNECTION_PROPERTY_TRUTHINESS
            | as the message if connectionProperties.doThrowExceptionsForAllOperations is true""".trimMargin()) {
            val cut = createConnectionForTest(connectionProperties = LiquiprimeConnectionProperties(true))

            val exception = shouldThrow<SQLException> {
                cut.commit()
            }

            exception
                .message
                .shouldBe(LiquiprimeConnection.ExceptionMessages.DO_THROW_EXCEPTIONS_FOR_ALL_OPERATIONS_PROPERTY_TRUTHINESS)
        }

        it("""writes the statement arguments to prior 'execute' method invocations, while
            | auto-commit mode was disabled, to the file used to create the connection """.trimMargin()) {
            val outputFile = cut.outputFile
            cut.autoCommit = false
            val statement = cut.createStatement()
            val statementText = "CREATE DATABASE TEST;"
            statement.execute(statementText)
            outputFile.readText().shouldBeEmpty()

            cut.commit()

            outputFile.readText().shouldBe(statementText);
        }
    }

    describe("rollback") {
        it("does not throw a NonImplementedError") {
            shouldNotThrow<NotImplementedError> {
                cut.rollback()
            }
        }
    }

    describe("close") {
        it("does not throw a NotImplementedError") {
            shouldNotThrow<NotImplementedError> {
                cut.close()
            }
        }
    }
})
