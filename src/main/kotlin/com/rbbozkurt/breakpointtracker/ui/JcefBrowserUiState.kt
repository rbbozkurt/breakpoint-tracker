package com.rbbozkurt.breakpointtracker.ui

import com.rbbozkurt.breakpointtracker.util.Breakpoint

/**
 * Represents the UI state for the JCEF browser displaying breakpoints.
 *
 * @property isLoading Indicates whether the UI is currently in a loading state.
 * @property breakpoints The list of currently active breakpoints.
 * @property errorMessage Optional error message to display in case of issues.
 */
data class JcefBrowserUiState (
    val isLoading: Boolean = true,
    val breakpoints: List<Breakpoint> = emptyList(),
    val errorMessage: String? = null
)
