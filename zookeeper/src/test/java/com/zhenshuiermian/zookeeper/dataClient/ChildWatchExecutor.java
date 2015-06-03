package com.zhenshuiermian.zookeeper.dataClient;

import com.zhenshuiermian.zookeeper.dataClient.inter.BaseExecutor;
import com.zhenshuiermian.zookeeper.dataClient.inter.MonitorListener;
import org.apache.zookeeper.KeeperException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 监控指定路径下子节点数据变化状况及相关附加操作
 *
 * @author zejunW
 * @Date 2015年5月22日
 * @Version 1.0
 */
public class ChildWatchExecutor extends BaseExecutor {
    String parentNodePath;
    //之前获取的子节点数据
    List<String> prevChildren = null;
    //之前获取的节点数据
    byte prevData[] = null;

    /**
     * 构造函数
     *
     * @param hostPort       zookeeper服务端地址
     * @param parentNodePath 指定监控子节点的父节点路径
     * @throws KeeperException
     * @throws IOException
     */
    public ChildWatchExecutor(String hostPort,
                              String parentNodePath) throws KeeperException, IOException {
        super(hostPort);
        this.parentNodePath = parentNodePath;
    }

    /**
     * 节点监控回调方法返回数据的处理实现方法：
     * <p/>
     * 正常情况会在子节点数据变动后返回队列名称列表。在获取zookeeper服务端数据后，
     * 比较前一次获取的子节点数据，找出需要新增和删除的子节点，和新增子节点的数据
     * <p/>
     * 正常情况会在节点数据变动后返回新的节点数据。在获取zookeeper服务端数据后，
     * 比较前一次获取的节点数据，确认数据是否变化和非空，并打印新节点数据
     *
     * @see MonitorListener#dataProcess(java.lang.Object)
     */
    @Override
    public void dataProcess(Object obj) {
        if (obj != null && obj instanceof byte[]) {
            byte[] data = (byte[]) obj;
            if ((data == null && data != prevData)
                    || (data != null && !Arrays.equals(prevData, data))) {
                System.out.println("节点数据变化，新数据为：" + new String(data));
                prevData = data;
            }
        } else if (obj != null && obj instanceof List<?>) {
            System.out.println("Executor dataProcess:" + obj);
            List<String> nodeList = (List<String>) obj;
            if (prevChildren == null) {
                prevChildren = nodeList;
                System.out.println("首次获得子节点数据，内容为：" + nodeList);
            } else {
                try {
                    List<String> newChildrenList = new ArrayList<String>();
                    for (String childName : prevChildren) {
                        if (!nodeList.contains(childName)) {// 比前一次减少的子节点名称，不用添加到newChildrenList，以提高效率
                            System.out.println("比前一次减少的子节点名称:" + childName);
                        } else {// 不用移除的子节点添加到newChildrenList，准备用于检测是否是新增加子节点
                            newChildrenList.add(childName);
                        }
                    }
                    for (String nodeName : nodeList) {
                        if (!newChildrenList.contains(nodeName)) {// 需要添加的队列名称
                            System.out.println("比前一次增加的子节点名称:" + nodeName);
                            byte[] queueJsonBytes = getNodeData(this.parentNodePath
                                    + "/" + nodeName);
                            System.out.println("比前一次增加的子节点内容:" + new String(queueJsonBytes));
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
}