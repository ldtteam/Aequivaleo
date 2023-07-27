package com.ldtteam.aequivaleo.api.util;

import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class CompositeType implements ParameterizedType
{

    private final String   typeName;
    private final Class<?> baseClass;
    private final Type[]   genericClass;

    public CompositeType(Class<?> baseClass, Class<?>... genericClasses) {
        this.baseClass = baseClass;
        this.genericClass = genericClasses;

        List<String> generics = Arrays.asList(genericClasses)
                                  .stream()
                                  .map(Class::getName)
                                  .collect(Collectors.toList());
        String genericTypeString = StringUtils.join(generics, ",");
        this.typeName = baseClass.getName() + "<" + genericTypeString + ">";
    }

    @Override
    public String getTypeName() {
        return typeName;
    }

    @Override
    public Type[] getActualTypeArguments() {
        return genericClass;
    }

    @Override
    public Type getRawType() {
        return baseClass;
    }

    @Override
    public Type getOwnerType() {
        return null;
    }
}