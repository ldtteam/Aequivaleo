package com.ldtteam.aequivaleo.utils;

import com.ldtteam.aequivaleo.analysis.IAnalysisOwner;

import java.util.List;
import java.util.stream.Collectors;

public class WorldUtils
{

    private WorldUtils()
    {
        throw new IllegalStateException("Can not instantiate an instance of: WorldUtils. This is a utility class");
    }

    public static String formatWorldNames(final List<? extends IAnalysisOwner> worldList) {
        return "[" + worldList.stream().map(world -> world.getIdentifier().location().toString()).collect(Collectors.joining(", ")) + "]";
    }
}
