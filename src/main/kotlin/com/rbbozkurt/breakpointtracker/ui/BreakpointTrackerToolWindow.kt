package com.rbbozkurt.breakpointtracker.ui

import com.google.gson.Gson
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.ui.content.ContentFactory
import com.intellij.ui.jcef.JBCefBrowser
import com.intellij.ui.jcef.executeJavaScript
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
import javax.swing.JComponent

class BreakpointTrackerToolWindow(project: Project, toolWindow: ToolWindow) {

    private val logger = Logger.getInstance(BreakpointTrackerToolWindow::class.java)

    private val isExtern = System.getenv("UI_ENV") == "extern"
    private val frontendUrl = System.getenv("UI_URL") ?: "http://localhost:5173"
    private val browser = JBCefBrowser()
    private val gson = Gson()

    private val connection: MessageBusConnection = project.messageBus.connect()
    private val coroutineScope = CoroutineScope(Dispatchers.Main)

    // 🔥 UI State is now managed inside ToolWindow
    private val _uiStateFlow = MutableStateFlow(JcefBrowserUiState(isLoading = true))
    val uiStateFlow = _uiStateFlow.asStateFlow()

    init {
        logger.info("Initializing BreakpointTrackerToolWindow...")
        logger.info("UI_ENV = $isExtern, UI_URL = $frontendUrl")

        if (isExtern) {
            logger.info("Loading external frontend from: $frontendUrl")
            browser.loadURL(frontendUrl)
        } else {
            logger.info("Using built-in renderer.")
        }
        updateUI(_uiStateFlow.value)

        val panel = JPanel().apply {
            layout = BoxLayout(this, BoxLayout.Y_AXIS)
            add(browser.component)
        }

        // Subscribe to MessageBus for breakpoint updates
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
                updateUI(uiState)
            }
        }

        // Add UI to ToolWindow
        val contentFactory = ContentFactory.getInstance()
        val content = contentFactory.createContent(panel, "", false)
        toolWindow.contentManager.addContent(content)
    }

    val component: JComponent
        get() = browser.component

    /** Updates UI based on current state */
    private fun updateUI(uiState: JcefBrowserUiState) {
        if (isExtern) {
            sendToFrontend(uiState)
        } else {
            updateInternalRenderer(uiState)
        }
    }

    /** Sends UI state to external React frontend */
    private fun sendToFrontend(uiState: JcefBrowserUiState) {
        val jsonState = gson.toJson(uiState)
        logger.info("Sending UI state to frontend: $jsonState")

        coroutineScope.launch {
            browser.executeJavaScript("""
            if (window.updateBreakpoints) {
                window.updateBreakpoints($jsonState);
            }
        """.trimIndent())
        }
    }


    /** Updates built-in HTML Renderer */
    private fun updateInternalRenderer(uiState: JcefBrowserUiState) {
        val html = BreakpointHtmlRenderer.render(uiState)
        browser.loadHTML(html)

    }
}
