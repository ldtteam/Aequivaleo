package com.ldtteam.aequivaleo.api.util;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.ldtteam.aequivaleo.api.compound.container.ICompoundContainer;
import com.mojang.serialization.Codec;
import com.mojang.serialization.Decoder;
import org.checkerframework.checker.units.qual.A;

import java.util.*;
import java.util.function.Function;

/**
 * Utility class for extra codecs.
 */
public class AequivaleoExtraCodecs {
    
    private AequivaleoExtraCodecs() {
        throw new IllegalStateException("Tried to create utility class!");
    }

    /**
     * Creates a codec for a sorted set of elements that uses the given codec for the elements.
     *
     * @param codec The codec for the elements.
     * @return The codec for the sorted set.
     * @param <A> The type of the elements.
     */
    public static <A extends Comparable<A>> Codec<SortedSet<A>> sortedSetOf(Codec<A> codec) {
        return codec.listOf().xmap(Sets::newTreeSet, ArrayList::new);
    }

    /**
     * Creates a codec for a set of elements that uses the given codec for the elements.
     *
     * @param codec The codec for the elements.
     * @return The codec for the set.
     * @param <A> The type of the elements.
     */
    public static <A> Codec<Set<A>> setOf(Codec<A> codec) {
        return codec.listOf().xmap(Sets::newHashSet, ArrayList::new);
    }

    /**
     * Creates a codec for a bi-map of elements that uses the given codecs for the keys and values.
     *
     * @param keyCodec The codec for the keys.
     * @param valueCodec The codec for the values.
     * @return The codec for the list.
     * @param <A> The type of the keys.
     * @param <B> The type of the values.
     */
    public static <A, B> Codec<BiMap<A, B>> bimapOf(Codec<A> keyCodec, Codec<B> valueCodec) {
        return Codec.unboundedMap(keyCodec, valueCodec).xmap(HashBiMap::create, abBiMap -> abBiMap);
    }
}
