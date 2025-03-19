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

/**
 * Listener for tracking breakpoint changes in an IntelliJ-based IDE.
 * This class listens to added, removed, and changed breakpoints and updates a state flow accordingly.
 *
 * @property project The associated IntelliJ project.
 */
class BreakpointListener(private val project: Project) : XBreakpointListener<XBreakpoint<*>> {

    /** Mutable state flow holding the list of active breakpoints. */
    private val _breakpointsFlow = MutableStateFlow<List<Breakpoint>>(emptyList())

    /** Publicly exposed immutable state flow for observing breakpoint changes. */
    val breakpointsFlow = _breakpointsFlow.asStateFlow()

    init {
        // Retrieve and set existing breakpoints on initialization
        val breakpointManager = XDebuggerManager.getInstance(project).breakpointManager
        val existingBreakpoints = breakpointManager.allBreakpoints.mapNotNull { it.toCustomBreakpoint() }
        _breakpointsFlow.value = existingBreakpoints.subList(1, existingBreakpoints.size) // Drop first element.
    }

    /**
     * Called when a new breakpoint is added.
     *
     * @param breakpoint The newly added breakpoint.
     */
    override fun breakpointAdded(breakpoint: XBreakpoint<*>) {
        breakpoint.toCustomBreakpoint().let { newBreakpoint ->
            updateBreakpoints { it + newBreakpoint }
        }
    }

    /**
     * Called when a breakpoint is removed.
     *
     * @param breakpoint The breakpoint that was removed.
     */
    override fun breakpointRemoved(breakpoint: XBreakpoint<*>) {
        breakpoint.toCustomBreakpoint().let { removedBreakpoint ->
            updateBreakpoints { currentBreakpoints ->
                currentBreakpoints.filterNot { it == removedBreakpoint }
            }
        }
    }

    /**
     * Called when a breakpoint is modified.
     *
     * @param breakpoint The updated breakpoint.
     */
    override fun breakpointChanged(breakpoint: XBreakpoint<*>) {
        breakpoint.toCustomBreakpoint().let { updatedBreakpoint ->
            updateBreakpoints { currentBreakpoints ->
                currentBreakpoints.map { if (it.matches(breakpoint)) updatedBreakpoint else it }
            }
        }
    }

    /**
     * Updates the list of breakpoints in the state flow.
     *
     * @param update A function that modifies the current list of breakpoints.
     */
    private fun updateBreakpoints(update: (List<Breakpoint>) -> List<Breakpoint>) {
        _breakpointsFlow.value = update(_breakpointsFlow.value)
    }
}
