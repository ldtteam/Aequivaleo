package com.ldtteam.aequivaleo.api.compound.container.registry;

import com.google.gson.JsonDeserializer;
import com.google.gson.JsonSerializer;
import com.ldtteam.aequivaleo.api.IAequivaleoAPI;
import com.ldtteam.aequivaleo.api.compound.container.ICompoundContainer;
import com.ldtteam.aequivaleo.api.compound.container.factory.ICompoundContainerFactory;
import com.ldtteam.aequivaleo.api.util.IPacketBufferSerializer;
import net.minecraftforge.registries.IForgeRegistry;
import org.jetbrains.annotations.NotNull;

/**
 * A manager type for factories that handle wrapping compound containers, like ItemStacks.
 * Making this a manager with {@link IForgeRegistry} combination instead of hardcoding allows for easier expansion of the compound system into new and several types, like entities and power.
 */
public interface ICompoundContainerFactoryManager extends JsonDeserializer<ICompoundContainer<?>>, JsonSerializer<ICompoundContainer<?>>, IPacketBufferSerializer<ICompoundContainer<?>>
{
    /**
     * Gives access to the current instance of the factory registry.
     *
     * @return The factory registry.
     */
    static ICompoundContainerFactoryManager getInstance() {
        return IAequivaleoAPI.getInstance().getCompoundContainerFactoryManager();
    }

    /**
     * The forge registry for the factories.
     *
     * @return The registry.
     */
    IForgeRegistry<ICompoundContainerFactory<?>> getRegistry();

    /**
     * Utility method to check if a given class of a compound container can be wrapped properly.
     *
     * @param inputType The class to check
     * @param <T>   The type of the compound container to check.
     * @return True when a factory for type T is registered false when not.
     */
    <T> boolean canBeWrapped(@NotNull Class<T> inputType);

    /**
     * Utility method to check if a given instance of a possible compound container can be wrapped properly.
     *
     * @param gameObject The instance to check
     * @param <T>       The type of the compound container to check.
     * @return True when a factory for type T is registered false when not.
     */
    <T> boolean canBeWrapped(@NotNull T gameObject);


    /**
     * Wraps the given compound container.
     *
     * @param gameObject The instance of T to wrap. Will be brought to unit length by the factory (ItemStacks will be copied and have stack size 1).
     * @param count     The count to store in the container. For ItemStacks this will be the stacks size.
     * @param <T>       The type of the compound container to create.
     * @return The wrapped instance.
     * @throws IllegalArgumentException When T can not be wrapped properly {@code canBeWrapped(tInstance) == false;}
     */
    @NotNull
    <T> ICompoundContainer<T> wrapInContainer(@NotNull T gameObject, @NotNull double count) throws IllegalArgumentException;
}