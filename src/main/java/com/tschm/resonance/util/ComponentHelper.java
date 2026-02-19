package com.tschm.resonance.util;

import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.math.block.BlockCubeUtil;
import com.hypixel.hytale.math.util.ChunkUtil;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.modules.block.BlockModule;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.WorldProvider;
import com.hypixel.hytale.server.core.universe.world.chunk.BlockComponentChunk;
import com.hypixel.hytale.server.core.universe.world.chunk.WorldChunk;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import com.tschm.resonance.components.essence.EssenceGeneratorComponent;
import com.tschm.resonance.components.essence.generators.CarbonAttunementStoneComponent;
import com.tschm.resonance.components.essence.generators.SolarAttunementStoneComponent;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ComponentHelper {
    @Nullable
    public static <ECS_TYPE extends WorldProvider, T extends Component<ChunkStore>> T findComponentAt(@Nonnull World world, Vector3i pos, ComponentType<ChunkStore, T> componentType) {
        int x = pos.x;
        int y = pos.y;
        int z = pos.z;

        // Retrieve world chunk
        final long indexChunk = ChunkUtil.indexChunkFromBlock(x, z);
        WorldChunk worldChunk = world.getChunk(indexChunk);
        BlockComponentChunk blockComponentChunk = worldChunk != null ? worldChunk.getBlockComponentChunk() : null;
        if (worldChunk == null || worldChunk.getBlockType(pos) == null || blockComponentChunk == null) {
            return null;
        }

        // Retrieve block component entity at given bock position
        final int blockIndexColumn = ChunkUtil.indexBlockInColumn(x, y, z);
        Ref<ChunkStore> ref = blockComponentChunk.getEntityReference(blockIndexColumn);
        return blockComponentChunk.getComponent(blockIndexColumn, componentType);
    }

    // Work around for inheritance and component types
    @Nullable
    public static <ECS_TYPE extends WorldProvider> EssenceGeneratorComponent findGeneratorComponentAt(@Nonnull WorldProvider worldProvider, Vector3i pos){
        return findGeneratorComponentAt(worldProvider.getWorld(), pos);
    }
    @Nullable
    public static <ECS_TYPE extends WorldProvider> EssenceGeneratorComponent findGeneratorComponentAt(@Nonnull World world, Vector3i pos){
        int x = pos.x;
        int y = pos.y;
        int z = pos.z;

        // Retrieve world chunk
        final long indexChunk = ChunkUtil.indexChunkFromBlock(x, z);
        WorldChunk worldChunk = world.getChunk(indexChunk);
        BlockComponentChunk blockComponentChunk = worldChunk != null ? worldChunk.getBlockComponentChunk() : null;
        if (worldChunk == null || worldChunk.getBlockType(pos) == null || blockComponentChunk == null) {
            return null;
        }

        // Retrieve block component entity at given bock position
        final int blockIndexColumn = ChunkUtil.indexBlockInColumn(x, y, z);

        // Extend with more types in the future
        EssenceGeneratorComponent comp = null;
        comp = blockComponentChunk.getComponent(blockIndexColumn, SolarAttunementStoneComponent.getComponentType());
        comp = comp != null ? comp : blockComponentChunk.getComponent(blockIndexColumn, CarbonAttunementStoneComponent.getComponentType());

        return comp;
    }
}
