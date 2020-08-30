package com.ldtteam.aequivaleo.compound.container.blockstate;

import com.google.gson.*;
import com.ldtteam.aequivaleo.api.compound.container.ICompoundContainer;
import com.ldtteam.aequivaleo.api.compound.container.dummy.Dummy;
import com.ldtteam.aequivaleo.api.compound.container.factory.ICompoundContainerFactory;
import com.ldtteam.aequivaleo.api.util.Constants;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.network.PacketBuffer;
import net.minecraft.state.Property;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.ForgeRegistry;
import net.minecraftforge.registries.ForgeRegistryEntry;
import org.apache.commons.lang3.Validate;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.Map;
import java.util.stream.Collectors;

public class BlockStateContainer implements ICompoundContainer<BlockState>
{

    public static final class Factory extends ForgeRegistryEntry<ICompoundContainerFactory<?>> implements ICompoundContainerFactory<BlockState>
    {

        public Factory()
        {
            setRegistryName(Constants.MOD_ID, "blockstate");
        }

        @NotNull
        @Override
        public Class<BlockState> getContainedType()
        {
            return BlockState.class;
        }

        @NotNull
        @Override
        public ICompoundContainer<BlockState> create(@NotNull final BlockState inputInstance, final double count)
        {
            return new BlockStateContainer(inputInstance, count);
        }

        @Override
        public ICompoundContainer<BlockState> deserialize(final JsonElement json, final Type typeOfT, final JsonDeserializationContext context) throws JsonParseException
        {
            final ResourceLocation blockName = new ResourceLocation(json.getAsJsonObject().get("block").getAsString());
            final Double count = json.getAsJsonObject().get("count").getAsDouble();
            final Map<String, String> properties = !json.getAsJsonObject().has("properties") ? Collections.emptyMap() :
                json.getAsJsonObject().getAsJsonObject("properties").entrySet()
                    .stream()
                    .filter(e -> e.getValue().isJsonPrimitive())
                    .collect(Collectors.toMap(Map.Entry::getKey, stringJsonElementEntry -> stringJsonElementEntry.getValue().getAsString()));

            final Block block = ForgeRegistries.BLOCKS.getValue(blockName);
            if (block == null)
                throw new JsonParseException(String.format("Unknown block: %s", blockName));

            BlockState state = block.getDefaultState();
            for (Map.Entry<String, String> entry : properties.entrySet())
            {
                String propName = entry.getKey();
                String value = entry.getValue();
                Property<?> property = state.getBlock().getStateContainer().getProperty(propName);

                if (property == null)
                    throw new JsonParseException(String.format("Unknown property: %s on block: %s", propName, blockName));

                state = withProperty(state, property, value);
            }

            return new BlockStateContainer(
              state,
              count
            );
        }

        private <T extends Comparable<T>> BlockState withProperty(final BlockState state, final Property<T> property, final String value) {
            return state.with(
              property,
              property.parseValue(value).orElseThrow(() -> new JsonParseException(String.format("Could not parse: %s for property: %s of block: %s",
                value,
                property.getName(),
                state.getBlock().getRegistryName())))
            );
        }

        @Override
        public JsonElement serialize(final ICompoundContainer<BlockState> src, final Type typeOfSrc, final JsonSerializationContext context)
        {
            final JsonObject object = new JsonObject();
            object.addProperty("block", src.getContents().getBlock().getRegistryName().toString());
            object.addProperty("count", src.getContentsCount());

            final JsonObject properties = new JsonObject();
            for (final Property<?> property : src.getContents().getProperties())
            {
                properties.addProperty(property.getName(), convertToString(property, src.getContents()));
            }
            object.add("properties", properties);

            return object;
        }

        public <T extends Comparable<T>> String convertToString(final Property<T> property, final BlockState state) {
            return property.getName(state.get(property));
        }

        @Override
        public void write(final ICompoundContainer<BlockState> object, final PacketBuffer buffer)
        {
            try
            {
                buffer.func_240629_a_(BlockState.BLOCKSTATE_CODEC, object.getContents());
            }
            catch (IOException e)
            {
                throw new IllegalStateException("Failed to write BlockState", e);
            }
            buffer.writeDouble(object.getContentsCount());
        }

        @Override
        public ICompoundContainer<BlockState> read(final PacketBuffer buffer)
        {
            try
            {
                return new BlockStateContainer(
                  buffer.func_240628_a_(BlockState.BLOCKSTATE_CODEC),
                  buffer.readDouble()
                );
            }
            catch (IOException e)
            {
                throw new IllegalStateException("Failed to read BlockState", e);
            }
        }
    }

    private final BlockState contents;
    private final Double count;
    private final int hashCode;

    public BlockStateContainer(final BlockState contents, final Double count) {
        this.contents = contents;
        this.count = count;
        this.hashCode = contents.hashCode();
    }

    @Override
    public BlockState getContents()
    {
        return contents;
    }

    @Override
    public Double getContentsCount()
    {
        return count;
    }

    @Override
    public int compareTo(@NotNull final ICompoundContainer<?> o)
    {
        //Dummies are after us. :D
        if (o instanceof Dummy)
            return -1;

        final Object otherContents = Validate.notNull(o.getContents());
        if (!(otherContents instanceof BlockState))
        {
            return Block.class.getName().compareTo(otherContents.getClass().getName());
        }

        final BlockState otherBlock = (BlockState) otherContents;
        if (contents == otherBlock)
            return 0;

        return  ((ForgeRegistry<Block>) ForgeRegistries.BLOCKS).getID(contents.getBlock()) - ((ForgeRegistry<Block>) ForgeRegistries.BLOCKS).getID(otherBlock.getBlock());
    }

    @Override
    public boolean equals(final Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (!(o instanceof BlockStateContainer))
        {
            return false;
        }

        final BlockStateContainer that = (BlockStateContainer) o;

        if (Double.compare(that.count, count) != 0)
        {
            return false;
        }

        return contents.getBlock().getRegistryName().equals(that.getContents().getBlock().getRegistryName());
    }

    @Override
    public int hashCode()
    {
        return hashCode;
    }

    @Override
    public String toString()
    {
        return "BlockContainer{" +
                 "contents=" + contents.getBlock().getRegistryName() +
                 ", count=" + count +
                 '}';
    }
}
