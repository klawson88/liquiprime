package com._16minutes.liquiprime.tasks.exceptions

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import java.nio.file.Paths

class LiquiprimePrimeDatabasesExceptionSpec: DescribeSpec({
    describe("companion object") {
        describe("createMessage") {
            it("""returns a String in the format defined by SETUP_FAILURE_MESSAGE_TEMPLATE
                | for the specified activity if the specified primer file path is null""".trimMargin()) {
                val activityName = "foo"
                val expectedResult = String.format(
                    LiquiprimePrimeDatabasesException.SETUP_FAILURE_MESSAGE_TEMPLATE,
                    activityName
                )

                val actualResult = LiquiprimePrimeDatabasesException.createMessage(activityName, null)

                actualResult.shouldBe(expectedResult)
            }

            it("""returns a String in the format defined by EXECUTION_FAILURE_MESSAGE_TEMPLATE
                | for the specified activity and primer file path if the latter is not null""".trimMargin()) {
                val activityName = "foo"
                val primerFilePath = Paths.get("tmp", "foo.sql").toString()
                val expectedResult = String.format(
                    LiquiprimePrimeDatabasesException.EXECUTION_FAILURE_MESSAGE_TEMPLATE,
                    primerFilePath,
                    activityName
                )

                val actualResult = LiquiprimePrimeDatabasesException.createMessage(activityName, primerFilePath)

                actualResult.shouldBe(expectedResult)
            }
        }
    }
})
