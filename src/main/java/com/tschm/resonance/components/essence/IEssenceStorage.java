package com.tschm.resonance.components.essence;

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

    long addEssence(long paramLong, boolean paramBoolean);

    long receiveEssenceInternal(long paramLong, boolean paramBoolean);

    long removeEssence(long paramLong, boolean paramBoolean);

    long extractEssenceInternal(long paramLong, boolean paramBoolean);

    void setEssenceStored(long paramLong);

    void setCapacityStored(long paramLong);

    void addEssence(long paramLong);

    float getFillRatio();

    boolean isFull();

    boolean isEmpty();
}
