package com.zhenshuiermian.zookeeper.dataClient.inter;

/**
 * 消息监控处理接口类
 * 
 * @author zejunW
 * @Date 2015年5月25日
 * @Version 1.0
 *
 */
public interface MonitorListener {

	/**
	 * 用于节点监控回调方法返回数据的处理，入参可能是节点数据的byte[]，或者子节点的路径名称集合List<String>
	 */
	void dataProcess(Object obj);

	/**
	 * 消息监控处理类尝试结束程序和连接方法
	 *
	 * @param rc
	 *            the ZooKeeper reason code
	 */
	void closing(int rc);
}
