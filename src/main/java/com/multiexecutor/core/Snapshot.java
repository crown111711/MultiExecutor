package com.multiexecutor.core;

import lombok.Data;

/**
 * @author tanjia
 * @email 378097217@qq.com
 * @date 2019/9/10 0:56
 */
@Data
public class Snapshot {

    Object threadLocalMap;
    Object inheritThreadLocalMap;

    public Snapshot(Object threadLocalMap, Object inheritThreadLocalMap) {
        this.threadLocalMap = threadLocalMap;
        this.inheritThreadLocalMap = inheritThreadLocalMap;
    }
}
