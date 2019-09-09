package com.multiexecutor.core;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author tanjia
 */
public class ReflectUtil {

    private static ConcurrentHashMap<String, Cell> fieldCache = new ConcurrentHashMap<>();

    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    static class Cell {
        Class targetClass;
        Field targetField;
        String fieldName;

        public String getKey() {
            return targetClass.getSimpleName() + "#" + fieldName;
        }

        static String generateKey(Object object, String fieldName) {
            return object.getClass().getSimpleName() + "#" + fieldName;
        }
    }

    private static void makeAccessible(Field field) {
        if (!Modifier.isPublic(field.getModifiers())) {
            field.setAccessible(true);
        }
    }

    private static Field getDeclaredField(Object object, String filedName) {
        String key = Cell.generateKey(object, filedName);
        if (fieldCache.containsKey(key)) {
            return fieldCache.get(key).targetField;
        }
        for (Class<?> superClass = object.getClass(); superClass != Object.class; superClass = superClass
                .getSuperclass()) {
            try {
                Field field = superClass.getDeclaredField(filedName);
                fieldCache.putIfAbsent(key, Cell.builder().targetClass(object.getClass()).targetField(field).fieldName(filedName).build());
                return field;
            } catch (NoSuchFieldException e) {
                // Field 不在当前类定义, 继续向上转型
            }
        }
        return null;
    }

    public static void setFieldValue(Object object, String fieldName, Object value) {
        Field field = getDeclaredField(object, fieldName);

        if (field == null)
            throw new IllegalArgumentException("Could not find field [" + fieldName + "] on target [" + object + "]");

        makeAccessible(field);

        try {
            field.set(object, value);
        } catch (IllegalAccessException e) {
            throw new IllegalStateException(e);
        }
    }

    @SuppressWarnings("unchecked")
    public static <T> T getFieldValue(Object object, String fieldName) {
        Field field = getDeclaredField(object, fieldName);
        if (field == null)
            throw new IllegalArgumentException("Could not find field [" + fieldName + "] on target [" + object + "]");

        makeAccessible(field);

        Object result;
        try {
            result = field.get(object);
        } catch (IllegalAccessException e) {
            throw new IllegalStateException(e);
        }

        return (T) result;
    }
}
