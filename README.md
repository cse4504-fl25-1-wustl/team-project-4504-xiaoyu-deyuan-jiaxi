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

Alternatively, after `./gradlew build` you can run the main class directly:

```bash
java -cp "app/build/libs/*:app/build/classes/java/main" archdesign.Main <path/to/your.csv> [output.json] [packing-mode]
```

## GUI and packaging — detailed guide

This project contains both a command-line application (`archdesign.Main`) and a
lightweight Swing GUI (`archdesign.gui.GuiApp`). Both use the same core
business logic. The repository includes Gradle tasks to run the CLI or GUI in
development and provides two "fat" JAR tasks that bundle dependencies for
distribution:

- `:app:fatJar` — runnable JAR whose manifest `Main-Class` is `archdesign.Main` (CLI)
- `:app:fatJarGui` — runnable JAR whose manifest `Main-Class` is `archdesign.gui.GuiApp` (GUI)

Why two fat jars? Double-click behavior on desktop platforms launches the JAR's
manifest main class. To make a double-click open the GUI, use `fatJarGui`. If
you want a desktop installer, use `jpackage` (manual) or configure the
`org.beryx.runtime` Gradle plugin (optional) to automate packaging.

Prerequisites
 - JDK 21 (recommended) installed. Verify with:

```powershell
java -version
jpackage --version  # Optional - shows if jpackage is available in the JDK
```

 - Gradle wrapper is included; use it to ensure consistent Gradle.

Alpha release branch
 - The alpha work should live in `alpha_release`. To create and switch to it:

```powershell
git checkout main
git pull origin main
git checkout -b alpha_release
git push -u origin alpha_release
```

Building and running locally (development)

1) Build everything (compile + tests):

Windows (cmd/powershell):

```powershell
.\gradlew.bat build
```

macOS / Linux:

```bash
./gradlew build
```

2) Run the GUI from Gradle (development):

Windows:

```powershell
.\gradlew.bat :app:runGui
```

macOS / Linux:

```bash
./gradlew :app:runGui
```

3) Create fat jars (single-file distributions)

CLI fat jar (runs CLI when double-clicked or `java -jar`):

```powershell
.\gradlew.bat :app:fatJar
```

GUI fat jar (recommended for testers who want to double-click to launch UI):

```powershell
.\gradlew.bat :app:fatJarGui
```

Artifacts will be in `app/build/libs/`. Example names you will see in this
repository after a local build are:

- `app-all.jar` (generic fat jar built by the task)
- `app.jar` (non-fat jar)
- `app-all.jar` or `app-gui-all.jar` depending on project configuration — use
  whichever file your build produced; look in `app/build/libs/` to confirm.

Run a fat JAR from the command line (recommended for troubleshooting):

```powershell
java -jar app\build\libs\app-all.jar  <path/to/input.csv>
# or for GUI-fat-jar
java -jar app\build\libs\app-gui-all.jar
```

If a fat JAR does nothing when double-clicked, prefer running it from the
terminal to capture error messages (see Troubleshooting below).

Creating platform installers (manual jpackage)

Use `jpackage` when you want native installers (.exe/.msi on Windows, .dmg/.pkg
on macOS). You must run `jpackage` on the target OS. A minimal example follows.

Windows example (run on Windows, adjust names/paths):

```powershell
# 1) produce GUI fat jar
.\gradlew.bat :app:fatJarGui

# 2) run jpackage (simple example)
jpackage --input "app\build\libs" --name "PackerGUI" --main-jar "app-gui-all.jar" --main-class "archdesign.gui.GuiApp" --type exe --dest "dist" --app-version "1.0.0"
```

macOS example (run on macOS):

```bash
# 1) produce GUI fat jar
./gradlew :app:fatJarGui

# 2) run jpackage to create a DMG
jpackage --input "app/build/libs" --name "PackerGUI" --main-jar "app-gui-all.jar" --main-class "archdesign.gui.GuiApp" --type dmg --dest "dist" --app-version "1.0.0"
```

