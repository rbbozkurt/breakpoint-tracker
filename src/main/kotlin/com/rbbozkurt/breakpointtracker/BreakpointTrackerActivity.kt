package com.rbbozkurt.breakpointtracker

import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.ProjectActivity
import com.rbbozkurt.breakpointtracker.service.BreakpointTrackerService

class BreakpointTrackerStartupActivity : ProjectActivity {

    override suspend fun execute(project: Project) {
        BreakpointTrackerService.getInstance(project).start()
    }
}
