package com.ldtteam.aequivaleo.utils;

import net.minecraft.world.level.Level;

import java.util.List;
import java.util.stream.Collectors;

public class WorldUtils
{

    private WorldUtils()
    {
        throw new IllegalStateException("Can not instantiate an instance of: WorldUtils. This is a utility class");
    }

    public static String formatWorldNames(final List<? extends Level> worldList) {
        return "[" + worldList.stream().map(world -> world.dimension().location().toString()).collect(Collectors.joining(", ")) + "]";
    }
}
