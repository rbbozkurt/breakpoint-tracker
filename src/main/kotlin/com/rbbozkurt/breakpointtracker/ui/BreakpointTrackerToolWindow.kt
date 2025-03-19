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
import org.cef.browser.CefBrowser
import org.cef.browser.CefFrame
import org.cef.handler.CefLoadHandler
import org.cef.network.CefRequest
import javax.swing.JPanel
import javax.swing.BoxLayout
import javax.swing.JComponent

/**
 * Tool window responsible for displaying and tracking breakpoints using JCEF in a JetBrains IDE.
 *
 * This class manages the JCEF browser, subscribes to breakpoint updates, and renders the UI accordingly.
 *
 * @property project The associated IntelliJ project.
 * @property toolWindow The tool window where the UI is displayed.
 */
class BreakpointTrackerToolWindow(project: Project, toolWindow: ToolWindow) {

    private val logger = Logger.getInstance(BreakpointTrackerToolWindow::class.java)
    private val frontendPort = System.getenv("UI_PORT") ?: "5173"
    private val frontendUrl = System.getenv("UI_URL") ?: "http://localhost:$frontendPort"
    private val isExtern = System.getenv("UI_ENV") == "extern"

    private val browser = JBCefBrowser()
    private val gson = Gson()
    private val connection: MessageBusConnection = project.messageBus.connect()
    private val coroutineScope = CoroutineScope(Dispatchers.Main)

    private var isBrowserReady = false

    /**
     * UI state flow to manage the rendering of breakpoints.
     */
    private val _uiStateFlow = MutableStateFlow(JcefBrowserUiState(isLoading = true))
    val uiStateFlow = _uiStateFlow.asStateFlow()

    init {
        logger.info("Initializing BreakpointTrackerToolWindow...")
        logger.info("UI_ENV = $isExtern, UI_URL = $frontendUrl")

        if (isExtern) {
            browser.loadURL(frontendUrl)
            addBrowserLoadListener()
        } else {
            logger.info("Using built-in renderer.")
        }

        setupUI(toolWindow)
        subscribeToBreakpointUpdates()
        startRendering()
    }

    /**
     * Adds a load listener to track when JCEF is fully loaded.
     */
    private fun addBrowserLoadListener() {
        browser.jbCefClient.addLoadHandler(object : CefLoadHandler {
            override fun onLoadEnd(browser: CefBrowser?, frame: CefFrame?, httpStatusCode: Int) {
                logger.info("JCEF Browser fully loaded (Status: $httpStatusCode)")
                isBrowserReady = true
                sendToFrontend(uiStateFlow.value)
            }

            override fun onLoadError(browser: CefBrowser?, frame: CefFrame?, errorCode: CefLoadHandler.ErrorCode?, errorText: String?, failedUrl: String?) {
                logger.error("JCEF failed: $errorText ($failedUrl)")
            }

            override fun onLoadingStateChange(browser: CefBrowser?, isLoading: Boolean, canGoBack: Boolean, canGoForward: Boolean) {
                // Not yet implemented
            }

            override fun onLoadStart(browser: CefBrowser?, frame: CefFrame?, transitionType: CefRequest.TransitionType) {
                isBrowserReady = false
            }
        }, browser.cefBrowser)
    }

    /**
     * Sets up the UI layout within the tool window.
     */
    private fun setupUI(toolWindow: ToolWindow) {
        val panel = JPanel().apply {
            layout = BoxLayout(this, BoxLayout.Y_AXIS)
            add(browser.component)
        }
        val content = ContentFactory.getInstance().createContent(panel, "", false)
        toolWindow.contentManager.addContent(content)
    }

    /**
     * Subscribes to breakpoint updates and updates the UI state accordingly.
     */
    private fun subscribeToBreakpointUpdates() {
        connection.subscribe(BreakpointUpdateNotifier.TOPIC, object : BreakpointUpdateNotifier {
            override fun onBreakpointsUpdated(breakpoints: List<Breakpoint>) {
                val newState = JcefBrowserUiState(isLoading = false, breakpoints = breakpoints)
                if (newState != uiStateFlow.value) {
                    _uiStateFlow.value = newState
                }
            }
        })
    }

    /**
     * Starts the rendering process based on UI state changes.
     */
    private fun startRendering(){
        coroutineScope.launch {
            uiStateFlow.collectLatest { uiState ->
                when(isExtern){
                    true -> sendToFrontend(uiState)
                    false -> browser.loadHTML(BreakpointHtmlRenderer.render(uiState))
                }
            }
        }
    }

    /**
     * Sends the UI state data to the frontend.
     *
     * @param uiState The UI state to be sent.
     */
    private fun sendToFrontend(uiState: JcefBrowserUiState) {
        val jsonState = gson.toJson(uiState)
        coroutineScope.launch(Dispatchers.IO) {
            if (isBrowserReady) {
                browser.executeJavaScript("""
                    window.updateBreakpoints?.($jsonState)
                """.trimIndent())
            }
        }
    }
}
