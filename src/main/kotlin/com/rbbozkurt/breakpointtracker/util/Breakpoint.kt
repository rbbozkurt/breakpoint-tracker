package com.rbbozkurt.breakpointtracker.util
import com.intellij.util.messages.Topic
import com.intellij.xdebugger.breakpoints.XBreakpoint

data class Breakpoint(
    val fileName: String,
    val lineNumber: Int,
    val isEnabled: Boolean,
    var isActive: Boolean = false  // Dynamically updated
)

fun XBreakpoint<*>.toCustomBreakpoint(): Breakpoint {
    val pos = sourcePosition ?: return Breakpoint("Unknown", -1, isEnabled = false)
    return Breakpoint(
        fileName = pos.file.name,
        lineNumber = pos.line + 1,
        isEnabled = isEnabled
    )
}



fun Breakpoint.matches(breakpoint: XBreakpoint<*>): Boolean {
    return this.fileName == breakpoint.sourcePosition?.file?.name &&
            this.lineNumber == breakpoint.sourcePosition?.line?.plus(1)
}


interface BreakpointUpdateNotifier {
    fun onBreakpointsUpdated(breakpointUtils: List<Breakpoint>)
    companion object {
        val TOPIC = Topic.create("Breakpoints Updated", BreakpointUpdateNotifier::class.java)
    }
}
