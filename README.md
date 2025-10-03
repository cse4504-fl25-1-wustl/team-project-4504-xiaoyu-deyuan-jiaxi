# team-project-4504-xiaoyu-deyuan-jiaxi

## Developer Guide

This guide provides instructions for compiling and running the application from the command line on different operating systems.

---

## Running the Application

To run the application, follow these steps.

### 1. Compile the Java Code

Make sure you are in the project root directory (`team-project-4504-xiaoyu-deyuan-jiaxi`).

**Windows (CMD):**

```bat
javac -d bin ArtPackerCli.java parser\*.java entities\*.java interactor\*.java requests\*.java responses\*.java
```

**Windows (PowerShell):**

```powershell
javac (Get-ChildItem -Recurse -Filter *.java).FullName
```

**Linux / macOS (bash/zsh):**

```bash
javac ArtPackerCli.java parser/*.java entities/*.java interactor/*.java requests/*.java responses/*.java
```

---

### 2. Run the Application

Execute the compiled code using the `java` command.
You must provide the path to a `.csv` file as a command-line argument.

```bash
java -cp bin ArtPackerCli <path_to_your_file.csv>
```

**Example:**

```bash
java -cp bin ArtPackerCli test.csv
```

---

## Contribution Breakdown

* Deyuan: Packer
* Jiaxi: Request, Response
* Xiaoyu: Entities, Parser
