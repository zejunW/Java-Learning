package com.zhenshuiermian.zookeeper.dataClient;

/**
 * A simple example program to use DataMonitor to start and
 * stop executables based on a znode. The program watches the
 * specified znode and saves the data that corresponds to the
 * znode in the filesystem. It also starts the specified program
 * with the specified arguments when the znode exists and kills
 * the program if the znode goes away.
 */

import com.zhenshuiermian.zookeeper.dataClient.inter.MonitorListener;
import com.zhenshuiermian.zookeeper.dataClient.inter.BaseExecutor;
import org.apache.zookeeper.KeeperException;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

/**
 * 测试使用的BaseExecutor实现类
 * <<p/>
 * 监控指定路径的节点数据变化状况及相关附加操作
 *
 * @author zejunW
 * @Date 2015年5月22日
 * @Version 1.0
 */
public class DataWatchExecutor extends BaseExecutor {
    private String filename;

    public DataWatchExecutor(String hostPort, String filename) throws KeeperException,
            IOException {
        super(hostPort);
        this.filename = filename;
    }

    /**
     * 节点监控回调方法返回数据的处理实现方法，正常情况会在节点数据变动后返回新的节点数据。在获取zookeeper服务端数据后，
     * 比较前一次获取的节点数据，确认数据是否变化和非空，并往指定文件写入
     *
     * @see MonitorListener#dataProcess(java.lang.Object)
     */
    @Override
    public void dataProcess(Object obj) {
        if (obj != null && obj instanceof byte[]) {
            byte[] data = (byte[]) obj;
            System.out.println("Executor dataProcess:" + new String(data));
            try {
                File fileCopy = new File(filename);
                if (!fileCopy.exists()) {
                    fileCopy.createNewFile();
                }
                FileOutputStream fos = new FileOutputStream(filename);
                fos.write(data);
                fos.flush();
                fos.close();
                System.out.println("新数据已写入文件");
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else if (obj != null && obj instanceof List<?>) {
            System.out.println("Executor dataProcess:" + obj);
        }
    }

}