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

@Service(Service.Level.PROJECT)
class BreakpointTrackerService(private val project: Project) : Disposable {

    private var breakpointListener: BreakpointListener? = null
    private val coroutineScope = CoroutineScope(Dispatchers.Default)
    private var connection: MessageBusConnection? = null
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

    override fun dispose() {
        coroutineScope.cancel()
        connection?.disconnect()
    }

    companion object {
        fun getInstance(project: Project): BreakpointTrackerService = project.service()
    }
}
