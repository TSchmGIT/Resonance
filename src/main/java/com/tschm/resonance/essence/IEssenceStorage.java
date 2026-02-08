package com.tschm.resonance.essence;

// CREDITS TO killer.Essencestorage mod
// https://github.com/zkiller/HytaleEssenceStorage
// https://www.curseforge.com/hytale/mods/Essencestorage
public interface IEssenceStorage {
    long getEssenceStored();

    long getMaxEssenceStored();

    long getMaxReceive();

    long getMaxExtract();

    boolean canReceive();

    boolean canExtract();

    long receiveEssence(long paramLong, boolean paramBoolean);

    long receiveEssenceInternal(long paramLong, boolean paramBoolean);

    long extractEssence(long paramLong, boolean paramBoolean);

    long extractEssenceInternal(long paramLong, boolean paramBoolean);

    void setEssenceStored(long paramLong);

    void setCapacityStored(long paramLong);

    void addEssence(long paramLong);

    float getFillRatio();

    boolean isFull();

    boolean isEmpty();
}
