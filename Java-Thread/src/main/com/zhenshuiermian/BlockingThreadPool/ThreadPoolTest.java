package com.zhenshuiermian.BlockingThreadPool;

import org.junit.Test;

import java.util.concurrent.*;

/**
 * 代码中线程池统一使用ThreadPoolExecutor
 * <p/>
 * ThreadPoolExecutor第六个参数“线程池对拒绝任务的处理策略”使用默认策略，即：AbortPolicy，当线程池拒绝task之后抛出RejectedExecutionException异常
 * <p/>
 * ThreadPoolExecutor统一使用LinkedBlockingQueue作为队列
 * <p/>
 * Created by zejunW on 2015/5/7.
 */
public class ThreadPoolTest {

    @Test
    /**
     * 使用execute方法直接往线程池增加task，超过queue数量（4）和线程池最大线程数量（6）之和后，抛出异常
     * */
    public void AddTaskByExecute() throws InterruptedException {
        BlockingQueue<Runnable> queue = new LinkedBlockingQueue<Runnable>(4); //固定为4的线程队列
        ThreadPoolExecutor executor = new ThreadPoolExecutor(2, 6, 1, TimeUnit.DAYS, queue);
        for (int i = 0; i < 11; i++) {
            executor.execute(new Thread(new ExecuteThreadTest(), "TestThread".concat("" + i)));
            System.out.println("已加入线程" + i + "个");
            int threadSize = queue.size();
            System.out.println("线程队列大小为-->" + threadSize);
        }
        executor.shutdown();
    }

    @Test
    /**
     * 使用execute方法直接往线程池增加task，达到上限：queue数量（4）和线程池最大线程数量（6）之和，对任务队列使用add方法再添加task：若队列已满抛出异常
     * */
    public void AddTaskByExecuteAndAdd() throws InterruptedException {
        BlockingQueue<Runnable> queue = new LinkedBlockingQueue<Runnable>(4); //固定为4的线程队列
        ThreadPoolExecutor executor = new ThreadPoolExecutor(2, 6, 1, TimeUnit.DAYS, queue);
        for (int i = 0; i < 10; i++) {
            executor.execute(new Thread(new ExecuteThreadTest(), "TestThread".concat("" + i)));
            int threadSize = queue.size();
            System.out.println("线程队列大小为-->" + threadSize);
            if (threadSize == 4) {
                queue.add(new Runnable() {  //队列已满，抛异常
                    @Override
                    public void run() {
                        System.out.println("新线程在现场池和队列都满后，尝试加入队列！");
                    }
                });
            }
        }
        executor.shutdown();
    }

    @Test
    /**
     * 使用execute方法直接往线程池增加task，达到上限：queue数量（4）和线程池最大线程数量（6）之和，对任务队列使用offer方法再添加task：若队列未满添加成功返回true；若队列已满添加失败返回false
     * */
    public void AddTaskByExecuteAndOffer() throws InterruptedException {
        BlockingQueue<Runnable> queue = new LinkedBlockingQueue<Runnable>(4); //固定为4的线程队列
        ThreadPoolExecutor executor = new ThreadPoolExecutor(2, 6, 1, TimeUnit.DAYS, queue);
        for (int i = 0; i < 10; i++) {
            executor.execute(new Thread(new ExecuteThreadTest(), "TestThread".concat("" + i)));
            int threadSize = queue.size();
            System.out.println("线程队列大小为-->" + threadSize);
            if (threadSize == 4) {
                final boolean flag = queue.offer(new Runnable() {
                    @Override
                    public void run() {
                        System.out.println("新线程在现场池和队列都满后，尝试加入队列！");
                    }
                });
                System.out.println("添加新线程标志为-->" + flag);
            }
        }
        executor.shutdown();
    }

    @Test
    /**
     * 使用execute方法直接往线程池增加task，达到上限：queue数量（4）和线程池最大线程数量（6）之和，对任务队列使用put方法再添加task：若队列未满添加成功；若队列已满线程添加操作被阻塞至队列有空余
     * */
    public void AddTaskByExecuteAndPut() throws InterruptedException {
        BlockingQueue<Runnable> queue = new LinkedBlockingQueue<Runnable>(4); //固定为4的线程队列
        ThreadPoolExecutor executor = new ThreadPoolExecutor(2, 6, 1, TimeUnit.DAYS, queue);
        for (int i = 0; i < 10; i++) {
            executor.execute(new Thread(new ExecuteThreadTest(), "TestThread".concat("" + i)));
            int threadSize = queue.size();
            System.out.println("线程队列大小为-->" + threadSize);
            if (threadSize == 4) {
                queue.put(new Runnable() {
                    @Override
                    public void run() {
                        System.out.println("新线程在现场池和队列都满后，尝试加入队列！");
                    }
                });
            }
        }
        executor.shutdown();
    }


}
