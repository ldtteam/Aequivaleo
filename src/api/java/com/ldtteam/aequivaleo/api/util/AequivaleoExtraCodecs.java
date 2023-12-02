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

public class AequivaleoExtraCodecs {
    
    private AequivaleoExtraCodecs() {
        throw new IllegalStateException("Tried to create utility class!");
    }
    
    public static <A extends Comparable<A>> Codec<SortedSet<A>> sortedSetOf(Codec<A> codec) {
        return codec.listOf().xmap(Sets::newTreeSet, ArrayList::new);
    }
    
    public static <A> Codec<Set<A>> setOf(Codec<A> codec) {
        return codec.listOf().xmap(Sets::newHashSet, ArrayList::new);
    }
    
    public static <A, B> Codec<BiMap<A, B>> bimapOf(Codec<A> keyCodec, Codec<B> valueCodec) {
        return Codec.unboundedMap(keyCodec, valueCodec).xmap(HashBiMap::create, abBiMap -> abBiMap);
    }
}
