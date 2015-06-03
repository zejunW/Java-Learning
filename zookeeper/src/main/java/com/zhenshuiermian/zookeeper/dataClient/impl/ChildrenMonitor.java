package com.zhenshuiermian.zookeeper.dataClient.impl;

import com.zhenshuiermian.zookeeper.dataClient.inter.MonitorListener;
import com.zhenshuiermian.zookeeper.dataClient.inter.NodeMonitor;
import org.apache.zookeeper.AsyncCallback.Children2Callback;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.KeeperException.Code;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;

import java.util.List;

/**
 * 监控子节点数据，实现getChildren方法的异步回调接口
 * 
 * @author zejunW
 * @Date 2015年5月22日
 * @Version 1.0
 *
 */
public class ChildrenMonitor extends NodeMonitor implements Children2Callback {

	public ChildrenMonitor(ZooKeeper zk, String znode, Watcher chainedWatcher,
			MonitorListener listener) {
		super(zk, znode, chainedWatcher, listener);
		// 使用Executor作为watcher，自身作为异步回调类，监控指定节点
		this.zk.getChildren(znode, this, this, null);
	}

	/**
	 * getChildren方法的Watcher方法实现，用于监控事件及回调方法出发后的再次注册监控（因为监控功能是一次性的），
	 * 及调用额外需要加入的Watcher
	 * 
	 * @see org.apache.zookeeper.Watcher#process(org.apache.zookeeper.WatchedEvent)
	 */
	public void process(WatchedEvent event) {
		System.out.println("ChildrenMonitor process");
		String path = event.getPath();
		System.out.println("event.getPath:" + event.getPath());
		System.out.println("event.getType:" + event.getType());
		if (event.getType() == Event.EventType.None) {
			// We are are being told that the state of the
			// connection has changed
			switch (event.getState()) {
			case SyncConnected:
				// In this particular example we don't need to do anything
				// here - watches are automatically re-registered with
				// server and any watches triggered while the client was
				// disconnected will be delivered (in order of course)
				break;
			case Expired:
				// It's all over,session expired,need reconnection
				setDead(true);
				listener.closing(KeeperException.Code.SessionExpired);
				break;
			}
		} else {
			if (path != null && path.equals(getZnode())) {
				// path节点监控发现有变化，继续添加监控，具体数据处理交由processResult方法
				this.zk.getChildren(getZnode(), this, this, null);
			}
		}
		super.chaineWatcherProcess(event);
	}

	/**
	 * getChildren方法的异步回调方法实现：如果path的子节点存在，
	 * 将子节点路径名称集合传递给MonitorListener的dataProcess实现方法处理
	 * ；如果path节点不存在，则监控path节点的exists事件，用此类作为监控时间的watcher
	 * 
	 * @see org.apache.zookeeper.AsyncCallback.Children2Callback#processResult(int,
	 *      java.lang.String, java.lang.Object, java.util.List,
	 *      org.apache.zookeeper.data.Stat)
	 */
	@Override
	public void processResult(int rc, String path, Object ctx,
			List<String> children, Stat stat) {
		// path节点是否存在
		boolean exists;
		switch (rc) {
		case Code.Ok:
			exists = true;
			break;
		case Code.NoNode:
			exists = false;
			break;
		case Code.SessionExpired:
		case Code.NoAuth:
			setDead(true);
			listener.closing(rc);
			return;
		default:
			// Retry errors，正常情况下不会被调用
			this.zk.getChildren(getZnode(), this, this, null);
			return;
		}
		if (exists) {// path节点存在
			if (children != null) {
				listener.dataProcess(children);
			}
		} else {// path节点不存在，改为监控exists事件
			try {
				this.zk.exists(getZnode(), this);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}