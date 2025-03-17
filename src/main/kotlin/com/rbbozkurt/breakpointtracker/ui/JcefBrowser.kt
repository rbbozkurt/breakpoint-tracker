package com.rbbozkurt.breakpointtracker.ui

import com.intellij.ui.jcef.JBCefBrowser
import com.rbbozkurt.breakpointtracker.util.Breakpoint
import javax.swing.JComponent

class JcefBrowser {
    private val browser = JBCefBrowser()

    init {
        showLoading()  // Initially show loading state
    }

    val component: JComponent
        get() = browser.component

    fun showLoading() {
        browser.loadHTML(BreakpointHtmlRenderer.getLoadingHtml())
    }

    fun showNoBreakpoints() {
        browser.loadHTML(BreakpointHtmlRenderer.getNoBreakpointsHtml())
    }

    fun updateBreakpoints(breakpoints: List<Breakpoint>) {
        browser.loadHTML(BreakpointHtmlRenderer.getHtml(breakpoints))
    }
}
