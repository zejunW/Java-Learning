package com.zhenshuiermian.zookeeper.dataClient.inter;

import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;

/**
 * 节点监控抽象类，继承zookeeper原生类Watcher，并传入MonitorListener，
 * 用于管理监控的数据处理监控结束后的zookeeper长连接关闭尝试
 * 
 * @author zejunW
 * @Date 2015年5月25日
 * @Version 1.0
 *
 */
public abstract class NodeMonitor implements Watcher {

	public NodeMonitor(ZooKeeper zk, String znode, Watcher chainedWatcher,
			MonitorListener listener) {
		this.zk = zk;
		this.znode = znode;
		this.chainedWatcher = chainedWatcher;
		this.listener = listener;
	}

	public ZooKeeper zk;

	private String znode;

	private boolean dead;

	/**
	 * 额外添加的Watcher
	 */
	private Watcher chainedWatcher;

	protected MonitorListener listener;

	public boolean isDead() {
		return dead;
	}

	public void setDead(boolean dead) {
		this.dead = dead;
	}

	public String getZnode() {
		return znode;
	}

	public void setZnode(String znode) {
		this.znode = znode;
	}

	protected void chaineWatcherProcess(WatchedEvent event) {
		if (chainedWatcher != null) {
			chainedWatcher.process(event);
		}
	}

}
