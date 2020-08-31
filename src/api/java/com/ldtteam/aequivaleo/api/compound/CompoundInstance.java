package com.ldtteam.aequivaleo.api.compound;

import com.ldtteam.aequivaleo.api.util.IPacketBufferSerializer;
import org.jetbrains.annotations.NotNull;

public final class CompoundInstance implements Comparable<CompoundInstance>
{
    private final ICompoundType type;
    private final Double amount;

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
    public int compareTo(@NotNull final CompoundInstance o)
    {
        if (o.getType() != getType())
            return getType().getRegistryName().toString().compareTo(o.getType().getRegistryName().toString());

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
}
