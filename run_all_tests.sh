#!/bin/bash

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Function to print headers
print_header() {
    echo -e "\n${BLUE}=== $1 ===${NC}"
}

print_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

print_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

print_info() {
    echo -e "${YELLOW}[INFO]${NC} $1"
}

# Function to count actual test methods using Gradle's test report
count_test_methods() {
    
    # Clean previous test results to get accurate count
    ./gradlew clean test --tests "archdesign.integration.*" --tests "archdesign.integrationBox.*" --no-daemon > /dev/null 2>&1
    
    # Parse the test report to count individual test methods
    local test_report="./app/build/reports/tests/test/index.html"
    
    if [[ -f "$test_report" ]]; then
        # Extract test counts from the HTML report
        local total_tests=$(grep -oE "tests? passed[^<]*" "$test_report" | grep -oE '[0-9]+' | head -1)
        if [[ -n "$total_tests" ]]; then
            echo "$total_tests"
            return
        fi
    fi
    
    # Fallback: count test methods in source files (less accurate but works)
    local total_methods=0
    while IFS= read -r test_file; do
        if [[ -f "$test_file" ]]; then
            local methods_in_file=$(grep -c "@Test" "$test_file" 2>/dev/null || echo "0")
            total_methods=$((total_methods + methods_in_file))
        fi
    done < <(find . -path "*/archdesign/integration*/*Test.java" -type f)
    
    echo "$total_methods"
}

# Function to run all integration tests with progress
run_all_integration_tests() {
    print_header "RUNNING ALL INTEGRATION TESTS"
    
    # First, count the total test methods
    print_info "Counting test methods..."
    local total_tests=$(count_test_methods)
    
    if [[ $total_tests -eq 0 ]]; then
        print_error "No tests found! Please check your test configuration."
        exit 1
    fi
    
    print_info "Running $total_tests test methods"
    echo
    
    # Run the tests with more verbose output
    if ./gradlew test --tests "archdesign.integration.*" --tests "archdesign.integrationBox.*" --info; then
        print_success "🎉 ALL $total_tests TEST METHODS PASSED!"
        return 0
    else
        print_error "❌ SOME TESTS FAILED (out of $total_tests total methods)"
        return 1
    fi
}

# Function to run tests with detailed progress
run_tests_with_progress() {
    print_header "RUNNING INTEGRATION TESTS WITH PROGRESS"
    
    # Count test classes
    local integration_classes=$(find . -path "*/archdesign/integration/*Test.java" -type f | wc -l)
    local integrationBox_classes=$(find . -path "*/archdesign/integrationBox/*Test.java" -type f | wc -l)
    local total_classes=$((integration_classes + integrationBox_classes))
    
    print_info "Running tests from $total_classes test classes..."
    print_info "Progress: 0/$total_classes"
    
    local current=0
    local passed=0
    local failed=0
    
    # Run integration tests class by class for better progress tracking
    while IFS= read -r test_file; do
        ((current++))
        local class_name=$(echo "$test_file" | sed 's|.*/archdesign/|archdesign.|' | sed 's|.java||' | sed 's|/|.|g')
        
        echo -n "[$current/$total_classes] Running $class_name... "
        
        if ./gradlew test --tests "$class_name" --quiet > /dev/null 2>&1; then
            echo -e "${GREEN}✓${NC}"
            ((passed++))
        else
            echo -e "${RED}✗${NC}"
            ((failed++))
        fi
    done < <(find . -path "*/archdesign/integration*/*Test.java" -type f)
    
    # Final summary
    print_header "TEST EXECUTION SUMMARY"
    echo "Total test classes: $total_classes"
    echo -e "${GREEN}Passed: $passed${NC}"
    echo -e "${RED}Failed: $failed${NC}"
    echo "Total test methods: ~73 (multiple methods per class)"
    
    if [[ $failed -eq 0 ]]; then
        print_success "All test classes passed!"
        return 0
    else
        print_error "$failed test classes failed"
        return 1
    fi
}

# Function to show detailed test report
show_detailed_report() {
    print_header "DETAILED TEST REPORT"
    
    local test_report="./app/build/reports/tests/test/index.html"
    if [[ -f "$test_report" ]]; then
        print_info "Full HTML report: $test_report"
        
        # Try to extract basic info from the report
        if grep -q "tests passed" "$test_report"; then
            echo "Test results summary:"
            grep -E "tests? (passed|failed|skipped)" "$test_report" | head -3
        fi
    else
        print_info "No test report generated yet. Run tests first."
    fi
}

# Main execution function
main() {
    print_header "INTEGRATION TEST RUNNER"
    
    
    # Check if we're in the right directory
    if [[ ! -f "gradlew" ]]; then
        print_error "gradlew not found. Please run from project root."
        exit 1
    fi
    
    case "${1:-}" in
        "progress")
            run_tests_with_progress
            ;;
        "count")
            local total_tests=$(count_test_methods)
            print_header "ACCURATE TEST METHOD COUNT"
            echo "Total test methods: $total_tests"
            echo "Test classes: 54 (30 integration + 24 integrationBox)"
            echo "This explains why we have more test cases than files!"
            ;;
        "report")
            show_detailed_report
            ;;
        "fast")
            # Simple fast execution
            print_header "RUNNING TESTS (FAST MODE)"
            ./gradlew test --tests "archdesign.integration.*" --tests "archdesign.integrationBox.*"
            ;;
        *)
            # Default: run with normal output
            run_all_integration_tests
            ;;
    esac
    
    # Always show where to find detailed reports
    echo
    print_info "For detailed results: ./app/build/reports/tests/test/index.html"
}

# Run main function
main "$@"