package com.tschm.resonance.events;

import com.hypixel.hytale.component.Holder;
import com.hypixel.hytale.server.core.universe.world.chunk.ChunkColumn;
import com.hypixel.hytale.server.core.universe.world.chunk.section.BlockSection;
import com.hypixel.hytale.server.core.universe.world.events.ChunkPreLoadProcessEvent;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import com.tschm.resonance.worldgen.OreGenerator;

import java.util.Map;
import java.util.Random;
import java.util.concurrent.CompletableFuture;

public class OreGenChunkEvent {
    public static void onChunkPreLoadProcess(ChunkPreLoadProcessEvent event) {
        if (!event.isNewlyGenerated()) {
            return;
        }

        Holder<ChunkStore> holder = event.getHolder();
        long chunkIndex = event.getChunk().getIndex();

        CompletableFuture.runAsync(() -> {
            try {
                ChunkColumn chunkColumn = (ChunkColumn) holder.getComponent(ChunkColumn.getComponentType());
                if (chunkColumn == null) {
                    return;
                }

                Holder<ChunkStore>[] sectionHolders = chunkColumn.getSectionHolders();
                if (sectionHolders == null) {
                    return;
                }

                Map<Integer, Integer> map = OreGenerator.ROCK_TO_ORE;
                if (map.isEmpty()) {
                    return;
                }

                for (int sectionY = 0; sectionY < sectionHolders.length; sectionY++) {
                    BlockSection blockSection = (BlockSection)sectionHolders[sectionY].ensureAndGetComponent(BlockSection.getComponentType());

                    if (OreGenerator.isSectionInRange(sectionY, 20, 110)) {
                        long sectionSeed = OreGenerator.createSectionSeed(chunkIndex, sectionY);
                        Random random = new Random(sectionSeed);
                        OreGenerator.generateEchoStoneInSection(blockSection, map, random, 0.002D);
                    }
                }
            } catch (Exception exception) {
                System.err.println("[Resonance] Error during async ore generation: " + exception.getMessage());
                exception.printStackTrace();
            }
        });
    }
}
