package com.ldtteam.aequivaleo.compound.container.string;

import com.google.gson.*;
import com.ldtteam.aequivaleo.api.compound.container.ICompoundContainer;
import com.ldtteam.aequivaleo.api.compound.container.factory.ICompoundContainerFactory;
import net.minecraft.network.FriendlyByteBuf;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Type;
import java.util.Objects;

public class StringContainer implements ICompoundContainer<String> {

    public static final class Factory implements ICompoundContainerFactory<String> {

        @Override
        public @NotNull Class<String> getContainedType() {
            return String.class;
        }

        @Override
        public @NotNull ICompoundContainer<String> create(@NotNull String inputInstance, double count) {
            return new StringContainer(count, inputInstance);
        }

        @Override
        public ICompoundContainer<String> deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
            final String contents = jsonElement.getAsJsonObject().get("contents").getAsString();
            final double count = jsonElement.getAsJsonObject().get("count").getAsDouble();
            return new StringContainer(count, contents);
        }

        @Override
        public JsonElement serialize(ICompoundContainer<String> stringICompoundContainer, Type type, JsonSerializationContext jsonSerializationContext) {
            final var jsonObject = new JsonObject();
            jsonObject.getAsJsonObject().addProperty("contents", stringICompoundContainer.getContents());
            jsonObject.getAsJsonObject().addProperty("count", stringICompoundContainer.getContentsCount());
            return jsonObject;
        }

        @Override
        public void write(ICompoundContainer<String> object, FriendlyByteBuf buffer) {
            buffer.writeDouble(object.getContentsCount());
            buffer.writeUtf(object.getContents());
        }

        @Override
        public ICompoundContainer<String> read(FriendlyByteBuf buffer) {
            return new StringContainer(buffer.readDouble(), buffer.readUtf());
        }
    }

    private final double count;
    private final String contents;

    public StringContainer(double count, String contents) {
        this.count = count;
        this.contents = contents;
    }

    @Override
    public boolean isValid() {
        return true;
    }

    @Override
    public String getContents() {
        return contents;
    }

    @Override
    public Double getContentsCount() {
        return count;
    }

    @Override
    public int compareTo(@NotNull ICompoundContainer<?> o) {
        if (!(o instanceof StringContainer)) {
            return -1;
        }

        final int stringCompare = contents.compareTo(((StringContainer) o).contents);
        if (stringCompare != 0) {
            return stringCompare;
        }

        return (int) (count - o.getContentsCount());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        StringContainer that = (StringContainer) o;
        return Double.compare(count, that.count) == 0 && Objects.equals(getContents(), that.getContents());
    }

    @Override
    public int hashCode() {
        return Objects.hash(count, getContents());
    }

    @Override
    public String toString() {
        return "StringContainer{" +
                "count=" + count +
                ", contents='" + contents + '\'' +
                '}';
    }
}
