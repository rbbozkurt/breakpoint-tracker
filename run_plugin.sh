#!/bin/bash

# Define paths
FRONTEND_DIR="$(pwd)/frontend"
PLUGIN_DIR="$(pwd)"
DEFAULT_PORT=5173  # Default Vite port

# Function to check if a port is in use
check_port() {
    local PORT=$1
    local PID
    PID=$(lsof -ti :"$PORT")

    if [ -n "$PID" ]; then
        echo "❌ Port $PORT is already in use. Please choose another port."
        exit 1
    fi
}

# Ask user for environment choice
echo "Select UI mode:"
echo "1. intern: Uses JCEF built-in display (No frontend server needed)"
echo "2. extern: Uses external frontend server (Optional)"
read -p "Enter choice (1 or 2): " UI_MODE

# Set environment based on user input
if [[ "$UI_MODE" == "1" ]]; then
    export UI_ENV="intern"
    echo "✅ Running in intern mode (JCEF built-in renderer will be used)."
elif [[ "$UI_MODE" == "2" ]]; then
    export UI_ENV="extern"
    read -p "Enter frontend port (default: $DEFAULT_PORT): " FRONTEND_PORT
    FRONTEND_PORT=${FRONTEND_PORT:-$DEFAULT_PORT}
    check_port "$FRONTEND_PORT"
else
    echo "❌ Invalid selection. Please choose 1 (intern) or 2 (extern)."
    exit 1
fi

# Function to start the frontend server (if needed)
start_frontend() {
    echo "🚀 Starting frontend server on http://localhost:$FRONTEND_PORT..."

    if [ -d "$FRONTEND_DIR" ]; then
        cd "$FRONTEND_DIR" || exit
        npm run dev -- --port="$FRONTEND_PORT" &
        FRONTEND_PID=$!
        sleep 3  # Allow frontend time to start
        echo "✅ Frontend started with PID: $FRONTEND_PID"
    else
        echo "❌ Frontend directory not found. Falling back to built-in renderer."
        export UI_ENV="intern"
    fi
}

# Start frontend if using extern mode
if [[ "$UI_ENV" == "extern" ]]; then
    start_frontend
    export UI_PORT="$FRONTEND_PORT"
else
    export UI_PORT="N/A (intern mode)"
fi

# Function to build and start the plugin
start_plugin() {
    echo "🚀 Building JetBrains Plugin..."
    cd "$PLUGIN_DIR" || exit

    # Log UI environment and port
    echo "📝 UI_ENV: $UI_ENV"
    echo "📝 UI_PORT: $UI_PORT"

    ./gradlew build || { echo "❌ Build failed. Exiting."; exit 1; }

    echo "🚀 Starting JetBrains Plugin..."
    ./gradlew runIde -DUI_ENV="$UI_ENV" -DUI_PORT="${UI_PORT:-$DEFAULT_PORT}"
}

# Start the plugin
start_plugin
