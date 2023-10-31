package com._16minutes.liquiprime.tasks.exceptions

class LiquiprimePrimeDatabasesException(
    message: String,
    cause: Throwable,
    enableSuppression: Boolean = false,
    writableStackTrace: Boolean = false
): RuntimeException(message, cause, enableSuppression, writableStackTrace) {
    companion object {
        const val SETUP_FAILURE_MESSAGE_TEMPLATE =
            "An error occurred while setting up resources to prime a database, as specified by activity '%s'."

        const val EXECUTION_FAILURE_MESSAGE_TEMPLATE =
            "An error occurred while executing the statements in '%s' to prime a database, as specified by activity '%s'."

        fun createMessage(activityName: String, primerFilePath: String?): String {
            return if (primerFilePath == null) {
                String.format(SETUP_FAILURE_MESSAGE_TEMPLATE, activityName)
            } else {
                String.format(EXECUTION_FAILURE_MESSAGE_TEMPLATE, primerFilePath, activityName)
            }
        }
    }
}
