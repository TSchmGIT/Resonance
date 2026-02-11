package com.tschm.resonance.components.essence;

// CREDITS TO killer.Essencestorage mod
// https://github.com/zkiller/HytaleEssenceStorage
// https://www.curseforge.com/hytale/mods/Essencestorage
public abstract class AbstractEssenceStorage implements IEssenceStorage {
    protected long essenceStored;

    protected long maxEssence;

    protected long maxReceive;

    protected long maxExtract;

    protected AbstractEssenceStorage() {
        this(0L, 10000L, 1000L, 1000L);
    }

    protected AbstractEssenceStorage(long essenceStored, long maxEssence, long maxReceive, long maxExtract) {
        if (maxEssence <= 0L)
            throw new IllegalArgumentException("maxEssence must be > 0");
        if (essenceStored < 0L)
            throw new IllegalArgumentException("essenceStored must be >= 0");
        this.maxEssence = maxEssence;
        this.essenceStored = Math.min(essenceStored, maxEssence);
        this.maxReceive = Math.max(0L, maxReceive);
        this.maxExtract = Math.max(0L, maxExtract);
    }

    public long getEssenceStored() {
        return this.essenceStored;
    }

    public long getMaxEssenceStored() {
        return this.maxEssence;
    }

    public long getMaxReceive() {
        return this.maxReceive;
    }

    public long getMaxExtract() {
        return this.maxExtract;
    }

    public boolean canReceive() {
        return (this.maxReceive > 0L && this.essenceStored < this.maxEssence);
    }

    public boolean canExtract() {
        return (this.maxExtract > 0L && this.essenceStored > 0L);
    }

    public long receiveEssence(long amount, boolean simulate) {
        if (amount <= 0L)
            return 0L;
        if (!canReceive())
            return 0L;
        long received = Math.min(this.maxReceive, amount);
        long space = this.maxEssence - this.essenceStored;
        long inserted = Math.min(space, received);
        if (!simulate && inserted > 0L)
            this.essenceStored += inserted;

        return inserted;
    }

    public long extractEssence(long amount, boolean simulate) {
        if (amount <= 0L)
            return 0L;
        if (!canExtract())
            return 0L;
        long extracted = Math.min(this.maxExtract, amount);
        extracted = Math.min(extracted, this.essenceStored);
        if (!simulate && extracted > 0L)
            this.essenceStored -= extracted;
        return extracted;
    }

    public long receiveEssenceInternal(long amount, boolean simulate) {
        if (amount <= 0L)
            return 0L;
        long space = this.maxEssence - this.essenceStored;
        if (space <= 0L)
            return 0L;
        long inserted = Math.min(space, amount);
        if (!simulate && inserted > 0L)
            this.essenceStored += inserted;
        return inserted;
    }

    public long extractEssenceInternal(long amount, boolean simulate) {
        if (amount <= 0L)
            return 0L;
        if (this.essenceStored <= 0L)
            return 0L;
        long extracted = Math.min(this.essenceStored, amount);
        if (!simulate && extracted > 0L)
            this.essenceStored -= extracted;
        return extracted;
    }

    public void setEssenceStored(long amount) {
        this.essenceStored = Math.min(amount, this.maxEssence);
    }

    public void setCapacityStored(long capacity) {
        this.maxEssence = capacity;
    }

    public void addEssence(long amount) {
        setEssenceStored(getEssenceStored() + amount);
    }

    public float getFillRatio() {
        if (this.maxEssence <= 0L)
            return 0.0F;
        return (float)this.essenceStored / (float)this.maxEssence;
    }

    public boolean isFull() {
        return (this.essenceStored >= this.maxEssence);
    }

    public boolean isEmpty() {
        return (this.essenceStored <= 0L);
    }

    protected void copyFrom(AbstractEssenceStorage other) {
        this.essenceStored = other.essenceStored;
        this.maxEssence = other.maxEssence;
        this.maxReceive = other.maxReceive;
        this.maxExtract = other.maxExtract;
    }
}
