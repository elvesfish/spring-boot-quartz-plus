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
添加依赖包:该工程直接打包就行。
```
        <dependency>
            <groupId>com.elvesfish</groupId>
            <artifactId>spring-boot-quartz-plus</artifactId>
            <version>1.0.1</version>
        </dependency>
```
### 配置文件  ###
还是使用spring-boot-start-quartz的自动配置.
application.properties
```
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
```     
### 业务代码 ###
1. TestJob类
```
    package com.elvesfish.ms.job;
    
    import com.elvesfish.ms.service.ISecUserService;
    
    import org.quartz.DisallowConcurrentExecution;
    import org.quartz.Job;
    import org.quartz.JobExecutionContext;
    import org.quartz.JobExecutionException;
    import org.springframework.beans.factory.annotation.Autowired;
    
    import java.util.Date;
    
    import lombok.extern.slf4j.Slf4j;
    
    @DisallowConcurrentExecution
    @Slf4j
    public class TestJob implements Job {
    
        @Autowired
        private ISecUserService userService;
    
        @Override
        public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
             userService.list();
            log.info("==============定时任务：" + new Date() + "");
        }
    }
```
2. 使用上面介绍的保存接口：POST请求
jobName：任务名称</br>
jobGroup：任务组</br>
jobClass：TestJob测试类路径</br>
```
    {
      "cronExpression": "0 */1 * * * ?",
      "jobClass": "com.elvesfish.ms.job.TestJob",
      "jobDataMap": {},
      "jobDescription": "测试工作任务内容述说明",
      "jobGroup": "qq",
      "jobName": "test",
      "triggerDescription": "触发器描述说明",
      "triggerPriority": 5
    }
```
3. 打印日志内容
```
2019-04-01 15:36:00.031 DEBUG com.elvesfish.ms.dao.SecUserMapper.selectList - ==>  Preparing: SELECT user_id,user_name,password,nick_name,user_status,create_by,create_time,update_by,update_time FROM sec_user 
2019-04-01 15:36:00.032 DEBUG com.elvesfish.ms.dao.SecUserMapper.selectList - ==> Parameters: 
2019-04-01 15:36:00.037 DEBUG com.elvesfish.ms.dao.SecUserMapper.selectList - <==      Total: 6
2019-04-01 15:36:00.037 INFO  com.elvesfish.ms.job.TestJob - ==============定时任务：Mon Apr 01 15:36:00 GMT+08:00 2019
2019-04-01 15:37:00.029 DEBUG com.elvesfish.ms.dao.SecUserMapper.selectList - ==>  Preparing: SELECT user_id,user_name,password,nick_name,user_status,create_by,create_time,update_by,update_time FROM sec_user 
2019-04-01 15:37:00.029 DEBUG com.elvesfish.ms.dao.SecUserMapper.selectList - ==> Parameters: 
2019-04-01 15:37:00.033 DEBUG com.elvesfish.ms.dao.SecUserMapper.selectList - <==      Total: 6
2019-04-01 15:37:00.034 INFO  com.elvesfish.ms.job.TestJob - ==============定时任务：Mon Apr 01 15:37:00 GMT+08:00 2019
```