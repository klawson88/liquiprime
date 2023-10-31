package com._16minutes.liquiprime.sql

import java.io.File
import java.io.PrintWriter
import java.sql.*
import java.util.*
import java.util.concurrent.Executor
import kotlin.collections.ArrayDeque

class LiquiprimeConnection(
    val outputFile: File,
    val connectionProperties: LiquiprimeConnectionProperties
): Connection {
    data class LiquiprimeConnectionProperties(val doThrowExceptionsForAllOperations: Boolean = false) {

        fun toGenericProperties(): Properties {
            val genericProperties = Properties()
            genericProperties
                .setProperty("doThrowExceptionsForAllOperations", doThrowExceptionsForAllOperations.toString())

            return genericProperties
        }
        override fun  toString(): String {
            return "doThrowExceptionsForAllOperations=$doThrowExceptionsForAllOperations"
        }
    }

    object ExceptionMessages {
        const val DO_THROW_EXCEPTIONS_FOR_ALL_OPERATIONS_PROPERTY_TRUTHINESS =
            """doThrowExceptionsForAllOperations property for the conduit connection of the operation is true"""
    }

    private val sqlWriter = PrintWriter(outputFile.outputStream())
    private val statementSqlQueue = ArrayDeque<String>()
    private var isAutoCommitEnabled = true
    private var haveExecutedStatement = false

    inner class LiquiprimeConnectionStatement: Statement {
        override fun <T : Any?> unwrap(p0: Class<T>?): T {
            TODO("Not yet implemented")
        }

        override fun isWrapperFor(p0: Class<*>?): Boolean {
            TODO("Not yet implemented")
        }

        override fun close() {

        }

        override fun executeQuery(p0: String?): ResultSet {
            TODO("Not yet implemented")
        }

        override fun executeUpdate(p0: String?): Int {
            TODO("Not yet implemented")
        }

        override fun executeUpdate(p0: String?, p1: Int): Int {
            TODO("Not yet implemented")
        }

        override fun executeUpdate(p0: String?, p1: IntArray?): Int {
            TODO("Not yet implemented")
        }

        override fun executeUpdate(p0: String?, p1: Array<out String>?): Int {
            TODO("Not yet implemented")
        }

        override fun getMaxFieldSize(): Int {
            TODO("Not yet implemented")
        }

        override fun setMaxFieldSize(p0: Int) {
            TODO("Not yet implemented")
        }

        override fun getMaxRows(): Int {
            TODO("Not yet implemented")
        }

        override fun setMaxRows(p0: Int) {
            TODO("Not yet implemented")
        }

        override fun setEscapeProcessing(p0: Boolean) {
            TODO("Not yet implemented")
        }

        override fun getQueryTimeout(): Int {
            TODO("Not yet implemented")
        }

        override fun setQueryTimeout(p0: Int) {
            TODO("Not yet implemented")
        }

        override fun cancel() {
            TODO("Not yet implemented")
        }

        override fun getWarnings(): SQLWarning {
            TODO("Not yet implemented")
        }

        override fun clearWarnings() {
            TODO("Not yet implemented")
        }

        override fun setCursorName(p0: String?) {
            TODO("Not yet implemented")
        }

        override fun execute(p0: String?): Boolean {
            if (this@LiquiprimeConnection.connectionProperties.doThrowExceptionsForAllOperations) {
                throw SQLException(ExceptionMessages.DO_THROW_EXCEPTIONS_FOR_ALL_OPERATIONS_PROPERTY_TRUTHINESS)
            }

            return if (p0 != null) {
                if (this@LiquiprimeConnection.isAutoCommitEnabled) {
                    if (haveExecutedStatement) {
                        this@LiquiprimeConnection.sqlWriter.println()
                    }

                    this@LiquiprimeConnection.sqlWriter.print(p0)
                    this@LiquiprimeConnection.sqlWriter.flush()
                } else {
                    val statementPrefix = if (haveExecutedStatement) {
                       System.lineSeparator()
                    } else {
                        ""
                    }

                    this@LiquiprimeConnection.statementSqlQueue.addLast("$statementPrefix$p0")
                }

                haveExecutedStatement = true

                true
            } else {
                false
            }
        }

        override fun execute(p0: String?, p1: Int): Boolean {
            TODO("Not yet implemented")
        }

        override fun execute(p0: String?, p1: IntArray?): Boolean {
            TODO("Not yet implemented")
        }

        override fun execute(p0: String?, p1: Array<out String>?): Boolean {
            TODO("Not yet implemented")
        }

        override fun getResultSet(): ResultSet {
            TODO("Not yet implemented")
        }

        override fun getUpdateCount(): Int {
            TODO("Not yet implemented")
        }

        override fun getMoreResults(): Boolean {
            TODO("Not yet implemented")
        }

        override fun getMoreResults(p0: Int): Boolean {
            TODO("Not yet implemented")
        }

        override fun setFetchDirection(p0: Int) {
            TODO("Not yet implemented")
        }

        override fun getFetchDirection(): Int {
            TODO("Not yet implemented")
        }

        override fun setFetchSize(p0: Int) {
            TODO("Not yet implemented")
        }

        override fun getFetchSize(): Int {
            TODO("Not yet implemented")
        }

        override fun getResultSetConcurrency(): Int {
            TODO("Not yet implemented")
        }

        override fun getResultSetType(): Int {
            TODO("Not yet implemented")
        }

        override fun addBatch(p0: String?) {
            TODO("Not yet implemented")
        }

        override fun clearBatch() {
            TODO("Not yet implemented")
        }

        override fun executeBatch(): IntArray {
            TODO("Not yet implemented")
        }

        override fun getConnection(): Connection {
            TODO("Not yet implemented")
        }

        override fun getGeneratedKeys(): ResultSet {
            TODO("Not yet implemented")
        }

        override fun getResultSetHoldability(): Int {
            TODO("Not yet implemented")
        }

        override fun isClosed(): Boolean {
            TODO("Not yet implemented")
        }

        override fun setPoolable(p0: Boolean) {
            TODO("Not yet implemented")
        }

        override fun isPoolable(): Boolean {
            TODO("Not yet implemented")
        }

        override fun closeOnCompletion() {
            TODO("Not yet implemented")
        }

        override fun isCloseOnCompletion(): Boolean {
            TODO("Not yet implemented")
        }

    }

    override fun <T : Any?> unwrap(p0: Class<T>?): T {
        TODO("Not yet implemented")
    }

    override fun isWrapperFor(p0: Class<*>?): Boolean {
        TODO("Not yet implemented")
    }

    override fun close() {
        sqlWriter.close()
    }

    override fun createStatement(): Statement {
        return LiquiprimeConnectionStatement()
    }

    override fun createStatement(p0: Int, p1: Int): Statement {
        TODO("Not yet implemented")
    }

    override fun createStatement(p0: Int, p1: Int, p2: Int): Statement {
        TODO("Not yet implemented")
    }

    override fun prepareStatement(p0: String?): PreparedStatement {
        TODO("Not yet implemented")
    }

    override fun prepareStatement(p0: String?, p1: Int, p2: Int): PreparedStatement {
        TODO("Not yet implemented")
    }

    override fun prepareStatement(p0: String?, p1: Int, p2: Int, p3: Int): PreparedStatement {
        TODO("Not yet implemented")
    }

    override fun prepareStatement(p0: String?, p1: Int): PreparedStatement {
        TODO("Not yet implemented")
    }

    override fun prepareStatement(p0: String?, p1: IntArray?): PreparedStatement {
        TODO("Not yet implemented")
    }

    override fun prepareStatement(p0: String?, p1: Array<out String>?): PreparedStatement {
        TODO("Not yet implemented")
    }

    override fun prepareCall(p0: String?): CallableStatement {
        TODO("Not yet implemented")
    }

    override fun prepareCall(p0: String?, p1: Int, p2: Int): CallableStatement {
        TODO("Not yet implemented")
    }

    override fun prepareCall(p0: String?, p1: Int, p2: Int, p3: Int): CallableStatement {
        TODO("Not yet implemented")
    }

    override fun nativeSQL(p0: String?): String {
        TODO("Not yet implemented")
    }

    override fun setAutoCommit(p0: Boolean) {
        isAutoCommitEnabled = p0
    }

    override fun getAutoCommit(): Boolean {
        return isAutoCommitEnabled
    }

    override fun commit() {
        if (connectionProperties.doThrowExceptionsForAllOperations) {
            throw SQLException(ExceptionMessages.DO_THROW_EXCEPTIONS_FOR_ALL_OPERATIONS_PROPERTY_TRUTHINESS)
        }

        while(statementSqlQueue.isNotEmpty()) {
            sqlWriter.print(statementSqlQueue.removeFirst())
        }

        sqlWriter.flush()
    }

    override fun rollback() {

    }

    override fun rollback(p0: Savepoint?) {
        TODO("Not yet implemented")
    }

    override fun isClosed(): Boolean {
        TODO("Not yet implemented")
    }

    override fun getMetaData(): DatabaseMetaData {
        TODO("Not yet implemented")
    }

    override fun setReadOnly(p0: Boolean) {
        TODO("Not yet implemented")
    }

    override fun isReadOnly(): Boolean {
        TODO("Not yet implemented")
    }

    override fun setCatalog(p0: String?) {
        TODO("Not yet implemented")
    }

    override fun getCatalog(): String {
        TODO("Not yet implemented")
    }

    override fun setTransactionIsolation(p0: Int) {
        TODO("Not yet implemented")
    }

    override fun getTransactionIsolation(): Int {
        TODO("Not yet implemented")
    }

    override fun getWarnings(): SQLWarning {
        TODO("Not yet implemented")
    }

    override fun clearWarnings() {
        TODO("Not yet implemented")
    }

    override fun getTypeMap(): MutableMap<String, Class<*>> {
        TODO("Not yet implemented")
    }

    override fun setTypeMap(p0: MutableMap<String, Class<*>>?) {
        TODO("Not yet implemented")
    }

    override fun setHoldability(p0: Int) {
        TODO("Not yet implemented")
    }

    override fun getHoldability(): Int {
        TODO("Not yet implemented")
    }

    override fun setSavepoint(): Savepoint {
        TODO("Not yet implemented")
    }

    override fun setSavepoint(p0: String?): Savepoint {
        TODO("Not yet implemented")
    }

    override fun releaseSavepoint(p0: Savepoint?) {
        TODO("Not yet implemented")
    }

    override fun createClob(): Clob {
        TODO("Not yet implemented")
    }

    override fun createBlob(): Blob {
        TODO("Not yet implemented")
    }

    override fun createNClob(): NClob {
        TODO("Not yet implemented")
    }

    override fun createSQLXML(): SQLXML {
        TODO("Not yet implemented")
    }

    override fun isValid(p0: Int): Boolean {
        TODO("Not yet implemented")
    }

    override fun setClientInfo(p0: String?, p1: String?) {
        TODO("Not yet implemented")
    }

    override fun setClientInfo(p0: Properties?) {
        TODO("Not yet implemented")
    }

    override fun getClientInfo(p0: String?): String {
        TODO("Not yet implemented")
    }

    override fun getClientInfo(): Properties {
        TODO("Not yet implemented")
    }

    override fun createArrayOf(p0: String?, p1: Array<out Any>?): java.sql.Array {
        TODO("Not yet implemented")
    }

    override fun createStruct(p0: String?, p1: Array<out Any>?): Struct {
        TODO("Not yet implemented")
    }

    override fun setSchema(p0: String?) {
        TODO("Not yet implemented")
    }

    override fun getSchema(): String {
        TODO("Not yet implemented")
    }

    override fun abort(p0: Executor?) {
        TODO("Not yet implemented")
    }

    override fun setNetworkTimeout(p0: Executor?, p1: Int) {
        TODO("Not yet implemented")
    }

    override fun getNetworkTimeout(): Int {
        TODO("Not yet implemented")
    }
}
