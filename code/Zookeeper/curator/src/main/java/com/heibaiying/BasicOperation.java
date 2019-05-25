package com.heibaiying;

import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.api.CuratorWatcher;
import org.apache.curator.framework.imps.CuratorFrameworkState;
import org.apache.curator.framework.recipes.cache.*;
import org.apache.curator.framework.recipes.cache.PathChildrenCache.StartMode;
import org.apache.curator.retry.RetryNTimes;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.data.Stat;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

/**
 * @author : heibaiying
 * @description : curator客户端API基本使用
 */
public class BasicOperation {


    private CuratorFramework client = null;
    private static final String zkServerPath = "192.168.0.226:2181";
    private static final String nodePath = "/hadoop/yarn";


    @Before
    public void prepare() {
        // 重试策略
        RetryPolicy retryPolicy = new RetryNTimes(3, 5000);
        client = CuratorFrameworkFactory.builder()
                .connectString(zkServerPath)
                .sessionTimeoutMs(10000).retryPolicy(retryPolicy)
                .namespace("workspace").build();  //指定命名空间后，client的所有路径操作都会以/workspace开头
        client.start();
    }


    /**
     * 获取当前zookeeper的状态
     */
    @Test
    public void getStatus() {
        CuratorFrameworkState state = client.getState();
        System.out.println("服务是否已经启动:" + (state == CuratorFrameworkState.STARTED));
    }


    /**
     * 创建节点(s)
     */
    @Test
    public void createNodes() throws Exception {
        byte[] data = "abc".getBytes();
        client.create().creatingParentsIfNeeded()
                .withMode(CreateMode.PERSISTENT)      //节点类型
                .withACL(ZooDefs.Ids.OPEN_ACL_UNSAFE)
                .forPath(nodePath, data);
    }


    /**
     * 获取节点信息
     */
    @Test
    public void getNode() throws Exception {
        Stat stat = new Stat();
        byte[] data = client.getData().storingStatIn(stat).forPath(nodePath);
        System.out.println("节点数据:" + new String(data));
        System.out.println("节点信息:" + stat.toString());
    }

    /**
     * 获取该节点的所有子节点
     */
    @Test
    public void getChildrenNodes() throws Exception {
        List<String> childNodes = client.getChildren().forPath("/hadoop");
        for (String s : childNodes) {
            System.out.println(s);
        }
    }


    /**
     * 更新节点
     */
    @Test
    public void updateNode() throws Exception {
        byte[] newData = "defg".getBytes();
        client.setData().withVersion(0)     // 传入版本号，如果版本号错误则拒绝更新操作,并抛出BadVersion异常
                .forPath(nodePath, newData);
    }


    /**
     * 删除节点
     */
    @Test
    public void deleteNodes() throws Exception {
        client.delete()
                .guaranteed()                // 如果删除失败，那么在会继续执行，直到成功
                .deletingChildrenIfNeeded()  // 如果有子节点，则递归删除
                .withVersion(0)              // 传入版本号，如果版本号错误则拒绝删除操作,并抛出BadVersion异常
                .forPath(nodePath);
    }


    /**
     * 判断节点是否存在
     */
    @Test
    public void existNode() throws Exception {
        // 如果节点存在则返回其状态信息如果不存在则为null
        Stat stat = client.checkExists().forPath(nodePath + "aa/bb/cc");
        System.out.println("节点是否存在:" + !(stat == null));
    }


    /**
     * 使用usingWatcher注册的监听是一次性的,即监听只会触发一次，监听完毕后就销毁
     */
    @Test
    public void DisposableWatch() throws Exception {
        client.getData().usingWatcher(new CuratorWatcher() {
            public void process(WatchedEvent event) {
                System.out.println("节点" + event.getPath() + "发生了事件:" + event.getType());
            }
        }).forPath(nodePath);
        Thread.sleep(1000 * 1000);  //休眠以观察测试效果
    }


    /**
     * 注册永久监听
     */
    @Test
    public void permanentWatch() throws Exception {
        // 使用NodeCache包装节点，对其注册的监听作用于节点，且是永久性的
        NodeCache nodeCache = new NodeCache(client, nodePath);
        // 通常设置为true, 代表创建nodeCache时,就去获取对应节点的值并缓存
        nodeCache.start(true);
        nodeCache.getListenable().addListener(new NodeCacheListener() {
            public void nodeChanged() {
                ChildData currentData = nodeCache.getCurrentData();
                if (currentData != null) {
                    System.out.println("节点路径：" + currentData.getPath() +
                            "数据：" + new String(currentData.getData()));
                }
            }
        });
        Thread.sleep(1000 * 1000);  //休眠以观察测试效果
    }


    /**
     * 监听子节点的操作
     */
    @Test
    public void permanentChildrenNodesWatch() throws Exception {

        // 第三个参数代表除了节点状态外，是否还缓存节点内容
        PathChildrenCache childrenCache = new PathChildrenCache(client, "/hadoop", true);
        /*
         * StartMode代表初始化方式:
         *    NORMAL: 异步初始化
         *    BUILD_INITIAL_CACHE: 同步初始化
         *    POST_INITIALIZED_EVENT: 异步并通知,初始化之后会触发事件
         */
        childrenCache.start(StartMode.POST_INITIALIZED_EVENT);

        List<ChildData> childDataList = childrenCache.getCurrentData();
        System.out.println("当前数据节点的子节点列表：");
        childDataList.forEach(x -> System.out.println(x.getPath()));

        childrenCache.getListenable().addListener(new PathChildrenCacheListener() {

            public void childEvent(CuratorFramework client, PathChildrenCacheEvent event) {
                switch (event.getType()) {
                    case INITIALIZED:
                        System.out.println("childrenCache初始化完成");
                        break;
                    case CHILD_ADDED:
                        // 需要注意的是: 即使是之前已经存在的子节点，也会触发该监听，因为会把该子节点加入childrenCache缓存中
                        System.out.println("增加子节点:" + event.getData().getPath());
                        break;
                    case CHILD_REMOVED:
                        System.out.println("删除子节点:" + event.getData().getPath());
                        break;
                    case CHILD_UPDATED:
                        System.out.println("被修改的子节点的路径:" + event.getData().getPath());
                        System.out.println("修改后的数据:" + new String(event.getData().getData()));
                        break;
                }
            }
        });

        Thread.sleep(1000 * 1000); //休眠以观察测试效果
    }


    @After
    public void destroy() {
        if (client != null) {
            client.close();
        }
    }

}
