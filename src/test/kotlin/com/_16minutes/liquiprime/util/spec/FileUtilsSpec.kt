package com._16minutes.liquiprime.util.spec

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.equals.shouldBeEqual
import io.mockk.InternalPlatformDsl.toArray
import com._16minutes.liquiprime.util.parseQueryLanguageFileStatements

class FileUtilsSpec: DescribeSpec({
    describe("parseQueryLanguageFileStatements") {
        fun performTest(sqlPrimerContentComponents: List<String>) {
            val sqlPrimerContent = sqlPrimerContentComponents.joinToString("\n")

            val actualSqlStatements = sqlPrimerContent.byteInputStream().use {
                parseQueryLanguageFileStatements(it)
            }

            val expectedSqlStatements =
                sqlPrimerContentComponents
                    .asSequence()
                    .filter {
                        it.endsWith(";") || it.endsWith("GO")
                    }.map {
                        if (it.endsWith("GO")) {
                            it.replace(Regex("\\s+GO$"), "")
                        } else {
                            it
                        }
                    }.toList()
            actualSqlStatements.shouldBeEqual(expectedSqlStatements)
        }

        it("returns the statements from a query language file that does not contain any strings or comments") {
            performTest(listOf(
                "CREATE DATABASE test;",
                """CREATE TABLE test.test_table ( 
                    | column_1 INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
                    | column_2 VARCHAR(100),
                    | column_3 VARCHAR(100)
                |) GO""".trimMargin(),
                "INSERT INTO test.test_table (column_1, column_2, column_3) VALUES (1, null, null);"
            ))
        }

        it("""returns the statements from a query language file that contains no strings or comments
            | other than single-quote strings that do not contain statement delimiters""".trimMargin()) {
            performTest(listOf(
                "CREATE DATABASE test;",
                """CREATE TABLE test.test_table ( 
                    | column_1 INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
                    | column_2 VARCHAR(100),
                    | column_3 VARCHAR(100)
                |) GO""".trimMargin(),
                "INSERT INTO test.test_table (column_1, column_2, column_3) VALUES (1, 'foo', 'bar');"
            ))
        }

        it("""returns the statements from a query language file that contains no strings or comments
            | other than single-quote strings that do contain statement delimiters""".trimMargin()) {
            performTest(listOf(
                "CREATE DATABASE test;",
                """CREATE TABLE test.test_table ( 
                    | column_1 INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
                    | column_2 VARCHAR(100),
                    | column_3 VARCHAR(100)
                |) GO""".trimMargin(),
                "INSERT INTO test.test_table (column_1, column_2, column_3) VALUES (1, 'fo;o', 'bGOar');"
            ))
        }

        it("""returns the statements from a query language file that contains no strings or comments
            | other than double-quote strings that do not contain statement delimiters""".trimMargin()) {
            performTest(listOf(
                "CREATE DATABASE test;",
                """CREATE TABLE test.test_table ( 
                    | column_1 INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
                    | column_2 VARCHAR(100),
                    | column_3 VARCHAR(100)
                |) GO""".trimMargin(),
                """INSERT INTO test.test_table (column_1, column_2, column_3) VALUES (1, "foo", "bar");"""
            ))
        }

        it("""returns the statements from a query language file that contains no strings or comments
            | other than double-quote strings that do contain statement delimiters""".trimMargin()) {
            performTest(listOf(
                "CREATE DATABASE test;",
                """CREATE TABLE test.test_table ( 
                    | column_1 INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
                    | column_2 VARCHAR(100),
                    | column_3 VARCHAR(100)
                |) GO""".trimMargin(),
                """INSERT INTO test.test_table (column_1, column_2, column_3) VALUES (1, "fGOoo", "ba;r");"""
            ))
        }

        it("""returns the statements from a query language file that contains no strings or comments
            | other than single-line comments that do not contain statement delimiters""".trimMargin()) {
            performTest(listOf(
                "CREATE DATABASE test;",
                "-- This is a test comment\n",
                """CREATE TABLE test.test_table ( 
                    | column_1 INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
                    | column_2 VARCHAR(100),
                    | column_3 VARCHAR(100)
                |) GO""".trimMargin(),
                "-- This is another test comment\n",
                """INSERT INTO test.test_table (column_1, column_2, column_3) VALUES (1, null, null);"""
            ))
        }

        it("""returns the statements from a query language file that contains no strings or comments
            | other than single-line comments that do contain statement delimiters""".trimMargin()) {
            performTest(listOf(
                "CREATE DATABASE test;",
                "-- This is a test comment;\n",
                """CREATE TABLE test.test_table ( 
                    | column_1 INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
                    | column_2 VARCHAR(100),
                    | column_3 VARCHAR(100)
                |) GO""".trimMargin(),
                "-- This is another test commentGO\n",
                """INSERT INTO test.test_table (column_1, column_2, column_3) VALUES (1, null, null);"""
            ))
        }

        it("""returns the statements from a query language file that contains no strings or comments
            | other than multi-line comments that do not contain statement delimiters""".trimMargin()) {
            performTest(listOf(
                "CREATE DATABASE test;",
                """/* This is a test comment 
                    | that spans multiple lines
                    |*/""".trimMargin(),
                """CREATE TABLE test.test_table ( 
                    | column_1 INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
                    | column_2 VARCHAR(100),
                    | column_3 VARCHAR(100)
                |) GO""".trimMargin(),
                """/* This is a another test comment 
                    | that spans multiple lines
                    |*/""".trimMargin(),
                """INSERT INTO test.test_table (column_1, column_2, column_3) VALUES (1, null, null);"""
            ))
        }

        it("""returns the statements from a query language file that contains no strings or comments
            | other than multi-line comments that do contain statement delimiters""".trimMargin()) {
            performTest(listOf(
                "CREATE DATABASE test;",
                """/* This is a test comment 
                    | that spans multiple lines;
                    |*/""".trimMargin(),
                """CREATE TABLE test.test_table ( 
                    | column_1 INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
                    | column_2 VARCHAR(100),
                    | column_3 VARCHAR(100)
                |) GO""".trimMargin(),
                """/* This is a test comment 
                    | that spans multiple linesGO
                    |*/""".trimMargin(),
                """INSERT INTO test.test_table (column_1, column_2, column_3) VALUES (1, null, null);"""
            ))
        }

        it("""returns the statements from a query language file that contains
            | every type of string and comment without statement delimiters""".trimMargin()) {
            performTest(listOf(
                "CREATE DATABASE test;",
                "-- This is a test comment\n",
                """/* This is a test comment 
                    | that spans multiple lines
                    |*/""".trimMargin(),
                """CREATE TABLE test.test_table ( 
                    | column_1 INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
                    | column_2 VARCHAR(100),
                    | column_3 VARCHAR(100)
                |) GO""".trimMargin(),
                "-- This is another test commentGO\n",
                """/* This is a test comment 
                    | that spans multiple lines
                    |*/""".trimMargin(),
                """INSERT INTO test.test_table (column_1, column_2, column_3) VALUES (1, 'foo', "foo");"""
            ))
        }

        it("""returns the statements from a query language file that contains
            | every type of string and comment with statement delimiters""".trimMargin()) {
            performTest(listOf(
                "CREATE DATABASE test;",
                "-- This is a test comment;\n",
                """/* This is a test comment 
                    | that spans multiple linesGO
                    |*/""".trimMargin(),
                """CREATE TABLE test.test_table ( 
                    | column_1 INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
                    | column_2 VARCHAR(100),
                    | column_3 VARCHAR(100)
                |) GO""".trimMargin(),
                "-- This is another test commentGO\n",
                """/* This is a test comment 
                    | that spans multiple lines;
                    |*/""".trimMargin(),
                """INSERT INTO test.test_table (column_1, column_2, column_3) VALUES (1, 'f;oo', "foDOo");"""
            ))
        }
    }
})
