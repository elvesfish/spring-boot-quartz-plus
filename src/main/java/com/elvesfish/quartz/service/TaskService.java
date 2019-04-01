package com.elvesfish.quartz.service;


import com.elvesfish.quartz.bean.TaskInfo;
import com.elvesfish.quartz.bean.TaskVo;
import com.elvesfish.quartz.exception.ServiceException;

import org.apache.commons.lang3.time.DateFormatUtils;
import org.quartz.CronScheduleBuilder;
import org.quartz.CronTrigger;
import org.quartz.Job;
import org.quartz.JobBuilder;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.quartz.TriggerKey;
import org.quartz.impl.matchers.GroupMatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class TaskService {

    private static Logger logger = LoggerFactory.getLogger(TaskService.class);

    public static final String DATE_PATTERN = "yyyy-MM-dd HH:mm:ss";

    @Autowired
    private Scheduler scheduler;

    /**
     * 所有任务列表
     */
    public List<TaskInfo> list() {
        List<TaskInfo> list = new ArrayList();
        try {
            for (String groupJob : scheduler.getJobGroupNames()) {
                for (JobKey jobKey : scheduler.getJobKeys(GroupMatcher.<JobKey>groupEquals(groupJob))) {
                    List<? extends Trigger> triggers = scheduler.getTriggersOfJob(jobKey);
                    getTaskList(triggers, jobKey, list);
                }
            }
        } catch (SchedulerException e) {
            logger.error(e.getMessage());
        }
        return list;
    }

    private List<TaskInfo> getTaskList(List<? extends Trigger> triggers, JobKey jobKey, List<TaskInfo> list) throws SchedulerException {
        String cronExpression = "";
        String createTime = "";
        String nextFileTime = "";
        String previousFireTime = "";
        String triggerDescription = "";
        int priority = 0;
        TaskInfo info;
        for (Trigger trigger : triggers) {
            Trigger.TriggerState triggerState = scheduler.getTriggerState(trigger.getKey());
            JobDetail jobDetail = scheduler.getJobDetail(jobKey);
            if (trigger instanceof CronTrigger) {
                CronTrigger cronTrigger = (CronTrigger) trigger;
                cronExpression = cronTrigger.getCronExpression();
                triggerDescription = cronTrigger.getDescription();
                priority = cronTrigger.getPriority();
                if (cronTrigger.getNextFireTime() != null) {
                    nextFileTime = DateFormatUtils.format(cronTrigger.getNextFireTime(), DATE_PATTERN);
                }
                if (cronTrigger.getPreviousFireTime() != null) {
                    previousFireTime = DateFormatUtils.format(cronTrigger.getPreviousFireTime(), DATE_PATTERN);
                }
                createTime = DateFormatUtils.format(cronTrigger.getStartTime(), DATE_PATTERN);
            }
            info = new TaskInfo();
            info.setSchedulerName(scheduler.getSchedulerName());
            info.setSchedulerInstanceId(scheduler.getSchedulerInstanceId());
            info.setJobName(jobKey.getName());
            info.setJobGroup(jobKey.getGroup());
            info.setJobClass(jobDetail.getJobClass().getName());
            info.setJobDescription(jobDetail.getDescription());
            info.setJobDataMap(jobDetail.getJobDataMap());
            info.setJobStatus(triggerState.name());
            info.setCronExpression(cronExpression);
            info.setTriggerPriority(priority);
            info.setTriggerDescription(triggerDescription);
            info.setCreateTime(createTime);
            info.setNextFileTime(nextFileTime);
            info.setPreviousFireTime(previousFireTime);
            list.add(info);
        }
        return list;
    }

    /**
     * 保存定时任务
     */
    public void addJob(TaskVo info) {
        String jobName = info.getJobName();
        String jobGroup = info.getJobGroup();
        String jobClass = info.getJobClass();
        String cronExpression = info.getCronExpression();
        String jobDescription = info.getJobDescription();
        String triggerDescription = info.getTriggerDescription();
        Map<String, Object> jobDataMapInfo = info.getJobDataMap();
        int triggerPriority = info.getTriggerPriority();
        triggerPriority = triggerPriority == 0 ? 5 : triggerPriority;
        try {
            if (checkExists(jobName, jobGroup)) {
                throw new ServiceException(String.format("Job已经存在, jobName:{%s},jobGroup:{%s}", jobName, jobGroup));
            }

            TriggerKey triggerKey = TriggerKey.triggerKey(jobName, jobGroup);
            JobKey jobKey = JobKey.jobKey(jobName, jobGroup);
            //表达式触发器
            CronScheduleBuilder schedBuilder = CronScheduleBuilder.cronSchedule(cronExpression).withMisfireHandlingInstructionDoNothing();
            CronTrigger trigger = TriggerBuilder.newTrigger().withIdentity(triggerKey).withDescription(triggerDescription).
                withPriority(triggerPriority).withSchedule(schedBuilder).build();

            //简单触发器
//            SimpleScheduleBuilder simpleScheduleBuilder = SimpleScheduleBuilder.simpleSchedule().withIntervalInSeconds(2).repeatForever();
//            SimpleTrigger simpleTrigger = (SimpleTrigger) TriggerBuilder.newTrigger().withIdentity(triggerKey).withDescription(triggerDescription)
//                    .withSchedule(simpleScheduleBuilder).build();
            //jobDataMap
            JobDataMap jobDataMap = new JobDataMap();
            if (jobDataMapInfo != null) {
                jobDataMap = new JobDataMap(jobDataMapInfo);
//                jobDetail.getJobBuilder().setJobData(jobDataMap);
            }
            //JobDetail
            Class<? extends Job> clazz = (Class<? extends Job>) Class.forName(jobClass);
            JobDetail jobDetail = JobBuilder.newJob(clazz).withIdentity(jobKey).withDescription(jobDescription).requestRecovery().usingJobData(jobDataMap).build();

            scheduler.scheduleJob(jobDetail, trigger);
            String schedulerName = scheduler.getSchedulerName();
            logger.info("schedulerName:{},jobName:{},jobGroup:{},jobClass:{} 保存成功", schedulerName, jobName, jobGroup, jobClass);
        } catch (SchedulerException e) {
            throw new ServiceException("执行表达式错误");
        } catch (ClassNotFoundException e) {
            throw new ServiceException("类名不存在");
        }
    }

    /**
     * 修改定时任务
     */
    public void edit(TaskVo info) {
        String jobName = info.getJobName();
        String jobGroup = info.getJobGroup();
        String jobClass = info.getJobClass();
        String cronExpression = info.getCronExpression();
        String jobDescription = info.getJobDescription();
        String triggerDescription = info.getTriggerDescription();
        Map<String, Object> jobDataMapInfo = info.getJobDataMap();
        int triggerPriority = info.getTriggerPriority();
        triggerPriority = triggerPriority == 0 ? 5 : triggerPriority;
        try {
            if (!checkExists(jobName, jobGroup)) {
                throw new ServiceException(String.format("Job不存在, jobName:{%s},jobGroup:{%s}", jobName, jobGroup));
            }
            TriggerKey triggerKey = TriggerKey.triggerKey(jobName, jobGroup);
            JobKey jobKey = new JobKey(jobName, jobGroup);

            //先remove job
            scheduler.pauseTrigger(triggerKey);
            scheduler.unscheduleJob(triggerKey);

            //add job
            //表达式触发器
            CronScheduleBuilder schedBuilder = CronScheduleBuilder.cronSchedule(cronExpression).withMisfireHandlingInstructionDoNothing();
            CronTrigger trigger = TriggerBuilder.newTrigger().withIdentity(triggerKey).withDescription(triggerDescription).
                withPriority(triggerPriority).withSchedule(schedBuilder).build();

            //jobDataMap
            JobDataMap jobDataMap = new JobDataMap();
            if (jobDataMapInfo != null) {
                jobDataMap = new JobDataMap(jobDataMapInfo);
            }
            //JobDetail
            Class<? extends Job> clazz = (Class<? extends Job>) Class.forName(jobClass);
            JobDetail jobDetail = JobBuilder.newJob(clazz).withIdentity(jobKey).withDescription(jobDescription).requestRecovery().usingJobData(jobDataMap).build();

            scheduler.scheduleJob(jobDetail, trigger);
            String schedulerName = scheduler.getSchedulerName();
            logger.info("schedulerName:{},jobName:{},jobGroup:{},jobClass:{} 修改成功", schedulerName, jobName, jobGroup, jobClass);
        } catch (SchedulerException e) {
            throw new ServiceException("执行表达式错误");
        } catch (ClassNotFoundException e) {
            throw new ServiceException("类名不存在");
        }
    }

    /**
     * 删除定时任务
     *
     * @param jobName  任务名称
     * @param jobGroup 任务分组
     * @return true:成功 false:失败
     */
    public boolean delete(String jobName, String jobGroup) {
        boolean flag = false;
        try {
            if (checkExists(jobName, jobGroup)) {
                TriggerKey triggerKey = TriggerKey.triggerKey(jobName, jobGroup);
                scheduler.pauseTrigger(triggerKey);
                scheduler.unscheduleJob(triggerKey);
                flag = true;
                String schedulerName = scheduler.getSchedulerName();
                logger.info("schedulerName:{},jobName:{},jobGroup:{} 删除成功", schedulerName, jobName, jobGroup);
            }
        } catch (SchedulerException e) {
            throw new ServiceException(e.getMessage());
        }
        return flag;
    }

    /**
     * 暂停定时任务
     *
     * @param jobName  任务名称
     * @param jobGroup 任务分组
     * @return true:成功 false:失败
     */
    public boolean pause(String jobName, String jobGroup) {
        boolean flag = false;
        try {
            TriggerKey triggerKey = TriggerKey.triggerKey(jobName, jobGroup);
            if (checkExists(jobName, jobGroup)) {
                scheduler.pauseTrigger(triggerKey);
                flag = true;
                String schedulerName = scheduler.getSchedulerName();
                logger.info("schedulerName:{},jobName:{},jobGroup:{} 暂停成功", schedulerName, jobName, jobGroup);
            }
        } catch (SchedulerException e) {
            throw new ServiceException(e.getMessage());
        }
        return flag;
    }

    /**
     * 重新开始任务
     *
     * @param jobName  任务名称
     * @param jobGroup 任务分组
     * @return true:成功 false:失败
     */
    public boolean resume(String jobName, String jobGroup) {
        boolean flag = false;
        try {
            if (checkExists(jobName, jobGroup)) {
                TriggerKey triggerKey = TriggerKey.triggerKey(jobName, jobGroup);
                scheduler.resumeTrigger(triggerKey);
                String schedulerName = scheduler.getSchedulerName();
                flag = true;
                logger.info("schedulerName:{},jobName:{},jobGroup:{},重启成功", schedulerName, jobName, jobGroup);
            }
        } catch (SchedulerException e) {
            logger.error(e.getMessage());
        }
        return flag;
    }

    /**
     * 验证是否存在
     *
     * @param jobName
     * @param jobGroup
     * @throws SchedulerException
     */
    private boolean checkExists(String jobName, String jobGroup) throws SchedulerException {
        TriggerKey triggerKey = TriggerKey.triggerKey(jobName, jobGroup);
        return scheduler.checkExists(triggerKey);
    }
}
