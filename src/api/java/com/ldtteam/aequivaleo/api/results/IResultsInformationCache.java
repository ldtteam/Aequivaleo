package com.ldtteam.aequivaleo.api.results;

import com.ldtteam.aequivaleo.api.compound.ICompoundInstance;
import com.ldtteam.aequivaleo.api.compound.container.ICompoundContainer;

import java.util.Map;
import java.util.Set;

public interface IResultsInformationCache
{
    /**
     * Returns the calculated and cached resulting information.
     * @return The calculated and cached data, if no information is available, then an empty map is returned.
     */
    Map<ICompoundContainer<?>, Set<ICompoundInstance>> get();
}
