package com.ldtteam.aequivaleo.api.compound.container.serialization;

import com.google.gson.JsonDeserializer;
import com.google.gson.JsonSerializer;
import com.ldtteam.aequivaleo.api.compound.container.ICompoundContainer;

/**
 * Represents an object that is capable of serializing a compound container
 * from and into JSON.
 *
 * Extends the {@link JsonSerializer} and {@link JsonDeserializer} for the {@link ICompoundContainer}
 */
public interface ICompoundContainerSerializer<T> extends JsonSerializer<ICompoundContainer<T>>, JsonDeserializer<ICompoundContainer<T>>
{

    /**
     * The type that the serializer can handle when it is contained in a compound container.
     *
     * @return The type that the serializer can handle when it is contained in a compound container.
     */
    Class<T> getType();
}
