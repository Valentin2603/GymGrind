# Gym Grind: Road to Stage

Week 1 prototype of a JavaFX top-down gym simulator.

## Current status

The project already contains:

- JavaFX application entry point
- main menu with `Start` and `Exit`
- game state enum
- top-down gym map
- controllable player with `WASD` / arrow keys
- map boundaries
- interactive gym objects
- interaction prompt near objects
- placeholder interaction messages on `E`
- starter HUD with stats and form formula
- local Git repository initialized

## Controls

- `W`, `A`, `S`, `D` or arrow keys: move
- `E`: interact with nearby object
- `Enter`: start game from menu
- `Esc`: return to menu

## Project structure

```text
src/main/java/
 └── gymgrind/
     ├── Main.java
     ├── GameApp.java
     ├── GameController.java
     ├── GameRenderer.java
     ├── GameState.java
     ├── InputState.java
     │
     ├── logic/
     │   ├── InteractionService.java
     │   └── MovementService.java
     │
     ├── model/
     │   ├── GameMap.java
     │   ├── GymObject.java
     │   ├── InteractiveZone.java
     │   ├── MachineType.java
     │   ├── Player.java
     │   ├── Position.java
     │   ├── Stats.java
     │   ├── TrainingMachine.java
     │   └── ZoneType.java
     │
     └── ui/
         ├── GameView.java
         ├── Hud.java
         └── MainMenu.java
```

## Run

Recommended:

1. Use JDK 21 or newer.
2. Set `JAVA_HOME`.
3. Run `mvn javafx:run` or start from IntelliJ IDEA.

If Maven wrapper does not work on the first launch, it usually means Maven distribution still needs to be downloaded or `JAVA_HOME` is missing.

## What to build next

### Week 2

- connect real training logic to `TrainingMachine`
- add a click minigame
- update stats after training
- increase fatigue
- keep HUD in sync with game progress

### Week 3

- add money and work interaction
- add shop window and supplements
- add rest zone logic
- add skill check minigame
- add stage win/lose validation

### Week 4

- polish menu and overlays
- add win/lose screens
- balance numbers
- fix bugs
- prepare final presentation build
