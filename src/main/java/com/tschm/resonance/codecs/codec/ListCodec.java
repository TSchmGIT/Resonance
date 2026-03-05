package com.tschm.resonance.codecs.codec;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.ExtraInfo;
import com.hypixel.hytale.codec.WrappedCodec;
import com.hypixel.hytale.codec.exception.CodecException;
import com.hypixel.hytale.codec.schema.SchemaContext;
import com.hypixel.hytale.codec.schema.config.ArraySchema;
import com.hypixel.hytale.codec.schema.config.Schema;
import com.hypixel.hytale.codec.util.RawJsonReader;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;
import javax.annotation.Nonnull;
import org.bson.BsonArray;
import org.bson.BsonValue;

public class ListCodec<V, L extends List<V>> implements Codec<List<V>>, WrappedCodec<V> {
    private final Codec<V> codec;
    private final Supplier<L> supplier;
    private final boolean unmodifiable;

    public ListCodec(Codec<V> codec, Supplier<L> supplier, boolean unmodifiable) {
        this.codec = codec;
        this.supplier = supplier;
        this.unmodifiable = unmodifiable;
    }

    @Override
    public List<V> decode(@Nonnull BsonValue bsonValue, @Nonnull ExtraInfo extraInfo) {
        BsonArray list = bsonValue.asArray();
        if (list.isEmpty()) return this.unmodifiable ? Collections.emptyList() : this.supplier.get();
        L out = this.supplier.get();
        for (int i = 0; i < list.size(); i++) {
            extraInfo.pushIntKey(i);
            try { out.add(this.codec.decode(list.get(i), extraInfo)); }
            catch (Exception e) { throw new CodecException("Failed to decode", list.get(i), extraInfo, e); }
            finally { extraInfo.popKey(); }
        }
        return this.unmodifiable ? Collections.unmodifiableList(out) : out;
    }

    @Override
    public List<V> decodeJson(@Nonnull RawJsonReader reader, @Nonnull ExtraInfo extraInfo) throws IOException {
        reader.expect('[');
        reader.consumeWhiteSpace();
        if (reader.tryConsume(']')) return this.unmodifiable ? Collections.emptyList() : this.supplier.get();
        int i = 0;
        L out = this.supplier.get();
        while (true) {
            extraInfo.pushIntKey(i, reader);
            try { out.add(this.codec.decodeJson(reader, extraInfo)); i++; }
            catch (Exception e) { throw new CodecException("Failed to decode", reader, extraInfo, e); }
            finally { extraInfo.popKey(); }
            reader.consumeWhiteSpace();
            if (reader.tryConsumeOrExpect(']', ','))
                return this.unmodifiable ? Collections.unmodifiableList(out) : out;
            reader.consumeWhiteSpace();
        }
    }

    @Nonnull
    @Override
    public BsonValue encode(@Nonnull List<V> vs, @Nonnull ExtraInfo extraInfo) {
        BsonArray out = new BsonArray();
        int key = 0;
        for (V v : vs) {
            extraInfo.pushIntKey(key++);
            try { out.add(this.codec.encode(v, extraInfo)); }
            finally { extraInfo.popKey(); }
        }
        return out;
    }

    @Nonnull
    @Override
    public Schema toSchema(@Nonnull SchemaContext context) {
        ArraySchema schema = new ArraySchema();
        schema.setTitle("List");
        schema.setItem(context.refDefinition(this.codec));
        return schema;
    }

    @Override
    public Codec<V> getChildCodec() { return this.codec; }
}