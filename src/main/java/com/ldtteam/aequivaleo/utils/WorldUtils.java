package com.ldtteam.aequivaleo.utils;

import net.minecraft.world.World;

import java.util.List;
import java.util.stream.Collectors;

public class WorldUtils
{

    private WorldUtils()
    {
        throw new IllegalStateException("Can not instantiate an instance of: WorldUtils. This is a utility class");
    }

    public static String formatWorldNames(final List<? extends World> worldList) {
        return "[" + worldList.stream().map(world -> world.getDimensionKey().getLocation().toString()).collect(Collectors.joining(", ")) + "]";
    }
}
