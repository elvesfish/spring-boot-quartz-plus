### 组件介绍 ###
1. 基于spring-boot-start-quartz基础上添加一些动态接口
2. 支持spring-boot 2.x
3. 动态添加定时任务

### 接口介绍 ###

![](pic/1212.png)

1. 查询所有
2. 保存
3. 修改
4. 暂停
5. 重启
6. 删除


### 使用介绍 ###
        <dependency>
            <groupId>com.elvesfish</groupId>
            <artifactId>spring-boot-quartz-plus</artifactId>
            <version>1.0.1</version>
        </dependency>
        
### 配置文件  ###
还是使用spring-boot-start-quartz的自动配置

     #spring-boot-quartz
     spring.quartz.properties.org.quartz.scheduler.instanceName=scheduler-quartz-test
     spring.quartz.properties.org.quartz.scheduler.instanceId=AUTO
     #quartz jobStore
     spring.quartz.properties.org.quartz.jobStore.class=org.quartz.impl.jdbcjobstore.JobStoreTX
     spring.quartz.properties.org.quartz.jobStore.driverDelegateClass=org.quartz.impl.jdbcjobstore.StdJDBCDelegate
     spring.quartz.properties.org.quartz.jobStore.tablePrefix=QRTZ_
     spring.quartz.properties.org.quartz.jobStore.isClustered=true
     spring.quartz.properties.org.quartz.jobStore.clusterCheckinInterval=10000
     spring.quartz.properties.org.quartz.jobStore.useProperties=false
     #quartz threadPool
     spring.quartz.properties.org.quartz.threadPool.class=org.quartz.simpl.SimpleThreadPool
     spring.quartz.properties.org.quartz.threadPool.threadCount=20
     spring.quartz.properties.org.quartz.threadPool.threadPriority=5
     spring.quartz.properties.org.quartz.threadPool.threadsInheritContextClassLoaderOfInitializingThread=true
     #quartz job store
     spring.quartz.job-store-type=jdbc
     #quartz listener
     #spring.quartz.properties.org.quartz.jobListener.NAME.class=com.elvesfish.quartz.listener.SimpleJobListener
     #spring.quartz.properties.org.quartz.triggerListener.NAME.class=com.elvesfish.quartz.listener.SimpleTriggerListener