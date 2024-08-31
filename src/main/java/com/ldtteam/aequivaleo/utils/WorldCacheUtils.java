package com.ldtteam.aequivaleo.utils;

import com.ldtteam.aequivaleo.Aequivaleo;
import com.ldtteam.aequivaleo.analysis.IAnalysisOwner;
import com.ldtteam.aequivaleo.api.compound.CompoundInstance;
import com.ldtteam.aequivaleo.api.compound.container.ICompoundContainer;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.fml.loading.FMLLoader;
import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
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
    public static void writeCachedResults(final IAnalysisOwner analysisOwner, final String id, final Map<ICompoundContainer<?>, Set<CompoundInstance>> data) {
        final File worldCacheDirectory = analysisOwner.getCacheDirectory();
        final File cacheFile = new File(worldCacheDirectory, String.format("%s.bin-cache", id));

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
        final FriendlyByteBuf buffer = new FriendlyByteBuf(buf);
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

        cleanupCacheDirectory(analysisOwner);
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public static void cleanupCacheDirectory(final IAnalysisOwner analysisOwner) {
        final File worldCacheDirectory = analysisOwner.getCacheDirectory();
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
    public static Optional<Map<ICompoundContainer<?>, Set<CompoundInstance>>> loadCachedResults(final IAnalysisOwner analysisOwner, final String id) {
        final File worldCacheDirectory = analysisOwner.getCacheDirectory();
        File cacheFile = new File(worldCacheDirectory, String.format("%s.bin-cache", id));

        worldCacheDirectory.mkdirs();

        if (!cacheFile.exists()) {
            LOGGER.info("No world cache file found for: {}", cacheFile.getAbsolutePath());
            cacheFile = new File(FMLLoader.getGamePath().toFile(), String.format("aequivaleo/cache/%s.bin-cache", id));

            if (!cacheFile.exists()) {
                LOGGER.info("No global cache file found for: {}", cacheFile.getAbsolutePath());
                return Optional.empty();
            } else {
                LOGGER.info("Loading from global cache file: {}", cacheFile.getAbsolutePath());
            }
        }

        LOGGER.info("Loading cache file: {}", cacheFile.getAbsolutePath());

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

        if (data.length == 0) {
            LOGGER.error("Cache data is empty: {}", cacheFile.getAbsolutePath());
            return Optional.empty();
        }

        final ByteBuf buf = Unpooled.wrappedBuffer(data);
        final FriendlyByteBuf buffer = new FriendlyByteBuf(buf);

        final List<Map.Entry<ICompoundContainer<?>, Set<CompoundInstance>>> resultData = new ArrayList<>();
        try {
            IOUtils.readCompoundData(buffer, resultData);
            buf.release();
        }
        catch (Exception exception) {
            LOGGER.fatal(String.format("Exception while reading cache data: %s", cacheFile.getAbsolutePath()), exception);
            return Optional.empty();
        }

        LOGGER.info("Loaded {} cache entries from: {}", resultData.size(), cacheFile.getAbsolutePath());

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
