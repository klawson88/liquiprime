package com._16minutes.liquiprime.util

import java.io.InputStream
import java.util.regex.Pattern

/**
 * Parses query language statements from an InputStream which 
 * delivers content in the format of a Liquibase SQL changelog.
 * A description of the format can be found here: https://tinyurl.com/ttpx66w7
 */
fun parseQueryLanguageFileStatements(inputStream: InputStream): List<String> {
    val statements = mutableListOf<String>()
    
    val goKeywordPattern = Pattern.compile("\\bGO$")

    var isInSingleQuote = false
    var isInDoubleQuote = false
    var isInSingleLineComment = false
    var isInMultiLineComment = false
    var isInQuoteOrComment = false

    var currentChar: Char? = null
    var previousChar: Char? = null
    val currentStatementStringBuilder = StringBuilder()

    val inputStreamReader = inputStream.bufferedReader()

    var currentCharInt = inputStreamReader.read()
    while (currentCharInt != -1) {
        currentChar = currentCharInt.toChar()

        var isCharStatementDelimiter = false
        var doOverrideCharAppend = false

        when (currentChar) {
            '\'' -> {
                if (!isInQuoteOrComment) {
                    isInSingleQuote = true
                } else if (isInSingleQuote && previousChar != '\\') {
                    isInSingleQuote = false
                }
            }
            '"' -> {
                if (!isInQuoteOrComment) {
                    isInDoubleQuote = true
                } else if (isInDoubleQuote && previousChar != '\\') {
                    isInDoubleQuote = false
                }
            }
            '-', '\n' -> {
                if (!isInQuoteOrComment && previousChar == '-' && currentChar == '-') {
                    isInSingleLineComment = true
                    currentStatementStringBuilder.deleteCharAt(currentStatementStringBuilder.length - 1)
                } else if (isInSingleLineComment && currentChar == '\n') {
                    isInSingleLineComment = false
                    doOverrideCharAppend = true
                }
            }
            '/', '*' -> {
                if (!isInQuoteOrComment && previousChar == '/' && currentChar == '*') {
                    isInMultiLineComment = true
                    currentStatementStringBuilder.deleteCharAt(currentStatementStringBuilder.length - 1)
                } else if (isInMultiLineComment && previousChar == '*' && currentChar == '/') {
                    isInMultiLineComment = false
                    doOverrideCharAppend = true
                }
            }
            ';' -> {
                if (!isInQuoteOrComment) {
                    currentStatementStringBuilder.append(currentChar)
                    isCharStatementDelimiter = true
                }
            }
        }

        val didReachGoKeyword = 
            !isInQuoteOrComment
            && goKeywordPattern.matcher(currentStatementStringBuilder.toString()).find()
            && Character.isWhitespace(currentChar)
        val canParseStatement = (!isInQuoteOrComment && isCharStatementDelimiter) || didReachGoKeyword

        if (canParseStatement) {
            if (didReachGoKeyword) {
                val currentStatementLength = currentStatementStringBuilder.length
                currentStatementStringBuilder.delete(currentStatementLength - 2, currentStatementLength)
            }

            statements.add(currentStatementStringBuilder.toString().trim())
            currentStatementStringBuilder.clear()
        } else if (!isInSingleLineComment && !isInMultiLineComment && !doOverrideCharAppend){
            currentStatementStringBuilder.append(currentChar)
        }

        previousChar = currentChar
        isInQuoteOrComment = isInSingleQuote || isInDoubleQuote || isInSingleLineComment || isInMultiLineComment

        currentCharInt = inputStreamReader.read()
    }

    if (currentStatementStringBuilder.isNotEmpty()) {
        statements.add(currentStatementStringBuilder.toString().trim())
        currentStatementStringBuilder.clear()
    }

    return statements
}
