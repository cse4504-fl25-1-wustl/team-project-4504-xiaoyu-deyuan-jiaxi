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

#### Optional JSON Output

You can optionally specify an output JSON file as a second argument. If provided, the application will write the packing results to the file in JSON format:

Windows:

```powershell
.\gradlew.bat :app:run --args="<path/to/your.csv> <path/to/output.json>"
```

Linux / macOS:

```bash
./gradlew :app:run --args='<path/to/your.csv> <path/to/output.json>'
```

Alternatively, after `./gradlew build` you can run the main class directly using the classpath produced in `app/build` e.g.:

Windows (CMD):

```bat
java -cp "app\build\libs\*;app\build\classes\java\main" archdesign.Main <path/to/your.csv> [optional-output.json]
```

Linux / macOS:

```bash
java -cp "app/build/libs/*:app/build/classes/java/main" archdesign.Main <path/to/your.csv> [optional-output.json]
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

### Notes

- The CLI expects a CSV file path as the first argument.
- An optional JSON output file path can be provided as the second argument.
- For development prefer using the Gradle wrapper (`gradlew` / `gradlew.bat`) included in the repo.

### Running integration tests

Run all integration tests (tests in the `archdesign.integration` package) from the project root on Windows (cmd.exe / PowerShell):

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

### Feature

Xiaoyu: Algorithm fix, main args & cli & .json presenter implementation
Jiaxi: Box testing files design & implementation
Deyuan: Crate & Pallet testing files design & implementation



