import { useState, useEffect } from "react";
import "./App.css";

function App() {
    const [uiState, setUiState] = useState({
        isLoading: true,
        breakpoints: [],
        errorMessage: null
    });

    useEffect(() => {
        // Expose function for JetBrains Plugin to send full UI state
        window.updateBreakpoints = (data) => {
            console.log("Received UI State:", data);

            // Ensure valid data structure
            if (!data || typeof data !== "object") {
                console.warn("Invalid data received:", data);
                return;
            }

            // Update state with the full UI state
            setUiState({
                isLoading: data.isLoading ?? true,
                breakpoints: Array.isArray(data.breakpoints) ? data.breakpoints : [],
                errorMessage: data.errorMessage || null
            });
        };
    }, []);

    return (
        <div id="root">
            <h1>Breakpoint Tracker</h1>

            {uiState.errorMessage ? (
                <p className="error-text">⚠️ {uiState.errorMessage}</p>
            ) : uiState.isLoading ? (
                <p className="loading-text">Loading breakpoints...</p>
            ) : uiState.breakpoints.length === 0 ? (
                <p className="no-breakpoints">No breakpoints set.</p>
            ) : (
                <>
                    <p className="total-breakpoints">
                        Total Breakpoints: <strong>{uiState.breakpoints.length}</strong>
                    </p>
                    <table>
                        <thead>
                        <tr>
                            <th>File</th>
                            <th>Line</th>
                        </tr>
                        </thead>
                        <tbody>
                        {uiState.breakpoints.map((bp, index) => (
                            <tr key={index}>
                                <td>{bp.filePath}</td>
                                <td>{bp.lineNumber}</td>
                            </tr>
                        ))}
                        </tbody>
                    </table>
                </>
            )}
        </div>
    );
}

export default App;
