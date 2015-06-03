package com.zhenshuiermian.zookeeper.dataClient;


/**
 * Created by zejunW on 2015/6/3.
 */
public class Test {


    /**
     * 测试ChildWatchExecutor类，分别监控znode1的子节点变化和znode2的节点数据变化
     * */
    @org.junit.Test
    public  void childWatchExecutorTest() throws Exception {
        String hostPort = "127.0.0.1:2181,127.0.0.1:2182,127.0.0.1:2183";
        String znode1 = "/root/parent1";
        String znode2 = "/root/parent2";
        ChildWatchExecutor executor = new ChildWatchExecutor(hostPort,
                znode1);
        executor.addChildrenMonitor(znode1);
        //目前ChildWatchExecutor需要支持同时支持两个父节点的话，还需改造
        executor.addDataMonitor(znode2);
        System.out.println("executor start");
        executor.start();
    }

    /**
     * 测试DataWatchExecutor类，分别监控znode的节点数据变化，并将变化后的新数据写入filename文件中
     * */
    @org.junit.Test
    public  void dataWatchExecutorTest() throws Exception {
        String hostPort = "127.0.0.1:2181,127.0.0.1:2182,127.0.0.1:2183";
        String znode="/root/dataNode";
        String filename = "ddddd.txt";
        DataWatchExecutor executor = new DataWatchExecutor(hostPort,
                filename);
        executor.addDataMonitor(znode);
        System.out.println("executor start");
        executor.start();
    }
}
