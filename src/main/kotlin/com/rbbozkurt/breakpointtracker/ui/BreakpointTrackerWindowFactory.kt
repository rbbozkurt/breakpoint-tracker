package com.rbbozkurt.breakpointtracker.ui

import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory

/**
 * Factory class for creating the Breakpoint Tracker tool window in the JetBrains IDE.
 *
 * This factory initializes the [BreakpointTrackerToolWindow] when the tool window is created.
 */
class BreakpointTrackerWindowFactory : ToolWindowFactory {

    /**
     * Creates the content for the Breakpoint Tracker tool window.
     *
     * @param project The current IntelliJ project.
     * @param toolWindow The tool window where the content will be added.
     */
    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        BreakpointTrackerToolWindow(project, toolWindow) // Only initialize
    }
}
