package com.multiexecutor.agent;

import com.multiexecutor.ExceptionWrapper;
import com.multiexecutor.spi.ContextStarter;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;

/**
 * @author tanjia
 * @since 2019/9/10
 */
public class MultiTransformer implements ClassFileTransformer {

    private static final byte[] EMPTY_BYTE_ARRAY = {};

    @Override
    public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {

        String pruneClassName = toClassName(className);
        if (!ContextStarter.TRANSFORM_CLASS.contains(pruneClassName)) {
            return ExceptionWrapper.unCheckRun(ContextStarter::transform).apply(new ClassInfo(pruneClassName, loader, classfileBuffer));
        }
        return EMPTY_BYTE_ARRAY;
    }

    private static String toClassName(final String classFile) {
        return classFile.replaceAll("/", ".");
    }
}
