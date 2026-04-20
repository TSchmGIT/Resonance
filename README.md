# Resonance

**Resonance** is a Hytale server mod centered around mystical automation powered by Echoes and Resonant Essence (RE). It starts with ore mining and ritual crafting, then grows into a small network of generators, storage, and utility blocks you can wire together with the **Echo Wand**.

If you like mods where progression moves from "find rare stuff" to "build smart systems," Resonance is built for that style.

## What You Can Do

### 1) Discover and Mine Echo Stone
- Echo Stone is injected into worldgen for newly generated chunks.
- It can appear in stone, basalt, sandstone, shale, and volcanic rock layers.
- Mining Echo Stone yields **Echo Shards** (plus cobble), which are your starter material.

### 2) Build a Ritual-Crafting Setup
- Craft a **Ritual Stone** at a regular Workbench.
- Place up to **three inputs** on the Ritual Stone (main + two catalyst slots).
- Use the **Echo Wand** to trigger ritual crafting.
- Inserted items are previewed as in-world entities, and stored items are safely dropped if the Ritual Stone is broken.

### 3) Craft Echo Materials and Tools
Ritual recipes are grouped into bench categories:
- **Echo Ingredients** (e.g., Essence of Echo, Echo of Power, Echo of Swiftness)
- **Echo Items** (e.g., Resonant Pickaxe)
- **Echo Storage** (e.g., Resonant Vessel)
- **Echo Generator** (attunement stones and utility blocks)

### 4) Generate, Store, and Route Resonant Essence
Resonance includes an RE infrastructure layer:
- **Solar Attunement Stone**: passive essence generation over time.
- **Carbon Attunement Stone**: burns fuel-like inputs to generate essence.
- **Resonant Vessel**: high-capacity essence storage with visual fill levels.
- **Echo Wand bindings**: bind generators to storage, and bind compatible echo-storage blocks together.

### 5) Automate Utility Actions
- **Resonant Attractor** pulls nearby dropped items toward itself.
- **Resonant Disruptor** repeatedly damages harvestable blocks in an area, consuming essence as it works.
- **Resonant Pickaxe** is included in progression and described in-game as a 3x3 mining tool.

## Current Content Snapshot
The repo currently includes definitions, assets, and logic for:
- Echo Stone, Echo Shard, Echo Wand
- Ritual Stone
- Essence of Echo, Echo of Swiftness, Echo of Power
- Basic / Solar / Carbon / Verdant Attunement Stones
- Resonant Vessel
- Resonant Attractor
- Resonant Disruptor
- Resonant Pickaxe

> Note: Verdant Attunement Stone is marked `[NYI]` in localization and does not appear to be wired into active ticking systems yet.

## Gameplay Loop (Quick Version)
1. Mine **Echo Stone** for **Echo Shards**.
2. Craft and place a **Ritual Stone**.
3. Use ritual recipes to make **Essence of Echo** and advanced components.
4. Place generators + storage, then bind them with the **Echo Wand**.
5. Expand into utility automation (attraction/disruption) and higher-tier tools.

## Technical Notes
- Plugin entry: `com.tschm.resonance.Resonance`
- Manifest description: **Mystical fantasy automation mod**
- Includes asset pack resources (models, textures, animations, language)

## Building
This project uses Maven:

```bash
mvn -q -DskipTests package
```

## License
MIT. See [LICENSE](LICENSE).
