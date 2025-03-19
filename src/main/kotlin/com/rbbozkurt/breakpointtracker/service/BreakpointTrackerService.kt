package com.rbbozkurt.breakpointtracker.service

import com.intellij.openapi.Disposable
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.util.messages.MessageBusConnection
import com.intellij.xdebugger.breakpoints.XBreakpointListener
import com.rbbozkurt.breakpointtracker.util.BreakpointUpdateNotifier
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

/**
 * Service responsible for tracking breakpoints within a project.
 *
 * This service listens for breakpoint changes and notifies subscribed components.
 *
 * @property project The associated IntelliJ project.
 */
@Service(Service.Level.PROJECT)
class BreakpointTrackerService(private val project: Project) : Disposable {

    /** Listener for breakpoint events. */
    private var breakpointListener: BreakpointListener? = null

    /** Coroutine scope for managing asynchronous operations. */
    private val coroutineScope = CoroutineScope(Dispatchers.Default)

    /** Message bus connection for subscribing to breakpoint events. */
    private var connection: MessageBusConnection? = null

    /**
     * Starts the breakpoint tracking service.
     * Initializes the breakpoint listener and subscribes to breakpoint events.
     */
    fun start() {
        breakpointListener = BreakpointListener(project).also { listener ->
            connection = project.messageBus.connect().apply {
                subscribe(XBreakpointListener.TOPIC, listener)
            }
            coroutineScope.launch {
                listener.breakpointsFlow.collectLatest { breakpoints ->
                    project.messageBus.syncPublisher(BreakpointUpdateNotifier.TOPIC)
                        .onBreakpointsUpdated(breakpoints)
                }
            }
        }
    }

    /**
     * Cleans up resources when the service is disposed.
     * Cancels coroutines and disconnects the message bus connection.
     */
    override fun dispose() {
        coroutineScope.cancel()
        connection?.disconnect()
    }

    companion object {
        /**
         * Retrieves the instance of [BreakpointTrackerService] for the given project.
         *
         * @param project The project for which to retrieve the service.
         * @return The [BreakpointTrackerService] instance.
         */
        fun getInstance(project: Project): BreakpointTrackerService = project.service()
    }
}
