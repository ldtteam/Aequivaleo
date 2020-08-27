package com.ldtteam.aequivaleo.compound.container.blockstate;

import com.google.gson.*;
import com.ldtteam.aequivaleo.api.compound.container.ICompoundContainer;
import com.ldtteam.aequivaleo.api.compound.container.dummy.Dummy;
import com.ldtteam.aequivaleo.api.compound.container.factory.ICompoundContainerFactory;
import com.ldtteam.aequivaleo.api.compound.container.serialization.ICompoundContainerSerializer;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.ForgeRegistry;
import org.apache.commons.lang3.Validate;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Objects;

public class BlockContainer implements ICompoundContainer<Block>
{

    public static final class BlockFactory implements ICompoundContainerFactory<Block, Block>
    {

        @NotNull
        @Override
        public Class<Block> getInputType()
        {
            return Block.class;
        }

        @NotNull
        @Override
        public Class<Block> getOutputType()
        {
            return Block.class;
        }

        @NotNull
        @Override
        public ICompoundContainer<Block> create(@NotNull final Block inputInstance, final double count)
        {
            return new BlockContainer(inputInstance, count);
        }
    }

    public static final class BlockStateFactory implements ICompoundContainerFactory<BlockState, Block>
    {

        @NotNull
        @Override
        public Class<BlockState> getInputType()
        {
            return BlockState.class;
        }

        @NotNull
        @Override
        public Class<Block> getOutputType()
        {
            return Block.class;
        }

        @NotNull
        @Override
        public ICompoundContainer<Block> create(@NotNull final BlockState inputInstance, final double count)
        {
            return new BlockContainer(inputInstance.getBlock(), count);
        }
    }

    public static final class Serializer implements ICompoundContainerSerializer<Block>
    {

        @Override
        public Class<Block> getType()
        {
            return Block.class;
        }

        @Override
        public ICompoundContainer<Block> deserialize(final JsonElement json, final Type typeOfT, final JsonDeserializationContext context) throws JsonParseException
        {
            final ResourceLocation blockName = new ResourceLocation(json.getAsJsonObject().get("block").getAsString());
            final Double count = json.getAsJsonObject().get("count").getAsDouble();

            return new BlockContainer(
              ForgeRegistries.BLOCKS.getValue(blockName),
              count
            );
        }

        @Override
        public JsonElement serialize(final ICompoundContainer<Block> src, final Type typeOfSrc, final JsonSerializationContext context)
        {
            final JsonObject object = new JsonObject();
            object.addProperty("block", src.getContents().getRegistryName().toString());
            object.addProperty("count", src.getContentsCount());

            return object;
        }
    }

    private final Block contents;
    private final Double count;
    private final int hashCode;

    public BlockContainer(final Block contents, final Double count) {
        this.contents = contents;
        this.count = count;
        this.hashCode = Objects.requireNonNull(contents.getRegistryName()).hashCode();
    }

    @Override
    public Block getContents()
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
        if (!(otherContents instanceof Block))
        {
            return Block.class.getName().compareTo(otherContents.getClass().getName());
        }

        final Block otherBlock = (Block) otherContents;
        if (contents == otherBlock)
            return 0;

        return ((ForgeRegistry<Block>) ForgeRegistries.BLOCKS).getID(contents) - ((ForgeRegistry<Block>) ForgeRegistries.BLOCKS).getID(otherBlock);
    }

    @Override
    public boolean equals(final Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (!(o instanceof BlockContainer))
        {
            return false;
        }

        final BlockContainer that = (BlockContainer) o;

        if (Double.compare(that.count, count) != 0)
        {
            return false;
        }

        return Objects.equals(contents.getRegistryName(), that.contents.getRegistryName());
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
                 "contents=" + contents.getRegistryName() +
                 ", count=" + count +
                 '}';
    }
}
