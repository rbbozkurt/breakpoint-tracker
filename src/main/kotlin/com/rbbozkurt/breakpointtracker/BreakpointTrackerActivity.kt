package com.rbbozkurt.breakpointtracker

import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.ProjectActivity
import com.rbbozkurt.breakpointtracker.service.BreakpointTrackerService
import java.io.File
import java.io.IOException
import com.intellij.openapi.application.PathManager


class BreakpointTrackerStartupActivity : ProjectActivity {
    override suspend fun execute(project: Project) {
        BreakpointTrackerService.getInstance(project).start()
    }
}
