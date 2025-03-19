#!/bin/bash

# Define paths
FRONTEND_DIR="$(pwd)/frontend"
PLUGIN_DIR="$(pwd)"
DEFAULT_PORT=5173  # Default Vite port

# Function to check if a command is available
check_command() {
    if ! command -v "$1" &> /dev/null; then
        echo "❌ Error: $1 is not installed. Please install it and try again."
        exit 1
    fi
}

# Check required dependencies
check_command "lsof"  # Required for checking if a port is in use
check_command "npm"   # Required for frontend
check_command "java"  # Required for Gradle
check_command "bash"  # Ensures shell compatibility

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

# Prompt user for environment mode
while true; do
    echo "Select UI mode:"
    echo "1. intern: Uses JCEF built-in display (No frontend server needed)"
    echo "2. extern: Uses external frontend server (Optional)"
    read -p "Enter choice (1 or 2): " UI_MODE

    case $UI_MODE in
        1)
            export UI_ENV="intern"
            echo "✅ Running in intern mode (JCEF built-in renderer will be used)."
            break
            ;;
        2)
            export UI_ENV="extern"
            read -p "Enter frontend port (default: $DEFAULT_PORT): " FRONTEND_PORT
            FRONTEND_PORT=${FRONTEND_PORT:-$DEFAULT_PORT}
            check_port "$FRONTEND_PORT"
            break
            ;;
        *)
            echo "❌ Invalid selection. Please choose 1 (intern) or 2 (extern)."
            ;;
    esac
done

# Function to start the frontend server (if needed)
start_frontend() {
    echo "🚀 Starting frontend server on http://localhost:$FRONTEND_PORT..."

    if [ -d "$FRONTEND_DIR" ]; then
        cd "$FRONTEND_DIR" || exit

        # Ensure dependencies are installed
        if [ ! -d "node_modules" ]; then
            echo "🔄 Installing frontend dependencies..."
            npm install || { echo "❌ Failed to install dependencies."; exit 1; }
        fi

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

    # Ensure Gradle wrapper is executable
    chmod +x ./gradlew

    # Log UI environment and port
    echo "📝 UI_ENV: $UI_ENV"
    echo "📝 UI_PORT: $UI_PORT"

    ./gradlew build || { echo "❌ Build failed. Exiting."; exit 1; }

    echo "🚀 Starting JetBrains Plugin..."
    ./gradlew runIde -DUI_ENV="$UI_ENV" -DUI_PORT="${UI_PORT:-$DEFAULT_PORT}"
}

# Start the plugin
start_plugin
