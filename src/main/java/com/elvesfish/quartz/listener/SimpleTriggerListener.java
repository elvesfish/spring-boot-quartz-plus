package com.elvesfish.quartz.listener;

import com.alibaba.fastjson.JSON;

import org.apache.commons.lang3.time.DateFormatUtils;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.TriggerListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedHashMap;
import java.util.Map;

public class SimpleTriggerListener implements TriggerListener {

    private static Logger logger = LoggerFactory.getLogger(SimpleTriggerListener.class);

    public SimpleTriggerListener() {
    }

    @Override
    public String getName() {
        return "SimpleTriggerListener";
    }

    @Override
    public void triggerFired(Trigger trigger, JobExecutionContext context) {
        String resultMsg = getMsg(trigger, context);
        logger.info("{} 触发之前", resultMsg);
    }

    @Override
    public boolean vetoJobExecution(Trigger trigger, JobExecutionContext context) {
        return false;
    }

    @Override
    public void triggerMisfired(Trigger trigger) {
        String triggerName = trigger.getKey().getName();
        String nextFireTime = DateFormatUtils.format(trigger.getNextFireTime(), "yyyy-MM-dd HH:mm:ss");
        logger.info("triggerName:{},下次触发时间:{},错过触发了", triggerName, nextFireTime);
    }

    @Override
    public void triggerComplete(Trigger trigger, JobExecutionContext context, Trigger.CompletedExecutionInstruction triggerInstructionCode) {
        String resultMsg = getMsg(trigger, context);
        logger.info("{} 触发完成", resultMsg);
    }

    private String getMsg(Trigger trigger, JobExecutionContext context) {
        String schedulerName = "";
        String triggerName = trigger.getKey().getName();
        try {
            schedulerName = context.getScheduler().getSchedulerName();
        } catch (SchedulerException e) {
            logger.error("SchedulerException :" + e.getMessage());
        }
        JobDetail jobDetail = context.getJobDetail();
        Map<String, Object> map = new LinkedHashMap();
        map.put("schedulerName", schedulerName);
        map.put("triggerName", triggerName);
        map.put("jobName", jobDetail.getKey().getName());
        map.put("jobGroup", jobDetail.getKey().getGroup());
        map.put("jobClass", jobDetail.getJobClass().getName());
        return JSON.toJSONString(map);
    }
}
