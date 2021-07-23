package com.ldtteam.aequivaleo.api.compound.container.dummy;

import com.google.gson.JsonObject;
import com.ldtteam.aequivaleo.api.compound.container.ICompoundContainer;
import org.apache.commons.lang3.Validate;
import org.jetbrains.annotations.NotNull;

/**
 * Represents a unknown type.
 * Will contain the original JsonData in case of recovery.
 */
public class Dummy implements ICompoundContainer<Dummy>
{

    @NotNull
    private final JsonObject originalData;

    public Dummy(@NotNull final JsonObject originalData) {
        this.originalData = Validate.notNull(originalData);
    }

    @Override
    public boolean isValid()
    {
        return false;
    }

    @Override
    public Dummy getContents()
    {
        return this;
    }

    @Override
    public Double getContentsCount()
    {
        return 0d;
    }

    @Override
    public boolean canBeLoadedFromDisk()
    {
        return false;
    }

    @Override
    public String getContentAsFileName()
    {
        throw new IllegalStateException("Tried to access the file name for the container. Container does not support.");
    }

    @NotNull
    public JsonObject getOriginalData()
    {
        return originalData;
    }

    @Override
    public int compareTo(@NotNull final ICompoundContainer<?> o)
    {
        if (!(o instanceof Dummy))
        {
            //If it is not a dummy then we say we are greater. Dummies end up last in the list.
            return 1;
        }

        final Dummy d = (Dummy) o;

        //Now we can compare the data stored inside.
        return originalData.toString().compareTo(d.originalData.toString());
    }

    @Override
    public boolean equals(final Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (!(o instanceof Dummy))
        {
            return false;
        }

        final Dummy dummy = (Dummy) o;

        return originalData.toString().equals(dummy.originalData.toString());
    }

    @Override
    public int hashCode()
    {
        return originalData.toString().hashCode();
    }
}
