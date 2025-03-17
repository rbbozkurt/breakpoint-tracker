package com.rbbozkurt.breakpointtracker.ui

import com.rbbozkurt.breakpointtracker.util.Breakpoint

data class JcefBrowserUiState (
    val isLoading: Boolean = true,
    val breakpoints: List<Breakpoint> = emptyList(),
    val errorMessage: String? = null
)
