package com.stonecraft.datastore.logs

import java.lang.RuntimeException

data class ConnectionLog(val databaseName: String, val connectionStatus: ConnectionStatus,
                         val timeInMillis: Long, val exception: Exception?) {
    constructor(databaseName: String, connectionStatus: ConnectionStatus,
                timeInMillis: Long): this(databaseName, connectionStatus, timeInMillis, null)

}

enum class ConnectionStatus {
    ADDED,
    OPENED,
    CREATED,
    UPGRADED,
    ATTEMPT_RECONNECT,
    RECONNECT_FAILED,
    REMOVED,
    CLOSED
}