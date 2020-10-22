package com.ldtteam.aequivaleo.api.compound.information.datagen;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.gson.Gson;
import com.ldtteam.aequivaleo.api.IAequivaleoAPI;
import com.ldtteam.aequivaleo.api.compound.CompoundInstance;
import com.ldtteam.aequivaleo.api.compound.container.ICompoundContainer;
import com.ldtteam.aequivaleo.api.compound.container.registry.ICompoundContainerFactoryManager;
import com.ldtteam.aequivaleo.api.util.Constants;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DirectoryCache;
import net.minecraft.data.IDataProvider;
import net.minecraft.util.ResourceLocation;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public abstract class ValueInformationProvider extends AbstractInformationProvider
{
    public static final String VALUE_PATH = "value";

    private final String modId;
    private final DataGenerator dataGenerator;

    protected ValueInformationProvider(final String modId, final DataGenerator dataGenerator)
    {
        this.modId = modId;
        this.dataGenerator = dataGenerator;
    }

    @Override
    protected Set<Path> getPathsToWrite(String worldPath)
    {
        final Set<Path> pathSet = Sets.newHashSet();
        pathSet.add(dataGenerator.getOutputFolder().resolve(String.format("data/%s/aequivaleo/%s/%s", modId, VALUE_PATH, worldPath)));
        return pathSet;
    }

    @Override
    public String getName()
    {
        return StringUtils.capitalize(modId) + " " + VALUE_PATH + " information data generator.";
    }
}
