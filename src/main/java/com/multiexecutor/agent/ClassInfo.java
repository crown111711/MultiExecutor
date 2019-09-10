package com.multiexecutor.agent;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.LoaderClassPath;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.ByteArrayInputStream;
import java.io.IOException;

/**
 * @author tanjia
 * @since 2019/9/10
 */
@Data
@NoArgsConstructor
public class ClassInfo {
    private String className;
    private ClassLoader classLoader;
    private byte[] byteBuffer;

    CtClass ctClass;

    public ClassInfo(String className, ClassLoader classLoader, byte[] byteBuffer) {
        this.className = className;
        this.classLoader = classLoader;
        this.byteBuffer = byteBuffer;
    }

    public CtClass getCtClass() {
        if (ctClass != null) {
            return ctClass;
        }

        ClassPool classPool = new ClassPool(true);
        if (classLoader == null) {
            classPool.appendClassPath(new LoaderClassPath(ClassLoader.getSystemClassLoader()));
        } else {
            classPool.appendClassPath(new LoaderClassPath(classLoader));
        }

        final CtClass clazz;
        try {
            clazz = classPool.makeClass(new ByteArrayInputStream(byteBuffer), false);
        } catch (Exception e) {
            throw new RuntimeException("class make error " + e.getMessage());
        }
        ctClass.defrost();

        this.ctClass = clazz;
        return clazz;
    }
}
