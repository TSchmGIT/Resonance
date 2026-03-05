package com.tschm.resonance.components.essence;

import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.validation.Validators;

// CREDITS TO killer.Essencestorage mod
// https://github.com/zkiller/HytaleEssenceStorage
// https://www.curseforge.com/hytale/mods/Essencestorage
public abstract class AbstractEssenceStorage implements IEssenceStorage {
    public static final BuilderCodec<AbstractEssenceStorage> CODEC;

    /**
     * Transfers a specified amount of essence from one storage to another.
     * Validates the transfer based on the capabilities and capacities of the
     * source and target essence storages. Can optionally simulate the transfer
     * without performing it.
     *
     * @param from     The source essence storage from which essence will be extracted.
     * @param to       The target essence storage to which essence will be transferred.
     * @param amount   The amount of essence to transfer.
     * @param simulate If true, the transfer will only be simulated and no actual
     *                 changes will be made to the storages.
     * @return {@code true} if the transfer is possible (or would be possible in
     * simulation mode), {@code false} otherwise.
     */
    public static long transferEssence(IEssenceStorage from, IEssenceStorage to, long amount, boolean simulate) {
        final long possibleExtracted = from.removeEssence(amount, true);
        final long possibleInserted = to.addEssence(possibleExtracted, true);

        if (!simulate) {
            final long extracted = from.removeEssence(possibleInserted, false);
            final long inserted = to.addEssence(possibleInserted, false);
            assert extracted == possibleInserted && inserted == possibleInserted;
        }

        return possibleInserted;
    }


    protected long essenceStored = 0L;

    protected long maxEssence = Long.MAX_VALUE;

    protected long maxReceive = Long.MAX_VALUE;

    protected long maxExtract = Long.MAX_VALUE;

    protected AbstractEssenceStorage() {
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

    public void setMaxReceive(long maxReceive) {
        this.maxReceive = maxReceive;
    }

    public long getMaxExtract() {
        return this.maxExtract;
    }

    public void setMaxExtract(long maxExtract) {
        this.maxExtract = maxExtract;
    }

    public boolean canReceive() {
        return (this.maxReceive > 0L && this.essenceStored < this.maxEssence);
    }

    public boolean canExtract() {
        return (this.maxExtract > 0L && this.essenceStored > 0L);
    }

    public long addEssence(long amount, boolean simulate) {
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

    public long removeEssence(long amount, boolean simulate) {
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
        return (float) this.essenceStored / (float) this.maxEssence;
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

    static {
        CODEC = BuilderCodec.abstractBuilder(AbstractEssenceStorage.class)
                .append(new KeyedCodec<>("EssenceStored", BuilderCodec.LONG), (c, v) -> c.essenceStored = v, c -> c.essenceStored)
                .addValidator(Validators.greaterThanOrEqual(0L))
                .documentation("Currently stored RE").add()
                .append(new KeyedCodec<>("MaxEssence", BuilderCodec.LONG), (c, v) -> c.maxEssence = v, c -> c.maxEssence)
                .addValidator(Validators.greaterThanOrEqual(0L))
                .documentation("Maximum amount of stored RE possible").add()
                .append(new KeyedCodec<>("MaxReceive", BuilderCodec.LONG), (c, v) -> c.maxReceive = v, c -> c.maxReceive)
                .addValidator(Validators.greaterThanOrEqual(0L))
                .documentation("Maximum RE received per operation").add()
                .append(new KeyedCodec<>("MaxExtract", BuilderCodec.LONG), (c, v) -> c.maxExtract = v, c -> c.maxExtract)
                .addValidator(Validators.greaterThanOrEqual(0L))
                .documentation("Maximum RE extracted per operation").add()
                .build();
    }
}
