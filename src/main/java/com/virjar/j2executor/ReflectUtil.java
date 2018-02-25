package com.virjar.j2executor;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

/**
 * Created by virjar on 2018/2/25.<br>
 * 通过反射增强线程池功能
 */
public class ReflectUtil {
    private static void makeAccessible(Field field) {
        if (!Modifier.isPublic(field.getModifiers())) {
            field.setAccessible(true);
        }
    }

    private static Field getDeclaredField(Object object, String filedName) {
        for (Class<?> superClass = object.getClass(); superClass != Object.class; superClass = superClass
                .getSuperclass()) {
            try {
                return superClass.getDeclaredField(filedName);
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
