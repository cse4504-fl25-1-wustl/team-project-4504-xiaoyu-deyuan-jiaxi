#!/bin/bash

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Counters
TOTAL_TESTS=0
PASSED_TESTS=0
FAILED_TESTS=0

# Function to print colored messages
print_success() {
    echo -e "${GREEN}[PASS]${NC} $1"
}

print_error() {
    echo -e "${RED}[FAIL]${NC} $1"
}

print_info() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

# Function to process tests based on JSON files in a directory
process_csv_files() {
    local input_dir="$1"
    local client_config="$2"
    
    print_info "Processing tests in: $input_dir"
    print_info "Client config: $client_config"
    
    # Find all expected_output.json files (each represents a test)
    while IFS= read -r -d '' expected_json; do
        ((TOTAL_TESTS++))
        
        # Get the directory containing this JSON file
        local test_dir=$(dirname "$expected_json")
        local test_name=$(echo "$test_dir" | sed "s|^$input_dir/||")
        
        # Find any CSV file in the same directory
        local csv_file=$(find "$test_dir" -maxdepth 1 -type f -name "*.csv" | head -1)
        
        # If no CSV file found, skip this test
        if [[ -z "$csv_file" ]]; then
            print_error "FAILED: $test_name (No CSV file found in directory)"
            ((FAILED_TESTS++))
            continue
        fi
        
        # Convert paths to absolute Windows paths
        # Use realpath if available, otherwise use the cd method
        if command -v realpath &> /dev/null; then
            local abs_csv=$(realpath "$csv_file" 2>/dev/null || echo "$csv_file")
            # Convert to Windows path format (C:/ instead of /c/)
            abs_csv=$(echo "$abs_csv" | sed 's|^/\([a-z]\)/|\U\1:/|')
        else
            local abs_csv=$(cd "$(dirname "$csv_file")" && pwd -W 2>/dev/null || pwd)/$(basename "$csv_file")
        fi
        
        # Use a temporary file in the test directory (easier for Windows paths)
        local temp_output="${test_dir}/.temp_output_$$.json"
        
        # Convert temp output path similarly
        if command -v realpath &> /dev/null; then
            # Get the directory's absolute path first
            local temp_dir=$(realpath "$test_dir" 2>/dev/null || echo "$test_dir")
            temp_dir=$(echo "$temp_dir" | sed 's|^/\([a-z]\)/|\U\1:/|')
            local abs_temp_output="${temp_dir}/.temp_output_$$.json"
        else
            local abs_temp_output=$(cd "$test_dir" && pwd -W 2>/dev/null || pwd)/.temp_output_$$.json
        fi
        
        # Run the Gradle application with appropriate packing mode
        # - crate_packing: use crate-only (only crates)
        # - pallet_packing: use box-only (only boxes and pallets, no crates)
        # - box_packing: use box-only (only boxes and pallets, no crates)
        local gradle_exit_code=0
        local gradle_output=""
        if [[ "$client_config" == "crate-only" ]]; then
            # For crate_packing tests: only use crates
            gradle_output=$(./gradlew run --args="$abs_csv $abs_temp_output crate-only" --quiet --console=plain 2>&1)
            gradle_exit_code=$?
        elif [[ "$client_config" == "box-only" ]]; then
            # For pallet_packing and box_packing tests: only use boxes and pallets (no crates)
            gradle_output=$(./gradlew run --args="$abs_csv $abs_temp_output box-only" --quiet --console=plain 2>&1)
            gradle_exit_code=$?
        else
            # For other tests or default mode: allow all box types
            gradle_output=$(./gradlew run --args="$abs_csv $abs_temp_output" --quiet --console=plain 2>&1)
            gradle_exit_code=$?
        fi
        
        # Check if output was produced
        if [[ ! -f "$temp_output" ]]; then
            echo ""
            echo "================================================"
            echo "Test #$TOTAL_TESTS: $test_name"
            echo "================================================"
            print_error "FAILED: $test_name (No output file produced)"
            print_info "Attempted to create: $temp_output"
            print_info "CSV path used: $abs_csv"
            print_info "Temp output path: $abs_temp_output"
            print_info "Gradle exit code: $gradle_exit_code"
            if [[ $gradle_exit_code -ne 0 ]] || [[ -n "$gradle_output" ]]; then
                echo ""
                echo "Program output:"
                echo "$gradle_output" | grep -i "error\|exception\|failed\|cannot" || echo "$gradle_output" | head -20
            fi
            ((FAILED_TESTS++))
            rm -f "$temp_output"
            continue
        fi
        
        # Read the JSON output into memory
        local actual_json=$(cat "$temp_output")
        
        # Immediately delete the temporary file
        rm -f "$temp_output"
        
        # Compare only the fields that exist in expected_output.json
        # This allows partial JSON validation (only checking key metrics)
        local comparison_result=0
        local error_message=""
        
        # Check for Python (try python3 first, then python for Windows)
        local python_cmd=""
        if command -v python3 &> /dev/null; then
            python_cmd="python3"
        elif command -v python &> /dev/null; then
            python_cmd="python"
        fi
        
        if [[ -n "$python_cmd" ]]; then
            # Use Python to compare JSON in memory (no file writes)
            error_message=$($python_cmd -c "
import json
import sys

try:
    # Read expected JSON from file
    with open('$expected_json', 'r') as f:
        expected = json.load(f)
    
    # Parse actual JSON from captured output
    actual_json_str = '''$actual_json'''
    actual = json.loads(actual_json_str)
    
    # Compare only fields that exist in expected JSON
    mismatches = []
    for key, expected_value in expected.items():
        if key not in actual:
            mismatches.append(f'{key}: missing in actual output')
        elif actual[key] != expected_value:
            mismatches.append(f'{key}: expected {expected_value}, got {actual[key]}')
    
    if mismatches:
        for m in mismatches:
            print(f'  - {m}')
        sys.exit(1)
    else:
        sys.exit(0)
except Exception as e:
    print(f'Error parsing JSON: {e}')
    sys.exit(1)
" 2>&1)
            comparison_result=$?
        else
            # Fallback: if no Python available, report error
            error_message="Python not found - cannot compare JSON"
            comparison_result=1
        fi
        
        if [[ $comparison_result -eq 0 ]]; then
            # Only print success message (one line)
            print_success "PASSED #$TOTAL_TESTS: $test_name"
            ((PASSED_TESTS++))
        else
            # Print detailed error information only for failures
            echo ""
            echo "================================================"
            echo "Test #$TOTAL_TESTS: $test_name"
            echo "================================================"
            print_info "Input: $(basename "$csv_file")"
            print_info "Expected: expected_output.json"
            print_error "FAILED: $test_name (Output mismatch)"
            if [[ -n "$error_message" ]]; then
                echo "$error_message"
            fi
            echo ""
            echo "Expected JSON:"
            cat "$expected_json"
            echo ""
            echo "Actual JSON:"
            echo "$actual_json"
            echo ""
            ((FAILED_TESTS++))
        fi
        
    done < <(find "$input_dir" -type f -name "expected_output.json" -print0)
}

# Main execution
main() {
    echo "========================================"
    echo "  Integration Test Runner"
    echo "========================================"
    echo ""
    
    # Check if we're in the right directory
    if [[ ! -f "gradlew" ]]; then
        print_error "gradlew not found. Please run from project root."
        exit 1
    fi
    
    # Define base test directory
    local BASE_DIR="app/src/test/resources"
    
    # Check if test resources exist
    if [[ ! -d "$BASE_DIR" ]]; then
        print_error "Test resources directory not found: $BASE_DIR"
        exit 1
    fi
    
    # Find all directories ending with "packing" (excluding e2e)
    local packing_dirs=()
    while IFS= read -r dir; do
        # Exclude e2e directory
        if [[ ! "$dir" =~ /e2e$ ]] && [[ ! "$dir" =~ /e2e/ ]]; then
            packing_dirs+=("$dir")
        fi
    done < <(find "$BASE_DIR" -type d -name "*packing" | sort)
    
    if [[ ${#packing_dirs[@]} -eq 0 ]]; then
        print_error "No *packing directories found in $BASE_DIR"
        exit 1
    fi
    
    print_info "Found ${#packing_dirs[@]} packing directories to test"
    
    # Process each packing directory
    for dir in "${packing_dirs[@]}"; do
        local dir_name=$(basename "$dir")
        echo ""
        echo "###################################"
        echo "#  Testing: $dir_name"
        echo "###################################"
        
        # Determine packing mode based on directory name:
        # - crate_packing: use crate-only (only crates, no boxes/pallets)
        # - pallet_packing: use box-only (only boxes and pallets, no crates)
        # - box_packing: use box-only (only boxes and pallets, no crates)
        if [[ "$dir_name" == "crate_packing" ]]; then
            process_csv_files "$dir" "crate-only"
        elif [[ "$dir_name" == "pallet_packing" ]]; then
            process_csv_files "$dir" "box-only"
        elif [[ "$dir_name" == "box_packing" ]]; then
            # box_packing should also use box-only mode (no crates)
            process_csv_files "$dir" "box-only"
        else
            # Other directories: use default mode
            process_csv_files "$dir" "default"
        fi
    done
    
    # Print final summary
    echo ""
    echo "========================================"
    echo "  TEST SUMMARY"
    echo "========================================"
    echo "Total tests:  $TOTAL_TESTS"
    echo -e "${GREEN}Passed:       $PASSED_TESTS${NC}"
    echo -e "${RED}Failed:       $FAILED_TESTS${NC}"
    echo "========================================"
    
    # Exit with appropriate code
    if [[ $FAILED_TESTS -eq 0 ]]; then
        echo ""
        print_success "All tests passed! 🎉"
        exit 0
    else
        echo ""
        print_error "Some tests failed!"
        exit 1
    fi
}

# Run main function
main "$@"
