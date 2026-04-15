package com.tschm.resonance.systems.functional;

import com.hypixel.hytale.component.AddReason;
import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Holder;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.spatial.SpatialResource;
import com.hypixel.hytale.component.system.EntityEventSystem;
import com.hypixel.hytale.component.system.tick.EntityTickingSystem;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3f;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.event.events.ecs.BreakBlockEvent;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.inventory.container.ItemContainer;
import com.hypixel.hytale.server.core.inventory.transaction.ItemStackTransaction;
import com.hypixel.hytale.server.core.modules.entity.item.ItemComponent;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.NPCPlugin;
import com.hypixel.hytale.server.npc.entities.NPCEntity;
import com.tschm.resonance.components.essence.EssenceStorageComponent;
import com.tschm.resonance.components.functional.EchoConjurerComponent;
import com.tschm.resonance.util.BlockHelper;
import com.tschm.resonance.util.ComponentHelper;
import com.tschm.resonance.util.DebugHelper;
import com.tschm.resonance.util.SystemsHelper;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;
import org.checkerframework.checker.nullness.compatqual.NullableDecl;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Random;

public class EchoConjurerSystems {

    public static class TickingSystem extends EntityTickingSystem<ChunkStore> {

        private static final Random RANDOM = new Random();
        private static final int PROBE_HEIGHT_ABOVE = 4;
        private static final int PROBE_DEPTH = 8;

        @Override
        public void tick(float dt, int idx,
                         @NonNullDecl ArchetypeChunk<ChunkStore> archetypeChunk,
                         @NonNullDecl Store<ChunkStore> store,
                         @NonNullDecl CommandBuffer<ChunkStore> commandBuffer) {
            final EchoConjurerComponent comp = archetypeChunk.getComponent(idx, EchoConjurerComponent.getComponentType());
            final EssenceStorageComponent compStorage = archetypeChunk.getComponent(idx, EssenceStorageComponent.getComponentType());
            final Vector3i targetPos = SystemsHelper.getPosForBlock(archetypeChunk, idx, commandBuffer);
            assert comp != null && compStorage != null && targetPos != null;

            final World world = commandBuffer.getExternalData().getWorld();

            boolean preConditionsMet = checkPreConditions(comp, compStorage, world, targetPos);
            BlockHelper.activateBlockState(preConditionsMet ? "On" : "Off", world, targetPos);

            // Update cadence timer
            if (!preConditionsMet)
                comp.remainingTicks = comp.ticksPerSpawn;
            else if (comp.remainingTicks > 0) {
                comp.remainingTicks--;
                return;
            }
            comp.remainingTicks = comp.ticksPerSpawn;

            if (preConditionsMet)
                conjure(comp, compStorage, world, targetPos);
        }

        private static boolean checkPreConditions(EchoConjurerComponent comp, EssenceStorageComponent
                compStorage, World world, Vector3i blockPos) {
            // No NPCs configured
            if (comp.npcIds == null || comp.npcIds.isEmpty()) {
                DebugHelper.PrintOnce("conjurer_no_npcs", "EchoConjurer: No NPCs configured");
                return false;
            }

            // Simulate RE removal
            if (compStorage.removeEssence(comp.essencePerSpawn, true) < comp.essencePerSpawn) {
                DebugHelper.PrintTimed("conjurer_insufficient_re", "EchoConjurer: Insufficient Resonant Essence");
                return false;
            }

            // Check Life Essence availability
            if (comp.lifeEssencePerSpawn > 0 && countItemsInContainer(comp.lifeEssenceContainer, comp.lifeEssenceItemId) < comp.lifeEssencePerSpawn) {
                DebugHelper.PrintTimed("conjurer_insufficient_life_essence", "EchoConjurer: Insufficient Life Essence");
                return false;
            }

            final String activeId = comp.npcIds.get(Math.floorMod(comp.selectedIndex, comp.npcIds.size()));
            final Store<EntityStore> entityStore = world.getEntityStore().getStore();

            final Vector3d center = new Vector3d(blockPos).add(0.5, 0.5, 0.5);

            // Cap check: count NPCs with matching active id within spawnRange
            int matchingCount = countMatchingNpcsInRange(entityStore, center, comp.spawnRange, activeId);
            if (matchingCount >= comp.maxNpcsInRange)
                return false;

            return true;
        }

        private static void conjure(EchoConjurerComponent comp, EssenceStorageComponent compStorage, World world, Vector3i blockPos) {
            // Pick ground-aligned spawn position
            Vector3d spawnPos = pickGroundAlignedPosition(world, blockPos, comp.spawnRange);
            if (spawnPos == null) {
                DebugHelper.Print("EchoConjurer: No valid spawn position found");
                return;
            }

            // Consume resources
            compStorage.removeEssence(comp.essencePerSpawn, false);
            if (comp.lifeEssencePerSpawn > 0) {
                ItemStack itemStackToRemove = new ItemStack(comp.lifeEssenceItemId, comp.lifeEssencePerSpawn);
                ItemStackTransaction transaction = comp.lifeEssenceContainer.removeItemStack(itemStackToRemove, true, false);
                if (!transaction.succeeded()) {
                    DebugHelper.PrintTimed("conjurer_life_essence_consume_fail",
                            "EchoConjurer: Failed to consume Life Essence after availability check");
                    return;
                }
            }

            // Spawn NPC on the world execution thread
            final String activeId = comp.npcIds.get(Math.floorMod(comp.selectedIndex, comp.npcIds.size()));
            final Store<EntityStore> entityStore = world.getEntityStore().getStore();
            final Vector3d finalSpawnPos = spawnPos;
            world.execute(() -> NPCPlugin.get().spawnNPC(entityStore, activeId, null, finalSpawnPos, Vector3f.ZERO));
        }

