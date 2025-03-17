package com.rbbozkurt.breakpointtracker.ui

import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory

class BreakpointTrackerWindowFactory : ToolWindowFactory {
    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        BreakpointTrackerToolWindow(project, toolWindow) // Only initialize
    }
}
