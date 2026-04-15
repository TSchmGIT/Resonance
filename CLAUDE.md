# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build

```bash
mvn -q -DskipTests package
```

Output: `target/Resonance-0.0.3.jar` (Java 25, Maven). No tests are defined. Drop the JAR into the Hytale server's `plugins/` directory.

## Hytale Server Reference Source (Decompiled)

**Location:** Configured as an additional directory in `.claude/settings.json`

This is the **official decompiled `HytaleServer.jar`** — use it as a read-only reference to understand Hytale's internal APIs, conventions, and patterns before implementing anything.

### Critical: Decompilation Limitations
- **Do not attempt to fix or edit this source** — it is reference material only
- **It will not compile** — decompiler artifacts are expected and normal
- Broken lambda reconstructions, synthetic accessors, and `// $FF:` comments can be ignored
- Auto-generated variable names (`var1`, `param0`, `i`) are unreliable — focus on method signatures and class structure instead

### What to Trust
| Reliable | Unreliable |
|---|---|
| Class/interface names and package layout | Method bodies with decompiler artifacts |
| Field names, types, and annotations | Reconstructed lambdas and switch expressions |
| Method signatures and return types | Synthetic inner class accessors |
| Interface hierarchies and generics | Auto-named local variables |

### How to Use It
When implementing a new feature in Resonance:
1. Search the reference source for how Hytale handles the same or similar logic
2. Prefer Hytale's own patterns and naming conventions over generic Java idioms
3. If a method body is unreadable, the class structure, field types, and method signatures are still valid
4. Cross-reference with what is already established in this mod (see Architecture above)

## Architecture

Resonance is a Hytale server plugin using a **component-based ECS** provided by the Hytale API. The three core abstractions are:

### Components
Data holders attached to blocks (via `ChunkStore`) or entities (via `EntityStore`). Each component class has:
- A static `ComponentType` registered once during plugin startup via `setComponentType()`
- A static `CODEC` (`BuilderCodec`) for automatic serialization
- A `clone()` implementation

### Systems
Tick-based processors that operate on all blocks/entities possessing the queried component types. Systems implement `EntityTickingSystem<ChunkStore>` and declare their scope via `getQuery()`. Multi-component queries use `Query.and(CompA, CompB, ...)`.

### Interactions
Player action handlers (`SimpleBlockInteraction`) that read and mutate components on the targeted block. Registered with a string key and codec in `Resonance.java`.

### Registration (`Resonance.java`)
All components, systems, and interactions must be registered in `Resonance.java#setup()`. Adding a new component/system/interaction requires adding entries in this file.

## Essence System

The essence (Resonant Essence / RE) transfer layer:

- **`IEssenceStorage`** — interface with `addEssence()`, `removeEssence()`, and a static `transferEssence()` simulation helper
- **`AbstractEssenceStorage`** — implements the interface with capacity, rate limiting, and simulation logic
- **`EssenceStorageComponent`** — concrete storage component with `boundSenderList` / `boundReceiverList` for multi-block networks
- **`EssenceGeneratorComponent`** — abstract base for generators; subclasses (e.g., `SolarAttunementStoneComponent`, `CarbonAttunementStoneComponent`) add production-specific fields
- Generator systems in `systems/generators/` handle tick-based production and transfer to bound storage

## Key Workaround: Polymorphic Generator Lookup

The Hytale ECS does not support querying by interface. When code needs to find "any generator at a position" (e.g., during EchoWand binding), it must call `ComponentHelper.findGeneratorComponentAt()`, which does a manual try-each lookup:

```java
// ComponentHelper.java
comp = blockComponentChunk.getComponent(idx, SolarAttunementStoneComponent.getComponentType());
comp = comp != null ? comp : blockComponentChunk.getComponent(idx, CarbonAttunementStoneComponent.getComponentType());
// ... add new generator types here
```

**Adding a new generator type requires updating `findGeneratorComponentAt()`.**

## Resources

Block models, item definitions, and particle effects live entirely in `src/main/resources/` (no code generation):

- `Common/Blocks/<Name>/` — `.blockymodel` and textures; block states (e.g., `On`, `Off`) must match state names referenced in code via `getBlockKeyForState()`
- `Server/Item/Items/<Name>.json` — item metadata
- `Server/Particles/` and `Server/Particles/Item/` — particle effect and spawner definitions

## Key Files

| Purpose | Path |
|---|---|
| Plugin entry point & registration | `src/main/java/com/tschm/resonance/Resonance.java` |
| Essence storage interface | `components/essence/IEssenceStorage.java` |
| Essence storage component | `components/essence/EssenceStorageComponent.java` |
| Generator base class | `components/essence/EssenceGeneratorComponent.java` |
| Polymorphic generator lookup | `util/ComponentHelper.java` |
| Spatial / position utilities | `util/SystemsHelper.java` |
| Ritual Stone interaction | `interactions/RitualStoneInteraction.java` |
| EchoWand interaction + state machine | `interactions/EchoWandInteraction.java` + `metadata/EchoWandMetaData.java` |
| Ore world generation | `worldgen/OreGenerator.java` |