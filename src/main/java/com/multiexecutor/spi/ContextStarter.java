package com.multiexecutor.spi;

import com.multiexecutor.agent.ClassInfo;
import javassist.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * @author tanjia
 * @since 2019/9/10
 * 运行时环境启动
 */
public class ContextStarter {

    public static final Map<String, String> DECORATE_CLASS = new HashMap<>();

    public static final Set<String> TRANSFORM_CLASS = new HashSet<>();

    static {
        DECORATE_CLASS.put("java.lang.Runnable", "com.multiexecutor.core.MultiRunnable");
        TRANSFORM_CLASS.add("java.util.concurrent.ThreadPoolExecutor");
    }

    public static byte[] transform(ClassInfo classInfo) throws Exception {
        final CtClass ctClass = classInfo.getCtClass();
        generateClass(ctClass);
        return classInfo.getCtClass().toBytecode();
    }

    /**
     * ThreadPoolExecutor类修改
     */
    public static void start() throws Exception {
        ClassPool classPool = ClassPool.getDefault();
        classPool.appendClassPath(new LoaderClassPath(ClassLoader.getSystemClassLoader()));
        CtClass ctClass = classPool.get("java.util.concurrent.ThreadPoolExecutor");
        generateClass(ctClass);
        ctClass.writeFile();
    }

    private static void generateClass(CtClass ctClass) throws NotFoundException, CannotCompileException {
        for (CtMethod ctMethod : ctClass.getDeclaredMethods()) {
            final int modifier = ctMethod.getModifiers();
            if (!Modifier.isPublic(modifier) || Modifier.isStatic(modifier)) {
                continue;
            }
            CtClass[] paramTypes = ctMethod.getParameterTypes();
            StringBuilder insertCode = new StringBuilder();
            for (int i = 0; i < paramTypes.length; i++) {
                String paramTypeName = paramTypes[i].getName();
                if (!DECORATE_CLASS.containsKey(paramTypeName)) {
                    continue;
                }
                System.out.println(ctMethod.getName());
                insertCode.append(String.format("{ $%d =  %s.get($%d);}", i + 1, DECORATE_CLASS.get(paramTypeName), i + 1));
            }
            if (insertCode.length() > 0) {
                ctMethod.insertBefore(insertCode.toString());
            }
        }
    }

}
