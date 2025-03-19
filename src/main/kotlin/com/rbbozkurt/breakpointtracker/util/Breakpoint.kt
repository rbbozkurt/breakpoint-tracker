package com.rbbozkurt.breakpointtracker.util

import com.intellij.util.messages.Topic
import java.nio.file.Paths
import com.intellij.xdebugger.breakpoints.XBreakpoint

/**
 * Represents a breakpoint with relevant metadata.
 *
 * @property filePath The relative file path where the breakpoint is set.
 * @property lineNumber The line number where the breakpoint is set.
 * @property isEnabled Indicates whether the breakpoint is enabled.
 * @property isActive Indicates whether the breakpoint is currently active (dynamically updated).
 */
data class Breakpoint(
    val filePath: String,
    val lineNumber: Int,
    val isEnabled: Boolean,
    var isActive: Boolean = false  // Dynamically updated
)

/**
 * Converts an [XBreakpoint] to a [Breakpoint] instance.
 *
 * @receiver The XBreakpoint instance to convert.
 * @return A [Breakpoint] object containing relevant metadata.
 */
fun XBreakpoint<*>.toCustomBreakpoint(): Breakpoint {
    val pos = sourcePosition ?: return Breakpoint("Unknown", -1, isEnabled = false)
    val file = pos.file
    val filePath = Paths.get(file.path)
    val srcIndex = filePath.iterator().asSequence().indexOfFirst { it.toString() == "src" }
    val relativePath = if (srcIndex != -1 && srcIndex < filePath.nameCount - 1) {
        filePath.subpath(srcIndex + 1, filePath.nameCount).toString()
    } else {
        file.name
    }
    return Breakpoint(
        filePath = relativePath,
        lineNumber = pos.line + 1,
        isEnabled = isEnabled
    )
}

/**
 * Checks if a [Breakpoint] matches an [XBreakpoint].
 *
 * @receiver The current [Breakpoint] instance.
 * @param breakpoint The [XBreakpoint] to compare against.
 * @return `true` if the breakpoints match, otherwise `false`.
 */
fun Breakpoint.matches(breakpoint: XBreakpoint<*>): Boolean {
    return this.filePath == breakpoint.sourcePosition?.file?.name &&
            this.lineNumber == breakpoint.sourcePosition?.line?.plus(1)
}

/**
 * Interface for notifying subscribers when breakpoints are updated.
 */
interface BreakpointUpdateNotifier {
    /**
     * Called when the list of breakpoints is updated.
     *
     * @param breakpointUtils The updated list of breakpoints.
     */
    fun onBreakpointsUpdated(breakpointUtils: List<Breakpoint>)

    companion object {
        /** Topic for broadcasting breakpoint update events. */
        val TOPIC = Topic.create("Breakpoints Updated", BreakpointUpdateNotifier::class.java)
    }
}
