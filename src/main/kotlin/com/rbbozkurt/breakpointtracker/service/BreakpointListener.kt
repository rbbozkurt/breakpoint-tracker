package com.rbbozkurt.breakpointtracker.service

import com.intellij.openapi.project.Project
import com.intellij.xdebugger.XDebuggerManager
import com.intellij.xdebugger.breakpoints.XBreakpoint
import com.intellij.xdebugger.breakpoints.XBreakpointListener
import com.rbbozkurt.breakpointtracker.util.Breakpoint
import com.rbbozkurt.breakpointtracker.util.matches
import com.rbbozkurt.breakpointtracker.util.toCustomBreakpoint
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class BreakpointListener(private val project: Project) : XBreakpointListener<XBreakpoint<*>> {

    private val _breakpointsFlow = MutableStateFlow<List<Breakpoint>>(emptyList())
    val breakpointsFlow = _breakpointsFlow.asStateFlow()

    init {
        // Retrieve and set existing breakpoints on initialization
        val breakpointManager = XDebuggerManager.getInstance(project).breakpointManager
        val existingBreakpoints = breakpointManager.allBreakpoints.mapNotNull { it.toCustomBreakpoint() }
        _breakpointsFlow.value = existingBreakpoints.subList(1, existingBreakpoints.size) //drop first elem.
    }

    override fun breakpointAdded(breakpoint: XBreakpoint<*>) {
        breakpoint.toCustomBreakpoint().let { newBreakpoint ->
            updateBreakpoints { it + newBreakpoint }
        }
    }

    override fun breakpointRemoved(breakpoint: XBreakpoint<*>) {
        breakpoint.toCustomBreakpoint().let { removedBreakpoint ->
            updateBreakpoints { currentBreakpoints ->
                currentBreakpoints.filterNot { it == removedBreakpoint }
            }
        }
    }

    override fun breakpointChanged(breakpoint: XBreakpoint<*>) {
        breakpoint.toCustomBreakpoint().let { updatedBreakpoint ->
            updateBreakpoints { currentBreakpoints ->
                currentBreakpoints.map { if (it.matches(breakpoint)) updatedBreakpoint else it }
            }
        }
    }

    private fun updateBreakpoints(update: (List<Breakpoint>) -> List<Breakpoint>) {
        _breakpointsFlow.value = update(_breakpointsFlow.value)
    }
}
