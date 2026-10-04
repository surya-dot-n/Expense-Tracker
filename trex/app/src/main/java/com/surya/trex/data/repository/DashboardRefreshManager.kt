package com.surya.trex.data.repository

import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

/**
 * Sends a dashboard refresh event whenever transaction data changes.
 *
 * This does not store transaction data.
 * If HomeScreen is not visible when an event is emitted,
 * HomeScreen performs a fresh load when it is opened again.
 */
object DashboardRefreshManager {

    private val _refreshEvents =
        MutableSharedFlow<Unit>(
            replay = 0,
            extraBufferCapacity = 64,
            onBufferOverflow = BufferOverflow.DROP_OLDEST
        )

    val refreshEvents: SharedFlow<Unit> =
        _refreshEvents.asSharedFlow()

    fun refresh() {
        _refreshEvents.tryEmit(Unit)
    }
}