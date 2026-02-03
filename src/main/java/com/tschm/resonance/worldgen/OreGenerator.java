package com.tschm.resonance.worldgen;

import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.universe.world.chunk.section.BlockSection;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public final class OreGenerator {

    /** Unmodifiable map: rock -> coal ore */
    public static final Map<Integer, Integer> ROCK_TO_ORE;

    private OreGenerator() {
        // no instances
    }

    public static void createBidirectionalMappings(
            Map<Integer, Integer> oreToRock,
            Map<Integer, Integer> rockToOre
    ) {
        putBidirectional(oreToRock, rockToOre, "Echo_Stone", "Rock_Stone");
        putBidirectional(oreToRock, rockToOre, "Echo_Stone", "Rock_Basalt");
        putBidirectional(oreToRock, rockToOre, "Echo_Stone", "Rock_Sandstone");
        putBidirectional(oreToRock, rockToOre, "Echo_Stone", "Rock_Shale");
        putBidirectional(oreToRock, rockToOre, "Echo_Stone", "Rock_Volcanic");
    }

    public static int generateEchoStoneInSection(
            BlockSection section,
            Map<Integer, Integer> mapping,
            Random random,
            double density
    ) {
        int placed = 0;
        int attempts = (int) (density * (double) 32768.0F);

        for (int i = 0; i < attempts; ++i) {
            int index = random.nextInt(32768);
            int currentBlock = section.get(index);

            Integer replacement = mapping.get(currentBlock);
            if (replacement != null) {
                section.set(index, replacement, 0, 0);
                ++placed;

                int extra = 1 + random.nextInt(2);
                for (int j = 0; j < extra; ++j) {
                    int neighborIndex = index + random.nextInt(3) - 1;
                    if (neighborIndex >= 0 && neighborIndex < 32768) {
                        int neighborBlock = section.get(neighborIndex);
                        Integer neighborReplacement = mapping.get(neighborBlock);
                        if (neighborReplacement != null) {
                            section.set(neighborIndex, neighborReplacement, 0, 0);
                            ++placed;
                        }
                    }
                }
            }
        }

        return placed;
    }

    public static boolean isSectionInRange(int sectionY, int minY, int maxY) {
        int y = sectionY << 5;
        return y + 32 >= minY && y <= maxY;
    }

    public static long createSectionSeed(long worldSeed, int sectionId) {
        return worldSeed * 341873128712L + (long) sectionId * 132897987541L;
    }

    private static void putOneWay(Map<Integer, Integer> map, String fromBlockName, String toBlockName) {
        int from = BlockType.getAssetMap().getIndex(fromBlockName);
        int to = BlockType.getAssetMap().getIndex(toBlockName);

        if (from != Integer.MIN_VALUE && to != Integer.MIN_VALUE) {
            map.put(from, to);
        }
    }

    private static void putBidirectional(
            Map<Integer, Integer> forward,
            Map<Integer, Integer> backward,
            String forwardKeyName,
            String forwardValueName
    ) {
        int key = BlockType.getAssetMap().getIndex(forwardKeyName);
        int value = BlockType.getAssetMap().getIndex(forwardValueName);

        if (key != Integer.MIN_VALUE && value != Integer.MIN_VALUE) {
            forward.put(key, value);
            backward.put(value, key);
        }
    }

    static {
        HashMap<Integer, Integer> map = new HashMap<>();
        putOneWay(map, "Rock_Stone", "Echo_Stone");
        putOneWay(map, "Rock_Basalt", "Echo_Stone");
        putOneWay(map, "Rock_Sandstone", "Echo_Stone");
        putOneWay(map, "Rock_Shale", "Echo_Stone");
        putOneWay(map, "Rock_Volcanic", "Echo_Stone");
        ROCK_TO_ORE = Collections.unmodifiableMap(map);
    }
}
