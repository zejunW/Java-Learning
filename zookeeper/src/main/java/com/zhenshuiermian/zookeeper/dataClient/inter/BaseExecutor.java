package com.zhenshuiermian.zookeeper.dataClient.inter;

import com.zhenshuiermian.zookeeper.dataClient.impl.DataMonitor;
import com.zhenshuiermian.zookeeper.dataClient.impl.ExistsMonitor;
import com.zhenshuiermian.zookeeper.dataClient.impl.ChildrenMonitor;
import org.apache.zookeeper.*;
import org.apache.zookeeper.data.Stat;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * 使用NodeMonitor实现类监控zookeeper节点getData、exists、getChildren事件，
 * 并保持与zookeeper服务端的长连接
 * <p/>
 * 可以调用addChildrenMonitor、addDataMonitor、addExistsMonitor方法动态添加监控节点
 * <p/>
 * 调用start和stop启动和停止循环，启动时必须添加一个节点监控事件
 * <p/>
 * 实现类需要实现void dataProcess(Object obj)方法，实际处理监控获取的节点数据
 *  <p/>
 * 封装了getNodeData和setNodeData两个基本的zookeeper节点操作方法
 * @author zejunW
 * @Date 2015年5月22日
 * @Version 1.0
 */
public abstract class BaseExecutor implements Watcher, Runnable,
        MonitorListener {

    /**
     * 节点监控对象列表
     * <p/>
     * 可包括ChildrenMonitor、DataMonitor、ExistsMonitor，
     * 分别可用于监控zookeeper节点getChildren、getData、exists事件
     */
    private List<NodeMonitor> nodeMonitorList;
    /**
     * zookeeper客户端对象
     */
    private ZooKeeper zk;

    /**
     * 构造函数
     *
     * @param hostPort zookeeper服务端地址
     * @throws KeeperException
     * @throws IOException
     */
    public BaseExecutor(String hostPort) throws KeeperException, IOException {
        // 在建立连接前设置，让zookeeper绕过sasl安全机制，否则会报一个错：Failed to load
        // users/passwords/role files java.io.IOException: No properties file:
        // users.properties or defaults: defaultUsers.properties found
        System.setProperty("zookeeper.sasl.client", "false");
        zk = new ZooKeeper(hostPort, 3000, this);
        nodeMonitorList = new ArrayList<NodeMonitor>();
    }

    /**
     * 实现Watcher接口方法，作为客户端默认监控Watcher，无实际作用，基本只会在最初连接建立时触发一次
     *
     * @see org.apache.zookeeper.Watcher#process(org.apache.zookeeper.proto.WatcherEvent)
     */
    public void process(WatchedEvent event) {
        System.out.println("default watcher process,todo nothing");
    }

    /**
     * 实现Runnable接口方法，调用时先校验节点监控列表是否有内容，若无报错
     * <p/>
     * 启动后现场处于阻塞状态，一旦被唤醒，检查节点监控列表是否还有内容：若有，继续阻塞；若无，终止程序
     *
     * @see java.lang.Runnable#run()
     */
    public void run() {
        if (nodeMonitorList == null || nodeMonitorList.size() == 0) {
            System.out.println("must add least one monitor node!!");
            return;
        }
        try {
            synchronized (this) {
                while (nodeMonitorList.size() > 0) {
                    wait();
                    removeDeadMonitor();
                }
            }
        } catch (InterruptedException e) {
        } finally {
            try {
                nodeMonitorList = null;
                zk.close();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 唤醒run方法阻塞的线程，尝试终止程序和连接
     *
     * @see com.ls.jms.zookeeper.inter.MonitorListener#closing(int)
     */
    public void closing(int rc) {
        synchronized (this) {
            notifyAll();
        }
    }

    /**
     * 启动run方法，调用时先校验节点监控列表是否有内容，若无报错
     *
     * @throws Exception
     */
    public void start() throws Exception {
        if (nodeMonitorList == null || nodeMonitorList.size() == 0) {
            throw new Exception("must add least one monitor node!!");
        }
        run();
    }

    /**
     * 移除节点监控列表所有监控内容，并唤醒run方法阻塞的线程，尝试终止程序和连接
     */
    public void stop() {
        if (nodeMonitorList != null) {
            removeAllMonitor();
        }
        closing(0);
    }

    /**
     * 移除节点监控列表已结束监控内容
     */
    private void removeDeadMonitor() {
        List<NodeMonitor> delList = new ArrayList<NodeMonitor>();
        for (NodeMonitor nodeMonitor : nodeMonitorList) {
            if (nodeMonitor.isDead()) {
                // nodeMonitorList.remove(nodeMonitor);
                delList.add(nodeMonitor);
            }
        }
        nodeMonitorList.removeAll(delList);
    }

    /**
     * 移除节点监控列表所有监控内容
     */
    private void removeAllMonitor() {
        for (NodeMonitor nodeMonitor : nodeMonitorList) {
            nodeMonitor.setDead(true);
            // nodeMonitorList.remove(nodeMonitor);
        }
        nodeMonitorList.clear();
    }

    /**
     * 增加节点的子节点监控
     *
     * @param nodePath 监控节点路径
     */
    public void addChildrenMonitor(String nodePath) {
        NodeMonitor nodeMonitor = new ChildrenMonitor(zk, nodePath, null, this);
        nodeMonitorList.add(nodeMonitor);
    }

    /**
     * 增加节点的存在监控
     *
     * @param nodePath 监控节点路径
     */
    public void addExistsMonitorr(String nodePath) {
        NodeMonitor nodeMonitor = new ExistsMonitor(zk, nodePath, null, this);
        nodeMonitorList.add(nodeMonitor);
    }

    /**
     * 增加节点的数据监控
     *
     * @param nodePath 监控节点路径
     */
    public void addDataMonitor(String nodePath) {
        NodeMonitor nodeMonitor = new DataMonitor(zk, nodePath, null, this);
        nodeMonitorList.add(nodeMonitor);
    }

    /**
     * 获取指定节点的数据
     *
     * @param nodePath 获取数据的节点路径
     * @return
     * @throws KeeperException
     * @throws InterruptedException
     */
    public byte[] getNodeData(String nodePath) throws KeeperException,
            InterruptedException {
        return zk.getData(nodePath, false, null);
    }

    /**
     * 设置指定节点的数据，若节点不存在则创建
     *
     * @param nodePath 设置数据的节点路径
     * @param data 需要设值的节点数据
     * @throws KeeperException
     * @throws InterruptedException
     */
    public void setNodeData(String nodePath, byte[] data) throws KeeperException,
            InterruptedException {
        createNode(zk, nodePath.substring(0, nodePath.lastIndexOf("/")));
        Stat s = zk.exists(nodePath, false);
        if (s == null) {
            zk.create(nodePath, data,
                    ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
        } else {
            zk.setData(nodePath, data, -1);
        }
    }

    /**
     * 对需要创建接地地址进行迭代，逐级创建
     *
     * @param zk   zookeeper客户端对象
     * @param path 需要创建节点地址
     * @throws KeeperException
     * @throws InterruptedException
     */
    private void createNode(ZooKeeper zk, String path) throws KeeperException,
            InterruptedException {
        Stat s = zk.exists(path, false);
        if (s == null) {
            String subPath = path.substring(0, path.lastIndexOf("/"));
            System.out.println("subPath:" + subPath);
            if (subPath.length() > 1) {// 路径必须“/”起头
                createNode(zk, subPath);
            }
            zk.create(path, new byte[0], ZooDefs.Ids.OPEN_ACL_UNSAFE,
                    CreateMode.PERSISTENT);
            System.out.println("create node:" + path);
        }
    }

}