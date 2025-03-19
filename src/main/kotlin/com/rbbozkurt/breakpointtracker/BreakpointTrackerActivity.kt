package com.rbbozkurt.breakpointtracker

import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.ProjectActivity
import com.rbbozkurt.breakpointtracker.service.BreakpointTrackerService

/**
 * Startup activity that initializes the [BreakpointTrackerService] when the project starts.
 */
class BreakpointTrackerStartupActivity : ProjectActivity {

    /**
     * Executes the startup logic for breakpoint tracking.
     *
     * @param project The IntelliJ project instance.
     */
    override suspend fun execute(project: Project) {
        BreakpointTrackerService.getInstance(project).start()
    }
}
