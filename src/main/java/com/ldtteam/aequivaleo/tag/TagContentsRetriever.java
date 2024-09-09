package com.ldtteam.aequivaleo.tag;

import com.ldtteam.aequivaleo.api.compound.container.ICompoundContainer;
import com.ldtteam.aequivaleo.api.tag.ITagContentsRetriever;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.fluids.FluidStack;

import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.BiFunction;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class TagContentsRetriever implements ITagContentsRetriever {

    private static final TagContentsRetriever INSTANCE = new TagContentsRetriever();

    public static TagContentsRetriever getInstance() {
        return INSTANCE;
    }

    private final Queue<Handler<?>> handlers = new ConcurrentLinkedQueue<>();

    private TagContentsRetriever() {
        this.<Item>registerHandler(
                tagKey -> tagKey.isFor(Registries.ITEM),
                (access, itemTagKey) -> access.registryOrThrow(Registries.ITEM).getOrCreateTag(itemTagKey)
                        .stream()
                        .map(item -> item.get().getDefaultInstance())
                        .map(ICompoundContainer::from)
                        .collect(Collectors.toSet())
        );

        this.<Fluid>registerHandler(
                tagKey -> tagKey.isFor(Registries.FLUID),
                (access, fluidTagKey) -> access.registryOrThrow(Registries.FLUID).getOrCreateTag(fluidTagKey)
                        .stream()
                        .map(fluid -> ICompoundContainer.from(new FluidStack(fluid.get(), 1)))
                        .collect(Collectors.toSet())
        );
    }

    @Override
    public <T> void registerHandler(Predicate<TagKey<T>> selector, BiFunction<RegistryAccess, TagKey<T>, Set<ICompoundContainer<?>>> retriever) {
        handlers.add(new Handler<>(selector, retriever));
    }

    public Set<ICompoundContainer<?>> getContents(RegistryAccess access, TagKey<?> tag) {
        return handlers.stream()
                .filter(handler -> handler.matches(tag))
                .findFirst()
                .map(handler -> handler.apply(access, tag))
                .orElse(Set.of());
    }

    private record Handler<T>(Predicate<TagKey<T>> selector, BiFunction<RegistryAccess, TagKey<T>, Set<ICompoundContainer<?>>> retriever) {
        @SuppressWarnings("unchecked")
        public boolean matches(TagKey<?> tag) {
            try {
                return selector.test((TagKey<T>) tag);
            } catch (ClassCastException e) {
                return false;
            }
        }

        @SuppressWarnings("unchecked")
        public Set<ICompoundContainer<?>> apply(RegistryAccess access, TagKey<?> tag) {
            return retriever.apply(access, (TagKey<T>) tag);
        }
    }
}
