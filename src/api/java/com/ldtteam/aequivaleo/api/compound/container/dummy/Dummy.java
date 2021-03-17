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

    /**
     * The contents of this container.
     * Set to the 1 unit of the content type {@link Dummy}
     *
     * @return The contents.
     */
    @Override
    public Dummy getContents()
    {
        return this;
    }

    /**
     * The amount of {@link Dummy}s contained in this wrapper.
     * @return The amount.
     */
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

    /**
     * The originally stored data.
     *
     * @return The originally stored data.
     */
    @NotNull
    public JsonObject getOriginalData()
    {
        return originalData;
    }

    /**
     * Compares this object with the specified object for order.  Returns a
     * negative integer, zero, or a positive integer as this object is less
     * than, equal to, or greater than the specified object.
     *
     * <p>The implementor must ensure <tt>sgn(x.compareTo(y)) ==
     * -sgn(y.compareTo(x))</tt> for all <tt>x</tt> and <tt>y</tt>.  (This
     * implies that <tt>x.compareTo(y)</tt> must throw an exception iff
     * <tt>y.compareTo(x)</tt> throws an exception.)
     *
     * <p>The implementor must also ensure that the relation is transitive:
     * <tt>(x.compareTo(y)&gt;0 &amp;&amp; y.compareTo(z)&gt;0)</tt> implies
     * <tt>x.compareTo(z)&gt;0</tt>.
     *
     * <p>Finally, the implementor must ensure that <tt>x.compareTo(y)==0</tt>
     * implies that <tt>sgn(x.compareTo(z)) == sgn(y.compareTo(z))</tt>, for
     * all <tt>z</tt>.
     *
     * <p>It is strongly recommended, but <i>not</i> strictly required that
     * <tt>(x.compareTo(y)==0) == (x.equals(y))</tt>.  Generally speaking, any
     * class that implements the <tt>Comparable</tt> interface and violates
     * this condition should clearly indicate this fact.  The recommended
     * language is "Note: this class has a natural ordering that is
     * inconsistent with equals."
     *
     * <p>In the foregoing description, the notation
     * <tt>sgn(</tt><i>expression</i><tt>)</tt> designates the mathematical
     * <i>signum</i> function, which is defined to return one of <tt>-1</tt>,
     * <tt>0</tt>, or <tt>1</tt> according to whether the value of
     * <i>expression</i> is negative, zero or positive.
     *
     * @param o the object to be compared.
     * @return a negative integer, zero, or a positive integer as this object
     * is less than, equal to, or greater than the specified object.
     *
     * @throws NullPointerException if the specified object is null
     * @throws ClassCastException   if the specified object's type prevents it
     *                              from being compared to this object.
     */
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
