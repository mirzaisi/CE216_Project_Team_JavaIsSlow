# PlayForge Manager

PlayForge Manager is a Java-based sports manager game project developed for **CE 216 – Fundamental Topics in Programming**.

The main idea behind the project is to build a manager-style game that is **not tied to only one sport**. Football is the first sport being implemented, but the system is being designed around shared abstractions so that other sports can be added later without rebuilding the whole project from scratch.

At this stage, the project focuses on the **core architecture, foundational classes, and testing infrastructure**. The current goal is to make sure the shared framework is solid, the first sport module can be built on top of it cleanly, and the project runs correctly through Maven.

## Project goals

The project is being developed with a few main goals in mind:

- keep the design extensible for multiple sports,
- separate shared game logic from sport-specific behavior,
- make the code testable and maintainable,
- and provide a runnable Java application structure that can grow over the semester.

## Current status

This repository is currently in active development as part of the milestone-based course project process.

At the moment, the project includes:

- a Maven-based Java project setup,
- a layered package structure,
- shared core abstractions,
- an initial football bootstrap path,
- and a basic runnable entry point.

More functionality, tests, and the full playable flow will be added in later milestones.

## Technologies

- **Java 17**
- **Maven**
- **JUnit 5**

## Project structure

The codebase is organized into a few main parts:

- `core` – shared abstractions and base classes
- `application` – startup and workflow coordination
- `football` – football-specific implementations
- `infrastructure` – support services and assets
- `main` – program entry point

This structure is meant to keep the project aligned with the design document and make future extensions easier.

## How to run

From the project root, use Maven commands in the terminal:

```bash
mvn compile
mvn test
mvn exec:java