package com.rbbozkurt.breakpointtracker.ui

import com.intellij.openapi.diagnostic.Logger
import com.intellij.ui.jcef.JBCefBrowser
import com.rbbozkurt.breakpointtracker.util.Breakpoint
import javax.swing.JComponent

class JcefBrowser {
    private val logger = Logger.getInstance(JcefBrowser::class.java)

    private val isExtern = System.getenv("UI_ENV") == "extern"
    private val frontendUrl = System.getenv("UI_URL") ?: "http://localhost:5173"  // Default if not set
    private val browser = JBCefBrowser()

    init {
        logger.info("Initializing JcefBrowser...")
        logger.info("UI_ENV = $isExtern, UI_URL = $frontendUrl")

        if (isExtern) {
            logger.info("Loading external frontend from: $frontendUrl")
            browser.loadURL(frontendUrl)
        } else {
            logger.info("Using built-in renderer.")
            showLoading()
        }
    }

    val component: JComponent
        get() = browser.component

    fun showLoading() {
        if (!isExtern) {
            logger.info("Showing loading screen...")
            browser.loadHTML(BreakpointHtmlRenderer.getLoadingHtml())
        }
    }

    fun showNoBreakpoints() {
        if (!isExtern) {
            logger.info("No breakpoints found. Showing empty state UI.")
            browser.loadHTML(BreakpointHtmlRenderer.getNoBreakpointsHtml())
        }
    }

    fun updateBreakpoints(breakpoints: List<Breakpoint>) {
        if (!isExtern) {
            logger.info("Updating breakpoints. Count: ${breakpoints.size}")
            browser.loadHTML(BreakpointHtmlRenderer.getHtml(breakpoints))
        } else {
            logger.warn("updateBreakpoints() called, but UI_ENV is set to extern. Ignoring update.")
        }
    }
}
