# Retro Pacman Arcade Clone 

A standalone, desktop 2D Pacman game clone engineered in Java using the Swing framework and Abstract Window Toolkit (AWT). The project features real-time game loops, automated non-player character (NPC) behavior paths, matrix-level tile maps, and continuous background audio streams.

---

## Technical Highlights & Features

* **Dynamic Ghost AI Behavior Tracking:** Ghosts evaluate multi-directional movement tiles dynamically at grid intersections. Valid paths are determined using layout lookaheads via lookups across a strict boundary tracking algorithm.
* **Procedural Math-Driven Sprite Animation:** Pacman's mouth movement cycles are computed mathematically using active angle offsets over frame tick increments via an adjustable swinging delta loop.
* **Linear Matrix Game Board Mapping:** The graphical map landscape relies on a custom, optimized 1D short array structure mapped to 20x22 column-row configurations for memory efficiency and instant O(1) tile status calculations.
* **Concurrently Positioned Collision Spaces:** Handles discrete bounding checks using geometric interception parameters (`java.awt.Rectangle`) to manage tile item digestion and evaluate instant player-to-enemy impact zones.

---

##  Software Engineering Principles Applied

* **Composition vs Inheritance:** The primary GUI layer follows strict composition metrics, establishing a structural `HAS-A` relationship where the overarching configuration frame encapsulates a specialized rendering panel container (`GameBoard`).
* **Runtime Polymorphism:** Implements Java standard abstraction implementations (`ActionListener`, `KeyAdapter`) to tie key bindings, execution updates, and render refreshes cleanly to system timers.
* **Decoupled Audio Pipeline Stream Mixer:** Uses a dedicated multithreaded `SoundPlayer` utility layer accessing the Java Sound API (`javax.sound.sampled`) to manage concurrent background music configurations alongside instant sound effects without pausing execution.

---

##  Tech Stack & Dependencies

* **Language:** Java Core (JDK 8 or higher)
* **GUI Framework:** Java Swing & AWT 
* **Audio Library:** Java Audio System API

---

## Project Architecture

```text
├── Pacman_Game.java        # Main Desktop Application & GUI Configurator
├── SoundModule             # Utility Engine handling Play/Loop Audio Channels
└── GameBoard               # Core Loop Logic, AI Engines, Render Pipelines
