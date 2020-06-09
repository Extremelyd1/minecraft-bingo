package com.extremelyd1.util;

import java.lang.reflect.Field;

public class ReflectionUtil {

    /**
     * Get the value stored in the field with name fieldName from the object instance.
     * @param instance The instance to get the value from
     * @param fieldName The name of the field
     * @param returnClazz The class of the return value
     * @param <R> The type of return value
     * @param <T> The type of instance
     * @return The value in the given field of the given instance, or null if it cannot be found
     */
    public static <R, T> R getField(T instance, Class<R> returnClazz, String fieldName) {
        Field field;
        try {
            field = instance.getClass().getDeclaredField(fieldName);
        } catch (NoSuchFieldException ignored) {
            return null;
        }

        field.setAccessible(true);

        Object value;
        try {
            value = field.get(instance);
        } catch (IllegalAccessException e) {
            return null;
        }

        return returnClazz.cast(value);
    }

}
