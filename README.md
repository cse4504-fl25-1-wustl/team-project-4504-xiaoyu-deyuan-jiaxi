# team-project-4504-xiaoyu-deyuan-jiaxi

A simple packing/shipping planning Java project used for the course project. This repo contains an `app` module with the CLI entrypoint `archdesign.Main`.

## Developer Guide (minimal updates)

This project uses Gradle. Use the included wrapper to build and run the project so you get a reproducible Gradle version.

### Build with Gradle

From the project root:

Windows (CMD/PowerShell):

```powershell
.\gradlew.bat build
```

Linux / macOS:

```bash
./gradlew build
```

This will compile the code and run the tests. If you just want to run the app without tests, append `-x test` to the build command.

### Run the application

If the `:app:run` task is available in the project, you can run via Gradle:

Windows:

```powershell
.\gradlew.bat :app:run --args="<path/to/your.csv>"
```

Linux / macOS:

```bash
./gradlew :app:run --args='<path/to/your.csv>'
```

#### Optional JSON Output and Packing Modes

The application supports optional parameters for JSON output and packing mode selection:

**Full Usage:**
```
Main <input.csv> [output.json] [packing-mode]
```

**Parameters:**
- `input.csv` (required): Path to the input CSV file
- `output.json` (optional): Path to output JSON file. If provided, results will be written in JSON format
- `packing-mode` (optional): Packing strategy to use. Options:
  - `default`: Uses both boxes (STANDARD, LARGE) and CRATE boxes with all container types (default behavior)
  - `box-only`: Only uses STANDARD and LARGE boxes with pallets (no CRATE boxes)
  - `crate-only`: Only uses CRATE boxes with crates (no STANDARD/LARGE boxes)

**Examples:**

Basic usage (console output only, default mode):
```powershell
.\gradlew.bat :app:run --args="<path/to/your.csv>"
```

With JSON output (default mode):
```powershell
.\gradlew.bat :app:run --args="<path/to/your.csv> <path/to/output.json>"
```

With packing mode only (no JSON output):
```powershell
.\gradlew.bat :app:run --args="<path/to/your.csv> box-only"
```

With both JSON output and packing mode:
```powershell
.\gradlew.bat :app:run --args="<path/to/your.csv> <path/to/output.json> crate-only"
```

Linux / macOS examples:

```bash
./gradlew :app:run --args='<path/to/your.csv> <path/to/output.json> box-only'
```

Alternatively, after `./gradlew build` you can run the main class directly:

Windows (CMD):

```bat
java -cp "app\build\libs\*;app\build\classes\java\main" archdesign.Main <path/to/your.csv> [output.json] [packing-mode]
```

Linux / macOS:

```bash
java -cp "app/build/libs/*:app/build/classes/java/main" archdesign.Main <path/to/your.csv> [output.json] [packing-mode]
```

#### JSON Output Format

When an output file is specified, the application generates a JSON file with the following schema:

```json
{
  "total_pieces": <number of art pieces>,
  "standard_size_pieces": <number of standard-sized pieces (≤44")>,
  "oversized_pieces": [
    {
      "side1": <dimension in inches>,
      "side2": <dimension in inches>,
      "quantity": <count>
    }
  ],
  "standard_box_count": <number of STANDARD boxes>,
  "large_box_count": <number of LARGE boxes>,
  "custom_piece_count": <number of UPS and CRATE boxes>,
  "standard_pallet_count": <number of standard and glass pallets>,
  "oversized_pallet_count": <number of oversized pallets>,
  "crate_count": <number of crates>,
  "total_artwork_weight": <total weight of all art pieces>,
  "total_packaging_weight": <total weight of packaging materials>,
  "final_shipment_weight": <total shipment weight>
}
```

#### Error Handling for Unpacked Arts

If any art pieces cannot be packed (e.g., they are too large for available boxes/containers), the application will:

1. Print individual error messages to `System.err` during the packing process, identifying each unpacked art by ID
2. Display a summary warning message at the end showing the total number of unpacked pieces
3. Continue processing and generate results for the pieces that could be successfully packed

Example error output:
```
Art Tag-XL-1 not packable

!!! WARNING !!!
Unable to pack 2 out of 55 art piece(s).
These pieces are too large or do not fit within the available box and container constraints.
Please review the error messages above for specific art IDs that could not be packed.
!!!!!!!!!!!!!!!
```

**Note:** Unpacked pieces are not included in the JSON output, as the output format only contains successfully packed items.

### Notes

- The CLI expects a CSV file path as the first argument.
- Optional second argument: JSON output file path.
- Optional third argument: packing mode (`default`, `box-only`, or `crate-only`).
- For development prefer using the Gradle wrapper (`gradlew` / `gradlew.bat`) included in the repo.

