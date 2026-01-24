#!/bin/bash

echo "🛡️  Revival Guardian: Starting Full Code Review..."
echo "==============================================="

# Define paths
SCRIPT_DIR="$(dirname "$0")"
PROJECT_ROOT="$(pwd)"

# 1. Project Validation (Legacy + Resource Checks)
echo ""
echo "🔍 Phase 1: Resource & Integrity Check"
bash "$SCRIPT_DIR/validate_project.sh"

# 2. Python-based Code Analysis
echo ""
echo "🧠 Phase 2: Static Code Analysis (Complexity & Quality)"
python3 "$SCRIPT_DIR/review_code.py" "$PROJECT_ROOT"

# 3. Architecture Analysis
echo ""
echo "buildings Phase 3: Architectural Compliance"
python3 "$SCRIPT_DIR/analyze_architecture.py" "$PROJECT_ROOT"

# 4. Security Scan
echo ""
echo "🔒 Phase 4: Security Audit"
bash "$SCRIPT_DIR/security_scan.sh" "$PROJECT_ROOT"

echo ""
echo "==============================================="
echo "✅ Review Complete."
