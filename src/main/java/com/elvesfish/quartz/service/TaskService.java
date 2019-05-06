package com.elvesfish.quartz.service;


import com.elvesfish.quartz.bean.TaskInfo;
import com.elvesfish.quartz.bean.TaskVo;
import com.elvesfish.quartz.common.CronMisfire;
import com.elvesfish.quartz.common.SimpleMisfire;
import com.elvesfish.quartz.common.TriggerType;
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
import org.quartz.SimpleScheduleBuilder;
import org.quartz.SimpleTrigger;
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
        String jobDescription = info.getJobDescription();
        Map<String, Object> jobDataMapInfo = info.getJobDataMap();

        try {
            if (checkExists(jobName, jobGroup)) {
                throw new ServiceException(String.format("Job已经存在, jobName:{%s},jobGroup:{%s}", jobName, jobGroup));
            }

            TriggerKey triggerKey = TriggerKey.triggerKey(jobName, jobGroup);
            JobKey jobKey = JobKey.jobKey(jobName, jobGroup);
            //trigger
            Trigger trigger = this.selectTrigger(triggerKey, info);

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
        String jobDescription = info.getJobDescription();
        Map<String, Object> jobDataMapInfo = info.getJobDataMap();

        try {
            if (!checkExists(jobName, jobGroup)) {
                throw new ServiceException(String.format("Job不存在, jobName:{%s},jobGroup:{%s}", jobName, jobGroup));
            }
            TriggerKey triggerKey = TriggerKey.triggerKey(jobName, jobGroup);
            JobKey jobKey = new JobKey(jobName, jobGroup);

            //先remove job
            scheduler.pauseTrigger(triggerKey);
            scheduler.unscheduleJob(triggerKey);

            //trigger
            Trigger trigger = this.selectTrigger(triggerKey, info);

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

    /**
     * 根据触发器类型选择触发器(默认是表达式触发器)
     *
     * @param triggerKey
     * @param info
     * @return
     */
    private Trigger selectTrigger(TriggerKey triggerKey, TaskVo info) {
        Trigger trigger;
        if (info.getTriggerType().equals(TriggerType.CRON.getKey())) {
            trigger = setCronTrigger(triggerKey, info);
        } else if (info.getTriggerType().equals(TriggerType.SIMPLE.getKey())) {
            trigger = setSimpleTrigger(triggerKey, info);
        } else {
            trigger = setCronTrigger(triggerKey, info);
        }
        return trigger;
    }

    /**
     * 设置表达式触发器
     *
     * @param triggerKey
     * @param info
     * @return
     */
    private CronTrigger setCronTrigger(TriggerKey triggerKey, TaskVo info) {
        int triggerPriority = info.getTriggerPriority();
        triggerPriority = triggerPriority == 0 ? 5 : triggerPriority;
        //表达式触发器
        CronScheduleBuilder schedBuilder = CronScheduleBuilder.cronSchedule(info.getCronExpression());
        this.setCronMisFireType(info, schedBuilder);
        return TriggerBuilder.newTrigger().withIdentity(triggerKey).withDescription(info.getTriggerDescription()).
            withPriority(triggerPriority).withSchedule(schedBuilder).build();
    }

    /**
     * 设置简单触发器
     *
     * @param triggerKey
     * @param info
     * @return
     */
    private SimpleTrigger setSimpleTrigger(TriggerKey triggerKey, TaskVo info) {
        //简单触发器
        SimpleScheduleBuilder simpleScheduleBuilder = SimpleScheduleBuilder.simpleSchedule().
            withIntervalInSeconds(info.getIntervalInSeconds()).withRepeatCount(info.getRepeatCount());
        this.setSimpleMisFireType(info, simpleScheduleBuilder);
        return TriggerBuilder.newTrigger().withIdentity(triggerKey).withDescription(info.getTriggerDescription())
                             .withSchedule(simpleScheduleBuilder).build();
    }


    /**
     * 表达式失败处理指令选择策略
     *
     * @param info
     * @param cronScheduleBuilder
     */
    private void setCronMisFireType(TaskVo info, CronScheduleBuilder cronScheduleBuilder) {
        if (CronMisfire.DO_NOTHING.getKey().equals(info.getCronMisfire())) {
            //不触发立即执行,等待下次Cron触发频率到达时刻开始按照Cron频率依次执行
            cronScheduleBuilder.withMisfireHandlingInstructionDoNothing();
        } else if (CronMisfire.FIRE_ONCE_NOW.getKey().equals(info.getCronMisfire())) {
            //以当前时间为触发频率立刻触发一次执行
            //然后按照Cron频率依次执行
            cronScheduleBuilder.withMisfireHandlingInstructionFireAndProceed();
        } else if (CronMisfire.IGNORE_MISFIRES.getKey().equals(info.getCronMisfire())) {
            //以错过的第一个频率时间立刻开始执行
            //重做错过的所有频率周期后
            //当下一次触发频率发生时间大于当前时间后，再按照正常的Cron频率依次执行
            cronScheduleBuilder.withMisfireHandlingInstructionIgnoreMisfires();
        } else {
            //默认值
            //不触发立即执行,等待下次Cron触发频率到达时刻开始按照Cron频率依次执行
            cronScheduleBuilder.withMisfireHandlingInstructionDoNothing();
        }
    }

    /**
     * 简单失败处理指令选择策略
     *
     * @param info
     * @param simpleScheduleBuilder
     */
    private void setSimpleMisFireType(TaskVo info, SimpleScheduleBuilder simpleScheduleBuilder) {
        if (SimpleMisfire.FIRE_NOW.getKey().equals(info.getSimpleMisfire())) {
            //以当前时间为触发频率立即触发执行
            //执行至FinalTIme的剩余周期次数
            //以调度或恢复调度的时刻为基准的周期频率，FinalTime根据剩余次数和当前时间计算得到
            //调整后的FinalTime会略大于根据starttime计算的到的FinalTime值
            simpleScheduleBuilder.withMisfireHandlingInstructionFireNow();
        } else if (SimpleMisfire.IGNORE_MISFIRES.getKey().equals(info.getSimpleMisfire())) {
            //以错过的第一个频率时间立刻开始执行
            //重做错过的所有频率周期
            //当下一次触发频率发生时间大于当前时间以后，按照Interval的依次执行剩下的频率
            //共执行RepeatCount+1次
            simpleScheduleBuilder.withMisfireHandlingInstructionIgnoreMisfires();
        } else if (SimpleMisfire.NEXT_WITH_EXISTING_COUNT.getKey().equals(info.getSimpleMisfire())) {
            //不触发立即执行
            //等待下次触发频率周期时刻，执行至FinalTime的剩余周期次数
            //以startTime为基准计算周期频率，并得到FinalTime
            //即使中间出现pause，resume以后保持FinalTime时间不变
            simpleScheduleBuilder.withMisfireHandlingInstructionNextWithExistingCount();
        } else if (SimpleMisfire.NOW_WITH_EXISTING_COUNT.getKey().equals(info.getSimpleMisfire())) {
            //——以当前时间为触发频率立即触发执行
            //——执行至FinalTIme的剩余周期次数
            //——以调度或恢复调度的时刻为基准的周期频率，FinalTime根据剩余次数和当前时间计算得到
            //——调整后的FinalTime会略大于根据starttime计算的到的FinalTime值
            //总结:失效之后，再启动之后马上执行，但是起始次数清零，总次数=7+当前misfire执行次数-1
            simpleScheduleBuilder.withMisfireHandlingInstructionNowWithExistingCount();
        } else if (SimpleMisfire.NEXT_WITH_REMAINING_COUNT.getKey().equals(info.getSimpleMisfire())) {
            //——不触发立即执行
            //——等待下次触发频率周期时刻，执行至FinalTime的剩余周期次数
            //——以startTime为基准计算周期频率，并得到FinalTime
            //——即使中间出现pause，resume以后保持FinalTime时间不变
            simpleScheduleBuilder.withMisfireHandlingInstructionNextWithRemainingCount();
        } else if (SimpleMisfire.NOW_WITH_REMAINING_COUNT.getKey().equals(info.getSimpleMisfire())) {
            //——以当前时间为触发频率立即触发执行
            //——执行至FinalTIme的剩余周期次数
            //——以调度或恢复调度的时刻为基准的周期频率，FinalTime根据剩余次数和当前时间计算得到
            //——调整后的FinalTime会略大于根据starttime计算的到的FinalTime值
            simpleScheduleBuilder.withMisfireHandlingInstructionNowWithRemainingCount();
        } else {
            //默认值
            simpleScheduleBuilder.withMisfireHandlingInstructionFireNow();
        }
    }
}