### Running Integration Tests

#### Option 1: Run JUnit Integration Tests (Java Test Classes)

Run all JUnit integration tests from the project root on Windows (cmd.exe / PowerShell):

```powershell
.\gradlew.bat :app:test --no-daemon --tests "archdesign.integration.*"
```

Notes:

- To run every test in the `app` module (unit + integration) use:

```powershell
.\gradlew.bat :app:test --no-daemon
```

- If Gradle skips execution because tasks are "UP-TO-DATE", force a re-run with:

```powershell
.\gradlew.bat :app:test --no-daemon --tests "archdesign.integration.*" --rerun-tasks
```

- After a run, open the HTML report at `app\\build\\reports\\tests\\test\\index.html` to inspect failures. From cmd you can open it with:

```powershell
start "" "app\\build\\reports\\tests\\test\\index.html"
```

#### Option 2: Run CSV-Based Integration Tests (Automated Test Script)

The project includes `run_integration_tests.sh`, a bash script that automatically runs the application against all test cases in `app/src/test/resources/` and validates outputs against expected results.

**How it works:**
- Finds all `expected_output.json` files in the test resources (81 tests total)
- For each JSON file, selects any CSV file in the same directory as input
- Runs the application: `./gradlew run --args="<csv> actual_output.json"`
- Compares the generated output with the expected output (only validates fields present in expected_output.json)
- **Cleanup**: Automatically deletes `actual_output.json` after successful tests; keeps it for failed tests to aid debugging
- Supports three test categories:
  - `box_packing` (51 tests): Uses standard packing mode
  - `pallet_packing` (12 tests): Uses standard packing mode
  - `crate_packing` (18 tests): Uses `-crate-only` mode

**Important Notes:**
- `expected_output.json` contains only **key validation fields**, not the complete output structure
- The script compares only the fields present in `expected_output.json`:
  - `total_pieces`: Total number of artwork pieces
  - `standard_box_count`: Number of standard boxes used
  - `large_box_count`: Number of large boxes used
  - `custom_piece_count`: Number of custom/oversized pieces
  - Additional container counts (pallets, crates) in some tests
- Generated `actual_output.json` contains the full detailed output but is deleted after passing tests

**Run the script:**

On **Git Bash** or **WSL**:
```bash
# Make the script executable (first time only)
chmod +x run_integration_tests.sh

# Run all integration tests
./run_integration_tests.sh
```

On **Windows PowerShell** (requires Git Bash or WSL installed):
```powershell
# Using Git Bash
bash run_integration_tests.sh

# Or using WSL
wsl bash run_integration_tests.sh
```

**Output format:**
```
========================================
  Integration Test Runner
========================================

###################################
#  Testing: box_packing
###################################
[INFO] Processing tests in: app/src/test/resources/box_packing

================================================
Test #1: MixedMediumSameSize/LargeBox/1_4PerBox-5_6PerBox
================================================
[INFO] Input: input.csv
[INFO] Expected: expected_output.json
[INFO] Output: actual_output.json
[PASS] PASSED: MixedMediumSameSize/LargeBox/1_4PerBox-5_6PerBox

...

========================================
  TEST SUMMARY
========================================
Total tests:  81
Passed:       81
Failed:       0
========================================

[PASS] All tests passed! 🎉
```

**Notes:**
- The script automatically excludes the `e2e` directory from testing
- Failed tests display the test name and reason (e.g., "Output mismatch", "No CSV file found")
- The script uses `jq` or `python3` for JSON comparison if available, otherwise falls back to `diff`
- You can also use the older `run_all_tests.sh` script to run JUnit tests via Gradle

#### Running with Git Bash

```bash
# Run CSV-based integration tests (recommended)
./run_integration_tests.sh

# Or run JUnit integration tests
./run_all_tests.sh

# Or run the Gradle wrapper directly (works in Git Bash)
./gradlew :app:test --tests "archdesign.integration.*"
./gradlew :app:test --tests "archdesign.integrationBox.*"
```

Notes:
- If `./gradlew` complains about permissions, make it executable once: `chmod +x gradlew`.
- You can still use `gradlew.bat` from PowerShell or cmd.exe; use whichever shell you prefer.
- To open the HTML test report from Git Bash (Windows):

```bash
cmd.exe /c start "" app\\build\\reports\\tests\\test\\index.html
```

### Feature

Xiaoyu: Algorithm fix, main args & cli & .json presenter implementation
Jiaxi: Box testing files design & implementation
Deyuan: Crate & Pallet testing files design & implementation



