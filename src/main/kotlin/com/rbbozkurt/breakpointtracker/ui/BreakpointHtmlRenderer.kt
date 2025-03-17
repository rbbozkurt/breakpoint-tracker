package com.rbbozkurt.breakpointtracker.ui

import com.rbbozkurt.breakpointtracker.util.Breakpoint

object BreakpointHtmlRenderer {

    private const val STYLE = """
        <style>
            @import url('https://fonts.googleapis.com/css2?family=JetBrains+Mono&display=swap');
            body {
                font-family: 'JetBrains Mono', monospace;
                background-color: #2b2b2b;
                color: #a9b7c6;
                margin: 0;
                padding: 20px;
            }
            h2 {
                font-weight: normal;
                color: #ffc66d;
                margin-bottom: 20px;
            }
            table {
                width: 100%;
                border-collapse: collapse;
                margin-bottom: 20px;
            }
            th, td {
                padding: 8px;
                text-align: left;
            }
            thead th {
                border-bottom: 2px solid #515151;
                color: #cc7832; /* Keyword color */
            }
            tbody tr:nth-child(even) {
                background-color: #323232;
            }
            tbody tr:nth-child(odd) {
                background-color: #2b2b2b;
            }
            /* Syntax highlighting */
            .keyword { color: #cc7832; }
            .string { color: #6a8759; }
            .comment { color: #808080; font-style: italic; }
            .number { color: #6897bb; }
            .identifier { color: #a9b7c6; }
        </style>
    """

    fun getLoadingHtml(): String {
        return """
            <html>
            <head>
                $STYLE
            </head>
            <body>
                <h2>Collecting breakpoints...</h2>
            </body>
            </html>
        """.trimIndent()
    }

    fun getNoBreakpointsHtml(): String {
        return """
            <html>
            <head>
                $STYLE
            </head>
            <body>
                <h2>No breakpoints set.</h2>
            </body>
            </html>
        """.trimIndent()
    }

    fun getHtml(breakpoints: List<Breakpoint>): String {
        val breakpointsHtml = breakpoints.joinToString("") { bp ->
            "<tr><td class='identifier'>${bp.filePath}</td><td class='number'>${bp.lineNumber}</td></tr>"
        }

        return """
            <html>
            <head>
                $STYLE
            </head>
            <body>
                <h2>Current Breakpoints (${breakpoints.size})</h2>
                <table>
                    <thead>
                        <tr><th>File</th><th>Line</th></tr>
                    </thead>
                    <tbody>
                        $breakpointsHtml
                    </tbody>
                </table>
            </body>
            </html>
        """.trimIndent()
    }
}
