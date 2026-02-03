> Disclaimer: This README is work in progress and might not reflect the current state of the mod
# Resonance

Resonance is a Hytale server mod focused on the mystery of Echos. It adds a new Echo ore, ritual crafting, and a small toolkit that lets you manipulate Echo materials through a custom block interaction.

## Current Features
- Echo Stone ore generation in multiple rock types.
- Echo Shards as a primary material drop.
- Ritual Stone crafting station with custom interaction logic.
- Echo Wand to trigger ritual crafting.
- In-world preview items that sit on the Ritual Stone while crafting inputs are loaded.

## Content Added
Items and blocks currently defined by the mod:
- Echo Stone (ore block)
- Echo Shard (material)
- Echo Wand (ritual trigger item)
- Ritual Stone (crafting block)
- Echo Dummy Item (dev/test item)

## Gameplay Flow
1) Mine Echo Stone to obtain Echo Shards.
2) Craft a Ritual Stone at a normal workbench.
3) Place the Ritual Stone and insert up to three items (main + two catalysts).
4) Use the Echo Wand to perform the ritual and produce the matching recipe output.

The Ritual Stone previews inserted items as static world entities. Empty-hand interaction removes the most recently inserted item. Breaking the Ritual Stone drops any stored items.

## World Generation
Echo Stone replaces common rock variants (stone, basalt, sandstone, shale, volcanic) during chunk generation. Generation runs in new chunks only and targets sections that overlap Y 20 to Y 110, with a low density pass.

## Crafting
Ritual Stone:
- 8x Echo Shard
- 10x Rock_Stone (resource type)
- 5x Rock (resource type)
- Bench: Workbench (standard crafting)

Ritual Stone recipes are pulled from the Ritual Stone bench category. The Echo Dummy Item is an example/test recipe wired to this bench.

## Technical Notes
- Core interaction logic: `src/main/java/com/tschm/resonance/interactions/RitualStoneInteraction.java`
- Ritual Stone storage: `src/main/java/com/tschm/resonance/components/RitualStoneComponent.java`
- Ore generation: `src/main/java/com/tschm/resonance/worldgen/OreGenerator.java`

## Building
This project uses Maven. Build with:
```powershell
mvn -q -DskipTests package
```

The mod includes an asset pack and a filtered `manifest.json`.

## License
MIT. See `LICENSE`.

## Credits
- Some item entity utility logic is adapted from Mrbysco/ItemFrames (credited in code comments).
