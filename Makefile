# Makefile for Arkanoid Game
# Now powered by Maven with familiar make commands
# Works on Windows (PowerShell/CMD), Unix-like shells (Git Bash, WSL, macOS)

# Project configuration
JAVA_VERSION = 11
MAIN_CLASS = com.arcade.arkanoid.ArcadeLauncher

# Maven executable (use mvn.cmd on Windows, mvn on Unix)
MAVEN = mvn
ifeq ($(OS),Windows_NT)
    # Windows PowerShell/CMD
    MAVEN = mvn
else
    # Unix-like systems
    MAVEN = mvn
endif

# Default target
all: build

help:
	@echo "Arkanoid Game - Maven-Powered Makefile Commands"
	@echo "==============================================="
	@echo "  make build     Compile the game using Maven (Java $(JAVA_VERSION))"
	@echo "  make run       Compile and run the game"
	@echo "  make test      Run unit tests with JUnit 5"
	@echo "  make clean     Clean build artifacts"
	@echo "  make rebuild   Clean and build"
	@echo "  make package   Create distributable JAR"
	@echo "  make verify    Run tests and validation"
	@echo "  make deps      Download and update dependencies"
	@echo "  make help      Show this help message"
	@echo
	@echo "Maven Commands Available:"
	@echo "  mvn compile    Direct Maven compile"
	@echo "  mvn test       Direct Maven test"
	@echo "  mvn exec:java  Direct Maven run"
	@echo
	@echo "Controls: Left/A, Right/D, Space, Enter, Esc"

# Clean build artifacts using Maven
clean:
	@echo "Cleaning build artifacts with Maven..."
	$(MAVEN) clean
	@echo "Clean completed"

# Compile using Maven
build:
	@echo "Compiling Java sources with Maven (Java $(JAVA_VERSION))..."
	$(MAVEN) compile
	@echo "Build completed successfully"

# Run the game using Maven
run:
	@echo
	@echo "Starting Arkanoid game with Maven..."
	@echo "(Close the game window to return to the terminal)"
	@echo
	$(MAVEN) exec:java
	@echo
	@echo "Game closed. Thanks for playing!"

# Clean and build
rebuild: clean build

# Run tests using Maven
test:
	@echo "Running JUnit 5 tests with Maven..."
	$(MAVEN) test

# Create JAR package
package:
	@echo "Creating distributable JAR with Maven..."
	$(MAVEN) package

# Run tests and validation
verify:
	@echo "Running full verification with Maven..."
	$(MAVEN) verify

# Download and update dependencies
deps:
	@echo "Downloading and updating dependencies..."
	$(MAVEN) dependency:resolve
	@echo "Dependencies updated"

# Check Java and Maven installation
check-java:
	@echo "Checking Java installation..."
	@java -version
	@echo
	@echo "Checking Maven installation..."
	@$(MAVEN) -version
	@echo "Environment check completed"

# Run game without rebuilding (using Maven's compiled classes)
run-only:
	@echo "Running game using Maven (no rebuild)..."
	$(MAVEN) exec:java

# Legacy support - use Maven target directory
legacy-run:
	@echo "Running game from Maven target directory..."
	@if [ -f "target/classes/$(subst .,/,$(MAIN_CLASS)).class" ]; then \
		java -cp target/classes $(MAIN_CLASS); \
	else \
		echo "No compiled classes found. Run 'make build' first."; \
	fi

.PHONY: all help clean build run rebuild test package verify deps check-java run-only legacy-run
