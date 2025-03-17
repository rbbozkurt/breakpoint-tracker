#!/bin/bash

# Define paths
FRONTEND_DIR="$(pwd)/frontend"
PLUGIN_DIR="$(pwd)"

# Define frontend server settings
FRONTEND_PORT=5173  # Change if needed
FRONTEND_URL="http://localhost:$FRONTEND_PORT"

# Function to check if a port is in use and kill the process
kill_port_if_in_use() {
    local PORT=$1
    local PID
    PID=$(lsof -ti :"$PORT")

    if [ -n "$PID" ]; then
        echo "🔴 Port $PORT is in use. Killing process $PID..."
        kill -9 "$PID"
        sleep 2  # Allow some time for the process to be killed
        echo "✅ Port $PORT is now free."
    fi
}

# Function to start the frontend server
start_frontend() {
    echo "🚀 Starting frontend server on $FRONTEND_URL..."

    if [ -d "$FRONTEND_DIR" ]; then
        cd "$FRONTEND_DIR" || exit

        # Ensure the port is free before starting the frontend
        kill_port_if_in_use "$FRONTEND_PORT"

        export VITE_PORT=$FRONTEND_PORT  # Pass port to Vite
        npm run dev -- --port=$FRONTEND_PORT &
        FRONTEND_PID=$!
        sleep 3  # Give frontend time to start
        echo "✅ Frontend started with PID: $FRONTEND_PID"
    else
        echo "❌ Frontend directory not found. Falling back to built-in renderer."
        return 1
    fi
}

# Function to start the plugin
start_plugin() {
    echo "🚀 Starting JetBrains Plugin..."
    cd "$PLUGIN_DIR" || exit
    export UI_ENV="intern"
    export UI_URL="$FRONTEND_URL"
    ./gradlew runIde
}

# Try starting frontend
start_frontend

# If frontend failed, set UI_ENV to "intern"
if ! ps -p $FRONTEND_PID > /dev/null; then
    echo "⚠️  Using built-in renderer instead of external frontend."
    export UI_ENV="intern"
else
    export UI_ENV="extern"
fi

# Start the plugin
start_plugin
