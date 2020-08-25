package com.ldtteam.aequivaleo.api.event;

import com.ldtteam.aequivaleo.api.IAequivaleoAPI;

/**
 * Interface that defines aequivaleo events.
 */
public interface IAequivaleoEvent
{

    default IAequivaleoAPI getApi(){
        return IAequivaleoAPI.Holder.getInstance();
    }
}