        private static int countMatchingNpcsInRange(Store<EntityStore> entityStore, Vector3d center,
                                                    double radius, String activeId) {
            SpatialResource<Ref<EntityStore>, EntityStore> npcSpatialResource = entityStore.getResource(NPCPlugin.get().getNpcSpatialResource());
            List<Ref<EntityStore>> results = SpatialResource.getThreadLocalReferenceList();
            npcSpatialResource.getSpatialStructure().collect(center, radius, results);

            int count = 0;
            for (Ref<EntityStore> ref : results) {
                NPCEntity npc = entityStore.getComponent(ref, NPCEntity.getComponentType());
                if (npc == null)
                    continue;
                if (activeId.equals(npc.getNPCTypeId()))
                    count++;
            }
            return count;
        }

        private static int countItemsInContainer(ItemContainer container, String itemId) {
            int count = 0;
            for (short i = 0; i < container.getCapacity(); ++i) {
                ItemStack stack = container.getItemStack(i);
                if (stack == null || stack.isEmpty())
                    continue;
                if (itemId.equals(stack.getItemId()))
                    count += stack.getQuantity();
            }
            return count;
        }

        @Nullable
        private static Vector3d pickGroundAlignedPosition(World world, Vector3i blockPos, double radius) {
            double theta = RANDOM.nextDouble() * Math.PI * 2.0;
            double r = Math.sqrt(RANDOM.nextDouble()) * radius; // uniform over disk
            double x = blockPos.x + 0.5 + r * Math.cos(theta);
            double z = blockPos.z + 0.5 + r * Math.sin(theta);
            int probeX = (int) Math.floor(x);
            int probeZ = (int) Math.floor(z);

            // Walk downward: spawn on top of first solid block whose cell above is air
            int startY = blockPos.y + PROBE_HEIGHT_ABOVE;
            int endY = blockPos.y - PROBE_DEPTH;
            boolean prevSolid = isSolidAt(world, probeX, startY + 1, probeZ);
            for (int y = startY; y >= endY; --y) {
                boolean solid = isSolidAt(world, probeX, y, probeZ);
                if (solid && !prevSolid) {
                    DebugHelper.Print("EchoConjurer: Found spawn pos at (" + probeX + ", " + y + ", " + probeZ + ")");
                    return new Vector3d(x, y + 1, z);
                }
                prevSolid = solid;
            }
            return null;
        }

        private static boolean isSolidAt(World world, int x, int y, int z) {
            BlockType blockType = world.getBlockType(new Vector3i(x, y, z));
            if (blockType == null)
                return false;

            String id = blockType.getId();
            return id != null && !"Empty".equalsIgnoreCase(id);
        }

        @NullableDecl
        @Override
        public Query<ChunkStore> getQuery() {
            return Query.and(EchoConjurerComponent.getComponentType(), EssenceStorageComponent.getComponentType());
        }
    }

    public static class BreakSystem extends EntityEventSystem<EntityStore, BreakBlockEvent> {

        public BreakSystem() {
            super(BreakBlockEvent.class);
        }

        @Override
        public void handle(int i,
                           @Nonnull ArchetypeChunk<EntityStore> archetypeChunk,
                           @Nonnull Store<EntityStore> store,
                           @Nonnull CommandBuffer<EntityStore> commandBuffer,
                           @Nonnull BreakBlockEvent breakBlockEvent) {
            Vector3i pos = breakBlockEvent.getTargetBlock();
            World world = commandBuffer.getExternalData().getWorld();

            EchoConjurerComponent comp = ComponentHelper.findComponentAt(world, pos, EchoConjurerComponent.getComponentType());
            if (comp == null)
                return;

            Vector3d dropPos = pos.toVector3d();
            dropContainerItems(commandBuffer, dropPos, comp.lifeEssenceContainer);
        }

        private void dropContainerItems(CommandBuffer<EntityStore> commandBuffer, Vector3d pos, ItemContainer container) {
            List<ItemStack> items = container.dropAllItemStacks();
            Holder<EntityStore>[] holders = ItemComponent.generateItemDrops(commandBuffer, items, pos, Vector3f.ZERO);
            if (holders.length > 0)
                commandBuffer.addEntities(holders, AddReason.SPAWN);
        }

        @Nullable
        @Override
        public Query<EntityStore> getQuery() {
            return PlayerRef.getComponentType();
        }
    }
}
