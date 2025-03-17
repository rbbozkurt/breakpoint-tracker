package com.rbbozkurt.breakpointtracker.ui

import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.ui.content.ContentFactory
import com.intellij.util.messages.MessageBusConnection
import com.rbbozkurt.breakpointtracker.util.Breakpoint
import com.rbbozkurt.breakpointtracker.util.BreakpointUpdateNotifier
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.swing.JPanel
import javax.swing.BoxLayout

class BreakpointTrackerToolWindow(project: Project, toolWindow: ToolWindow) {

    private val browser = JcefBrowser()
    private val connection: MessageBusConnection = project.messageBus.connect()
    private val coroutineScope = CoroutineScope(Dispatchers.Main)

    // Internal state tracking
    private val _uiStateFlow = MutableStateFlow(JcefBrowserUiState(isLoading = true))
    val uiStateFlow = _uiStateFlow.asStateFlow()

    init {
        val panel = JPanel().apply {
            layout = BoxLayout(this, BoxLayout.Y_AXIS)
            add(browser.component)
        }

        // Subscribe to MessageBus for breakpoints updates
        connection.subscribe(BreakpointUpdateNotifier.TOPIC, object : BreakpointUpdateNotifier {
            override fun onBreakpointsUpdated(breakpoints: List<Breakpoint>) {
                coroutineScope.launch {
                    _uiStateFlow.value = JcefBrowserUiState(
                        isLoading = false,
                        breakpoints = breakpoints
                    )
                }
            }
        })

        // Collect state updates and update UI
        coroutineScope.launch {
            uiStateFlow.collectLatest { uiState ->
                when {
                    uiState.isLoading -> browser.showLoading()
                    uiState.breakpoints.isEmpty() -> browser.showNoBreakpoints()
                    else -> browser.updateBreakpoints(uiState.breakpoints)
                }
            }
        }

        // Add UI to ToolWindow
        val contentFactory = ContentFactory.getInstance()
        val content = contentFactory.createContent(panel, "", false)
        toolWindow.contentManager.addContent(content)
    }
}
