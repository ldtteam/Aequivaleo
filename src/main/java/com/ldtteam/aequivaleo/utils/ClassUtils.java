package com.ldtteam.aequivaleo.utils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.function.Function;

public final class ClassUtils
{

    private static final Logger LOGGER = LogManager.getLogger();

    private ClassUtils()
    {
        throw new IllegalStateException("Tried to initialize: ClassUtils but this is a Utility class.");
    }

    @SuppressWarnings("unchecked")
    @Nullable
    public static <T> T createOrGetInstance(String className, Class<T> baseClass, Class<? extends Annotation> instanceAnnotation, Function<T, String> nameFunction) {
        //Try to create an instance of the class
        try {
            Class<? extends T> subClass = Class.forName(className).asSubclass(baseClass);
            //First try looking at the fields of the class to see if one of them is specified as the instance
            Field[] fields = subClass.getDeclaredFields();
            for (Field field : fields) {
                if (field.isAnnotationPresent(instanceAnnotation)) {
                    if (Modifier.isStatic(field.getModifiers())) {
                        try {
                            Object fieldValue = field.get(null);
                            if (baseClass.isInstance(fieldValue)) {
                                T instance = (T) fieldValue;
                                LOGGER.debug("Found specified {} instance for: {}. Using it rather than creating a new instance.", baseClass.getSimpleName(),
                                  nameFunction.apply(instance));
                                return instance;
                            } else {
                                LOGGER.error("{} annotation found on non {} field: {}", instanceAnnotation.getSimpleName(), baseClass.getSimpleName(), field);
                                return null;
                            }
                        } catch (IllegalAccessException e) {
                            LOGGER.error("{} annotation found on inaccessible field: {}", instanceAnnotation.getSimpleName(), field);
                            return null;
                        }
                    } else {
                        LOGGER.error("{} annotation found on non static field: {}", instanceAnnotation.getSimpleName(), field);
                        return null;
                    }
                }
            }
            //If we don't have any fields that have the Instance annotation, then try to create a new instance of the class
            return subClass.newInstance();
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | LinkageError e) {
            LOGGER.error("Failed to load: {}", className, e);
        }
        return null;
    }
}
