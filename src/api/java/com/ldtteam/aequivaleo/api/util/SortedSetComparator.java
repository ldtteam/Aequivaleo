package com.ldtteam.aequivaleo.api.util;

import java.util.Comparator;
import java.util.Iterator;
import java.util.SortedSet;

public final class SortedSetComparator<T extends Comparable<T>> implements Comparator<SortedSet<T>>
{

    public static <Z extends Comparable<Z>> SortedSetComparator<Z> getInstance()
    {
        return new SortedSetComparator<>();
    }

    private SortedSetComparator()
    {
    }

    @Override
    public int compare(final SortedSet<T> ts, final SortedSet<T> t1)
    {
        Comparator<? super T> comparator = ts.comparator();

        if (comparator != t1.comparator() || comparator == null)
            comparator = Comparator.naturalOrder();

        comparator = Comparator.nullsLast(comparator);

        if (ts.size() != t1.size())
            return ts.size() - t1.size();

        final Iterator<T> left = ts.iterator();
        final Iterator<T> right = t1.iterator();

        for (int i = 0; i < ts.size(); i++)
        {
            final T leftEntry = left.next();
            final T rightEntry = right.next();

            final int comparison = comparator.compare(leftEntry, rightEntry);
            if (comparison != 0)
                return comparison;
        }

        return 0;
    }
}
