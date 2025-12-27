# Flood It – Simon Style Game 🎮

## Team Name
**Alpha Coders**

## Project Description
**Flood It – Simon Style** is a Java-based graphical game inspired by the classic *Flood-It* puzzle and the *Simon* color memory concept.  
The objective of the game is to flood the entire grid with a single color using the **minimum number of moves**, starting from the top-left cell.

The game is implemented using **Java Swing** and provides interactive controls, multiple modes, grid sizes, undo/redo functionality, and hints.

---

## Features ✨
- 🎨 Multiple color choices (Red, Green, Blue, Yellow, Magenta, Orange)
- 🧩 Adjustable grid sizes
- 🎯 Game modes (Normal / Challenge-style behavior)
- 🔄 Undo and Redo support
- 💡 Hint system to assist the player
- 📊 Move counter display
- 🖥️ Interactive GUI using Java Swing

---

## Technologies Used 🛠️
- **Language:** Java
- **GUI Framework:** Java Swing
- **Core Concepts:**
  - Object-Oriented Programming (OOP)
  - Event Handling
  - Recursion / Flood Fill Algorithm
  - Stack-based Undo/Redo
  - GUI Layout Management

---

## How the Game Works 🧠
1. The grid is initialized with random colors.
2. The top-left cell is the starting point.
3. The player selects a color.
4. All connected cells of the starting color change to the selected color.
5. The goal is to make the entire grid a single color in the least number of moves.

---

## Controls 🎮
- **Color Buttons / Dropdown:** Select the next flood color
- **Undo:** Revert the previous move
- **Redo:** Reapply an undone move
- **Grid Size Selector:** Change board size
- **Mode Selector:** Switch game modes
- **Hint:** Displays a suggested color choice

---

## How to Run the Program ▶️

### Prerequisites
- Java JDK 8 or higher
- Any Java-supported IDE (VS Code, IntelliJ, Eclipse) or terminal

### Steps
```bash
javac FloodItSimonStyle.java
java FloodItSimonStyle
