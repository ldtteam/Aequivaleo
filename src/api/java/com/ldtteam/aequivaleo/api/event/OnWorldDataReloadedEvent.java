package com.ldtteam.aequivaleo.api.event;

import net.minecraft.world.IWorld;
import net.minecraftforge.event.world.WorldEvent;

/**
 * Fired when the data of a world has to be reloaded.
 * Is fired after Aequivaleo has reloaded its own data.
 *
 * But before analysis begins.
 */
public class OnWorldDataReloadedEvent extends WorldEvent implements IAequivaleoEvent
{
    public OnWorldDataReloadedEvent(final IWorld world)
    {
        super(world);
    }
}
