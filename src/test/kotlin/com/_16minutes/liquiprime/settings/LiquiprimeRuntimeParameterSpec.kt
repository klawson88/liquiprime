package com._16minutes.liquiprime.settings

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.booleans.shouldBeTrue
import io.mockk.every
import io.mockk.mockk
import com._16minutes.liquiprime.env.EnvironmentVariableLoader
import com._16minutes.liquiprime.properties.SystemPropertyLoader
import java.util.*

class LiquiprimeRuntimeParameterSpec: DescribeSpec({
    describe("getValueFor") {
        fun performTest(systemPropertyValue: String?, environmentVariableValue: String?, expectedValue: String?) {
            val activityName = "foo"
            val systemPropertyTemplate = "liquiprime.%s.test"
            val activitySystemProperty = String.format(systemPropertyTemplate, activityName)
            val environmentVariableTemplate = "LIQUIPRIME_%s_TEST"
            val environmentVariableProperty = String.format(environmentVariableTemplate, activityName)
            val systemPropertyLoader = mockk<SystemPropertyLoader>()
            every { systemPropertyLoader.load(activitySystemProperty) } returns systemPropertyValue
            val environmentValueLoader = mockk<EnvironmentVariableLoader>()
            every { environmentValueLoader.get(environmentVariableProperty) } returns environmentVariableValue

            val actualValue = LiquiprimeRuntimeParameter(
                systemPropertyTemplate,
                environmentVariableTemplate,
                systemPropertyLoader,
                environmentValueLoader
            ).getValueFor(activityName)


            Objects.equals(actualValue, expectedValue).shouldBeTrue()
        }

        it("""returns null if the parameter represented by the invoking object for the given
            | activity does not have an associated system property or environment variable """.trimMargin()) {
            performTest(
                systemPropertyValue = null,
                environmentVariableValue = null,
                expectedValue = null
            )
        }

        it("""returns the value of the parameter represented by the invoking object for the
            | given activity, as defined by the associated system property, if such a property exists""".trimMargin()) {
            performTest(
                systemPropertyValue = "foo",
                environmentVariableValue = null,
                expectedValue = "foo"
            )
        }

        it("""returns the value of the parameter represented by the invoking object
            | for the given activity, as defined by the associated environment variable,
            | if an associated system property doesn't exist""".trimMargin()) {
            performTest(
                systemPropertyValue = null,
                environmentVariableValue = "foo",
                expectedValue = "foo"
            )
        }

        it("""returns the value of the parameter represented by the invoking object
            | for the given activity, as defined by the associated system property, 
            | if both the property and an associated environment variable exist""".trimMargin()) {
            performTest(
                systemPropertyValue = "foo",
                environmentVariableValue = "bar",
                expectedValue = "foo"
            )
        }
    }
})
