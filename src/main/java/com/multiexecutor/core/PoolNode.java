package com.multiexecutor.core;

import lombok.Data;

import java.util.List;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * @author tanjia
 * @email 378097217@qq.com
 * @date 2019/9/10 0:35
 */
@Data
public class PoolNode {
    PoolNode parent;
    List<PoolNode> children;
    ThreadPoolExecutor value;

    public boolean isLeaf() {
        return children == null || children.isEmpty();
    }
}
