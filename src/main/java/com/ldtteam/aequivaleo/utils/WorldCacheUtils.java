package com.ldtteam.aequivaleo.utils;

import com.ldtteam.aequivaleo.Aequivaleo;
import com.ldtteam.aequivaleo.api.compound.CompoundInstance;
import com.ldtteam.aequivaleo.api.compound.container.ICompoundContainer;
import com.ldtteam.aequivaleo.api.util.Constants;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.minecraft.network.PacketBuffer;
import net.minecraft.world.server.ServerWorld;
import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

public class WorldCacheUtils
{

    private static final Logger LOGGER = LogManager.getLogger();
    private WorldCacheUtils()
    {
        throw new IllegalStateException("Can not instantiate an instance of: WorldCacheUtils. This is a utility class");
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public static void writeCachedResults(final ServerWorld world, final int id, final Map<ICompoundContainer<?>, Set<CompoundInstance>> data) {
        final File aequivaleoDirectory = new File(world.getChunkProvider().chunkManager.dimensionDirectory, Constants.MOD_ID);
        final File cacheDirectory = new File(aequivaleoDirectory, "cache");
        final File worldCacheDirectory = new File(cacheDirectory,
          String.format("%s_%s", world.getDimensionKey().getLocation().getNamespace(), world.getDimensionKey().getLocation().getPath()));
        final File cacheFile = new File(worldCacheDirectory, String.format("%d.bin-cache", id));

        worldCacheDirectory.mkdirs();

        try
        {
            if (cacheFile.exists())
                cacheFile.delete();

            cacheFile.createNewFile();
        }
        catch (IOException e)
        {
            LOGGER.fatal(String.format("Failed to create cache file: %s", cacheFile.getAbsolutePath()), e);
            return;
        }

        final ByteBuf buf = Unpooled.buffer();
        final PacketBuffer buffer = new PacketBuffer(buf);
        final List<Map.Entry<ICompoundContainer<?>, Set<CompoundInstance>>> dataToWrite = new ArrayList<>(data.entrySet());

        IOUtils.writeCompoundDataEntries(buffer, dataToWrite);
        try {
            FileUtils.writeByteArrayToFile(cacheFile, buf.array());
        }
        catch (FileNotFoundException e) {
            LOGGER.fatal(String.format("Failed to find cache file:%s", cacheFile.getAbsolutePath()), e);
        }
        catch (IOException ioe) {
            LOGGER.fatal(String.format("Exception while writing cache file: %s", cacheFile.getAbsolutePath()), ioe);
        }

        cleanupCacheDirectory(world);
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public static void cleanupCacheDirectory(final ServerWorld serverWorld) {
        final File aequivaleoDirectory = new File(serverWorld.getChunkProvider().chunkManager.dimensionDirectory, Constants.MOD_ID);
        final File cacheDirectory = new File(aequivaleoDirectory, "cache");
        final File worldCacheDirectory = new File(cacheDirectory,
          String.format("%s_%s", serverWorld.getDimensionKey().getLocation().getNamespace(), serverWorld.getDimensionKey().getLocation().getPath()));

        if (!worldCacheDirectory.exists())
            return;

        final List<File> cacheFiles = Arrays.asList(Objects.requireNonNull(worldCacheDirectory.listFiles()));

        if (cacheFiles.size() > Aequivaleo.getInstance().getConfiguration().getServer().maxCacheFilesToKeep.get()) {
            cacheFiles
              .stream()
              .sorted(Comparator.comparing(File::lastModified).reversed())
              .skip(Aequivaleo.getInstance().getConfiguration().getServer().maxCacheFilesToKeep.get())
              .forEach(File::delete);
        }
    }

    @NotNull
    @SuppressWarnings("ResultOfMethodCallIgnored")
    public static Optional<Map<ICompoundContainer<?>, Set<CompoundInstance>>> loadCachedResults(final ServerWorld world, final int id) {
        final File aequivaleoDirectory = new File(world.getChunkProvider().chunkManager.dimensionDirectory, Constants.MOD_ID);
        final File cacheDirectory = new File(aequivaleoDirectory, "cache");
        final File worldCacheDirectory = new File(cacheDirectory,
          String.format("%s_%s", world.getDimensionKey().getLocation().getNamespace(), world.getDimensionKey().getLocation().getPath()));
        final File cacheFile = new File(worldCacheDirectory, String.format("%d.bin-cache", id));

        worldCacheDirectory.mkdirs();

        if (!cacheFile.exists())
            return Optional.empty();

        byte[] data = new byte[0];
        try {
            data = FileUtils.readFileToByteArray(cacheFile);
        }
        catch (FileNotFoundException e) {
            LOGGER.fatal(String.format("Failed to find cache file:%s", cacheFile.getAbsolutePath()), e);
        }
        catch (IOException ioe) {
            LOGGER.fatal(String.format("Exception while reading cache file: %s", cacheFile.getAbsolutePath()), ioe);
        }

        if (data.length == 0)
            return Optional.empty();

        final ByteBuf buf = Unpooled.wrappedBuffer(data);
        final PacketBuffer buffer = new PacketBuffer(buf);

        final List<Map.Entry<ICompoundContainer<?>, Set<CompoundInstance>>> resultData = new ArrayList<>();
        try {
            IOUtils.readCompoundData(buffer, resultData);
            buf.release();
        }
        catch (Exception exception) {
            LOGGER.fatal(String.format("Exception while reading cache data: %s", cacheFile.getAbsolutePath()), exception);
            return Optional.empty();
        }

        return Optional.of(
          resultData
            .stream()
            .collect(Collectors.toMap(
              Map.Entry::getKey,
              Map.Entry::getValue
            ))
        );
    }
}
