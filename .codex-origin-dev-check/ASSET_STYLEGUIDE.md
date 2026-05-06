# Asset Style Guide

This project uses small 2D game assets for a top-down JavaFX gym simulator.

## Core Style

- Style: 2D pixel art
- Camera: top-down or slight 3/4 top-down
- Shape language: simple, readable silhouettes
- Colors: bright, clean, and consistent
- Background: transparent PNG for sprites
- Detail level: low to medium, readable at game scale

Avoid realistic 3D models, photorealistic images, heavy isometric detail, and assets with mismatched camera angles.

## Recommended Sizes

- Map tiles: 64x64 px
- Player sprites: 64x64 px
- NPC sprites: 64x64 px
- Small machines: 64x64 or 96x96 px
- Large machines and zones: 128x128 px
- UI elements: size depends on layout, but keep the same pixel art style

## Asset Folders

Assets are stored under:

```text
src/main/resources/assets/
    tiles/
    machines/
    characters/
    npcs/
    ui/
```

## Naming Rules

Use lowercase file names with underscores.

Examples:

```text
floor_tile.png
wall_tile.png
bench_press.png
squat_rack.png
player_idle_front.png
player_walk_left_1.png
trainer_npc.png
```

## Prompt Template

Use one shared prompt style so generated assets match each other:

```text
Create a 2D pixel art [object name] for a top-down gym simulator game, clean readable shapes, bright simple colors, consistent game asset style, transparent background.
```

For tiles:

```text
Create a seamless 2D pixel art [tile type] tile for a top-down gym simulator game, 64x64 style, clean readable shapes, bright simple colors, consistent game asset style.
```

## MVP Asset Set

Minimum assets for the first playable version:

- Tiles: floor, wall, stage
- Machines: bench press, squat rack, treadmill, deadlift platform
- Zones: shop counter, rest zone
- Player: idle and walking sprites for four directions
- NPCs: trainer, rival