Suggested jpackage options you may want to set:
- `--icon <path>` to supply an application icon
- `--vendor` and `--app-version` to set metadata
- `--win-menu`/`--win-shortcut` to create Start Menu shortcuts on Windows
- `--mac-sign` and code-signing options on macOS (requires an Apple developer
  certificate)

Automating packaging via Gradle (optional)

If you want a fully automated Gradle task that creates runtime images and
installers, consider the `org.beryx.runtime` plugin. It will call `jlink` and
`jpackage` for you. We intentionally did not enable it in the default build to
avoid environments where plugin resolution or platform differences cause
surprises. If you want, I can add an optional, gated `runtime` block that is
enabled only when a project property (for example `-Pruntime=true`) is set.

Double-click behavior and which jar to use

- If you want testers to double-click and see the GUI, produce and distribute
  the GUI fat jar (`:app:fatJarGui`) or a platform installer produced by
  `jpackage` that points to the GUI main class.
- If you double-click the CLI fat jar, it will open but immediately exit with a
  usage message because the CLI requires a CSV argument. That's why we produce
  a separate GUI fat jar.

Troubleshooting
 - If double-clicking a JAR does nothing:
   1. Open a terminal and run `java -jar <jar-file>` to see console errors.
   2. Confirm the JDK/JRE on the machine matches the Java version used to
      build the jar (we use Java 21 toolchain in the project). `java -version`
      shows the runtime version.
   3. If `Error: Please provide the path to the CSV file as an argument.`
      appears when you run a fat JAR, you ran the CLI jar without arguments —
      use the GUI fat jar instead.
 - If `jpackage` is not found: install a JDK that includes jpackage or use a
   CI runner that has jpackage available. `jpackage --version` should return
   a version if available.

Packaging checklist for user testing (alpha release)
 - [ ] Merge feature branches (e.g., `feature2`) into `main` and create `alpha_release`
 - [ ] Confirm `:app:fatJarGui` produces a GUI-fat JAR that launches the GUI
 - [ ] Create platform-specific installer(s) via `jpackage` on each target OS
 - [ ] Add icons, version, and vendor metadata to installers
 - [ ] Test installers on clean VMs (Windows/macOS) and confirm they launch

Continuous integration suggestion
 - Add a GitHub Actions workflow that runs on `alpha_release` and performs:
   1. `./gradlew :app:fatJarGui` to produce the GUI jar
   2. On platform-specific runners (windows-latest, macos-latest), run
      `jpackage` to produce installers and upload artifacts

If you'd like, I can add an example `org.beryx.runtime` Gradle configuration
behind a property and a GitHub Actions example that creates installers for
Windows and macOS and uploads them as release artifacts.
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
- Runs the application with the appropriate packing mode based on test category
- Compares the generated output with the expected output (only validates fields present in expected_output.json)
- **Cleanup**: Automatically deletes temporary output files after successful tests; keeps them for failed tests to aid debugging
- Supports three test categories with different packing modes:
  - `box_packing` (51 tests): Uses **default mode** (boxes and pallets only)
  - `pallet_packing` (12 tests): Uses **`-box-only` mode** (boxes and pallets only, explicitly no crates)
  - `crate_packing` (18 tests): Uses **`-crate-only` mode** (crates only, no boxes/pallets)

**Important Notes:**
- `expected_output.json` contains only **key validation fields**, not the complete output structure
- The script compares only the fields present in `expected_output.json`:
  - `total_pieces`: Total number of artwork pieces
  - `standard_box_count`: Number of standard boxes used
  - `large_box_count`: Number of large boxes used
  - `custom_piece_count`: Number of custom/oversized pieces
  - Additional container counts (pallets, crates) in some tests
- Temporary output files are created in-memory and deleted immediately after validation

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



