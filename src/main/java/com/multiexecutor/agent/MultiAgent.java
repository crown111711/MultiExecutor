package com.multiexecutor.agent;

import java.lang.instrument.Instrumentation;
import java.util.HashMap;
import java.util.Map;

/**
 * @author tanjia
 * @since 2019/9/10
 */
public final class MultiAgent {


    public static volatile Map<String, String> args;
    public static volatile boolean loaded;

    /**
     * 若需要完全解决跨线程问题，则需要挂载此agent
     *
     * @param agentArgs
     * @param inst
     */
    public static void premain(String agentArgs, Instrumentation inst) {
        args = splitCommaColonStringToKV(agentArgs);
        MultiTransformer transformer = new MultiTransformer();
        inst.addTransformer(transformer);

        loaded = true;
    }

    /**
     * Split to {@code json} like String({@code "k1:v1,k2:v2"}) to KV map({@code "k1"->"v1", "k2"->"v2"}).
     */
    static Map<String, String> splitCommaColonStringToKV(String commaColonString) {
        Map<String, String> ret = new HashMap<String, String>();
        if (commaColonString == null || commaColonString.trim().length() == 0) return ret;

        final String[] splitKvArray = commaColonString.trim().split("\\s*,\\s*");
        for (String kvString : splitKvArray) {
            final String[] kv = kvString.trim().split("\\s*:\\s*");
            if (kv.length == 0) continue;

            if (kv.length == 1) ret.put(kv[0], "");
            else ret.put(kv[0], kv[1]);
        }

        return ret;
    }
}
