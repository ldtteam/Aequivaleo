package com.ldtteam.aequivaleo.data;

import com.ldtteam.aequivaleo.api.tags.Tags;
import com.ldtteam.aequivaleo.api.util.Constants;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.ItemTagsProvider;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.data.event.GatherDataEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

@Mod.EventBusSubscriber(modid = Constants.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ItemTagProvider extends ItemTagsProvider {

    @SubscribeEvent
    public static void onGatherData(GatherDataEvent event) {
        final BlockTagProvider blockTagProvider = new BlockTagProvider(
                event.getGenerator().getPackOutput(),
                event.getLookupProvider(),
                event.getExistingFileHelper()
        );

        event.getGenerator().addProvider(
                event.includeServer(),
                blockTagProvider
        );

        event.getGenerator().addProvider(
                event.includeServer(),
                new ItemTagProvider(
                        event.getGenerator().getPackOutput(),
                        event.getLookupProvider(),
                        blockTagProvider.contentsGetter(),
                        event.getExistingFileHelper()
                        )
                );
    }

    public ItemTagProvider(PackOutput packOutput, CompletableFuture<HolderLookup.Provider> providerFuture, CompletableFuture<TagLookup<Block>> tagLookupFuture, @Nullable ExistingFileHelper existingFileHelper) {
        super(packOutput, providerFuture, tagLookupFuture, Constants.MOD_ID, existingFileHelper);
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void addTags(HolderLookup.@NotNull Provider provider) {
        tag(Tags.Items.BLOCKED_COLOR_CYCLE).addTags(
                ItemTags.BEDS,
                ItemTags.WOOL_CARPETS,
                ItemTags.WOOL_CARPETS
        );
    }
}
