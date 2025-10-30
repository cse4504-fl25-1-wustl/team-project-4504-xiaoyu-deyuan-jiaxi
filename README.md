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

Alternatively, after `./gradlew build` you can run the main class directly using the classpath produced in `app/build` e.g.:

Windows (CMD):

```bat
java -cp "app\build\libs\*;app\build\classes\java\main" archdesign.Main <path/to/your.csv>
```

Linux / macOS:

```bash
java -cp "app/build/libs/*:app/build/classes/java/main" archdesign.Main <path/to/your.csv>
```

### Notes

- The CLI expects a CSV file path as the only argument.
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



