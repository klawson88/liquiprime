package com._16minutes.liquiprime.settings

import com._16minutes.liquiprime.env.EnvironmentVariableLoader
import com._16minutes.liquiprime.properties.SystemPropertyLoader
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.booleans.shouldBeTrue
import io.mockk.every
import io.mockk.mockk
import java.util.Objects

class LiquiprimeDeserializableRuntimeParameterSpec: DescribeSpec ({
    describe("getDeserializedValueFor") {
        fun <T> performTest(
            systemPropertyValue: String?,
            environmentVariableValue: String?,
            expectedDeserializedValue: T?
        ) {
            val activityName = "foo"
            val systemPropertyNameTemplate = "liquiprime.%s.test"
            val systemPropertyName = String.format(systemPropertyNameTemplate, activityName)
            val environmentVariableNameTemplate = "LIQUIPRIME_%s_TEST"
            val environmentVariableName = String.format(environmentVariableNameTemplate, activityName)
            val deserializer = mockk<(String) -> T>()
            val systemPropertyLoader = mockk<SystemPropertyLoader>()
            every { systemPropertyLoader.load(systemPropertyName) } returns systemPropertyValue
            val environmentValueLoader = mockk<EnvironmentVariableLoader>()
            every { environmentValueLoader.get(environmentVariableName) } returns environmentVariableValue
            val effectiveParameterValue = systemPropertyValue ?: environmentVariableValue;
            if (effectiveParameterValue != null  && expectedDeserializedValue != null) {
                every { deserializer.invoke(effectiveParameterValue) } returns expectedDeserializedValue
            }

            val actualValue = LiquiprimeDeserializableRuntimeParameter(
                systemPropertyNameTemplate,
                environmentVariableNameTemplate,
                deserializer,
                systemPropertyLoader,
                environmentValueLoader
            ).getDeserializedValueFor(activityName)

            Objects.equals(actualValue, expectedDeserializedValue).shouldBeTrue()
        }

        it("""returns null if the parameter represented by the invoking object for the given
            | activity does not have an associated system property or environment variable """.trimMargin()) {
            performTest(
                systemPropertyValue = null,
                environmentVariableValue = null,
                expectedDeserializedValue = null
            )
        }

        it("""returns the value of the parameter represented by the invoking object for the
            | given activity, as defined by the associated system property, if such a property exists""".trimMargin()) {
            performTest(
                systemPropertyValue = "1",
                environmentVariableValue = null,
                expectedDeserializedValue = 1
            )
        }

        it("""returns the value of the parameter represented by the invoking object
            | for the given activity, as defined by the associated environment variable,
            | if an associated system property doesn't exist""".trimMargin()) {
            performTest(
                systemPropertyValue = null,
                environmentVariableValue = "1",
                expectedDeserializedValue = 1
            )
        }

        it("""returns the value of the parameter represented by the invoking object
            | for the given activity, as defined by the associated system property, 
            | if both the property and an associated environment variable exist""".trimMargin()) {
            performTest(
                systemPropertyValue = "1",
                environmentVariableValue = "2",
                expectedDeserializedValue = 1
            )
        }
    }

    describe("getDeserializedValue") {
        fun <T> performTest(
            systemPropertyValue: String?,
            environmentVariableValue: String?,
            expectedDeserializedValue: T?
        ) {
            val systemPropertyName = "liquiprime.test"
            val environmentVariableName = "LIQUIPRIME_TEST"
            val deserializer = mockk<(String) -> T>()
            val systemPropertyLoader = mockk<SystemPropertyLoader>()
            every { systemPropertyLoader.load(systemPropertyName) } returns systemPropertyValue
            val environmentValueLoader = mockk<EnvironmentVariableLoader>()
            every { environmentValueLoader.get(environmentVariableName) } returns environmentVariableValue
            val effectiveParameterValue = systemPropertyValue ?: environmentVariableValue;
            if (effectiveParameterValue != null  && expectedDeserializedValue != null) {
                every { deserializer.invoke(effectiveParameterValue) } returns expectedDeserializedValue
            }

            val actualValue = LiquiprimeDeserializableRuntimeParameter(
                systemPropertyName,
                environmentVariableName,
                deserializer,
                systemPropertyLoader,
                environmentValueLoader
            ).getDeserializedValue()

            Objects.equals(actualValue, expectedDeserializedValue).shouldBeTrue()
        }

        it("""returns null if the parameter represented by the invoking object
            | does not have an associated system property or environment variable""".trimMargin()) {
            performTest(
                systemPropertyValue = null,
                environmentVariableValue = null,
                expectedDeserializedValue = null
            )
        }

        it("""returns the value of the parameter represented by the invoking object as defined
            | by the associated system property, if such a property exists""".trimMargin()) {
            performTest(
                systemPropertyValue = "1",
                environmentVariableValue = null,
                expectedDeserializedValue = 1
            )
        }

        it("""returns the value of the parameter represented by the
            | invoking object as defined by the associated environment variable,
            |  if an associated system property doesn't exist""".trimMargin()) {
            performTest(
                systemPropertyValue = null,
                environmentVariableValue = "1",
                expectedDeserializedValue = 1
            )
        }

        it("""returns the value of the parameter represented
            | by the invoking object as defined by the associated system property, 
            | if both the property and an associated environment variable exist""".trimMargin()) {
            performTest(
                systemPropertyValue = "1",
                environmentVariableValue = "2",
                expectedDeserializedValue = 1
            )
        }
    }
})