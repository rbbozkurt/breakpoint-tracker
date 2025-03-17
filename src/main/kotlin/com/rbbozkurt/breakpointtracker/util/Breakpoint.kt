package com.rbbozkurt.breakpointtracker.util
import com.intellij.util.messages.Topic
import java.nio.file.Paths
import com.intellij.xdebugger.breakpoints.XBreakpoint


data class Breakpoint(
    val filePath: String,
    val lineNumber: Int,
    val isEnabled: Boolean,
    var isActive: Boolean = false  // Dynamically updated
)

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



fun Breakpoint.matches(breakpoint: XBreakpoint<*>): Boolean {
    return this.filePath == breakpoint.sourcePosition?.file?.name &&
            this.lineNumber == breakpoint.sourcePosition?.line?.plus(1)
}


interface BreakpointUpdateNotifier {
    fun onBreakpointsUpdated(breakpointUtils: List<Breakpoint>)
    companion object {
        val TOPIC = Topic.create("Breakpoints Updated", BreakpointUpdateNotifier::class.java)
    }
}
