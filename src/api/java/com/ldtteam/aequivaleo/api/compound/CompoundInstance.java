package com.ldtteam.aequivaleo.api.compound;

import com.google.gson.*;
import com.ldtteam.aequivaleo.api.IAequivaleoAPI;
import com.ldtteam.aequivaleo.api.compound.information.CompoundInstanceRef;
import com.ldtteam.aequivaleo.api.compound.type.ICompoundType;
import net.minecraft.util.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Type;
import java.util.Objects;

public final class CompoundInstance implements Comparable<CompoundInstance>
{
    private final ICompoundType type;
    private final Double        amount;

    public CompoundInstance(final ICompoundType type, final Integer amount) {
        this.type = type;
        this.amount = Double.valueOf(amount);
    }

    public CompoundInstance(final ICompoundType type, final Double amount) {
        this.type = type;
        this.amount = amount;
    }

    /**
     * Returns the type of the instance.
     *
     * @return The type.
     */
    @NotNull
    public ICompoundType getType()
    {
        return type;
    }

    /**
     * Returns the amount stored in this instance.
     *
     * @return The amount.
     */
    @NotNull
    public Double getAmount()
    {
        return amount;
    }

    @Override
    public int compareTo(@NotNull final CompoundInstance o)
    {
        if (o.getType() != getType())
            return getType().compareTo(o.getType());

        return (int) (getAmount() - o.getAmount());
    }

    @Override
    public boolean equals(final Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (!(o instanceof CompoundInstance))
        {
            return false;
        }

        final CompoundInstance that = (CompoundInstance) o;

        if (!getType().equals(that.getType()))
        {
            return false;
        }
        return getAmount().equals(that.getAmount());
    }

    @Override
    public int hashCode()
    {
        int result = getType().hashCode();
        result = 31 * result + getAmount().hashCode();
        return result;
    }

    @Override
    public String toString()
    {
        return "SimpleCompoundInstance{" +
                 "type=" + type +
                 ", amount=" + amount +
                 '}';
    }

    public CompoundInstanceRef asRef() {
        return new CompoundInstanceRef(
          getType().getRegistryName(),
          getAmount()
        );
    }
}
