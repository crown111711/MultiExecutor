# MultiExecutor
多级担保线程池+跨线程数据传递
1.调用方式
    (1) 感知
        Pool管控 -> agent | proxy | aspect
        传入ThreadPoolExecutor
            -> 直接将任务提交给父线程或子线程运行不可实现跨线程   
    (2) 不感知
        agent