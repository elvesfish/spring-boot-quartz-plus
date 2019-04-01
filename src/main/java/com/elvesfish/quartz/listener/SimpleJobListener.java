package com.elvesfish.quartz.listener;

import com.alibaba.fastjson.JSON;

import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.JobListener;
import org.quartz.SchedulerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedHashMap;
import java.util.Map;

public class SimpleJobListener implements JobListener {

    private static Logger logger = LoggerFactory.getLogger(SimpleJobListener.class);

    public SimpleJobListener() {
    }

    @Override
    public String getName() {
        return getClass().getSimpleName();
    }

    @Override
    public void jobToBeExecuted(JobExecutionContext context) {
        String resultMsg = getJobMsg(context);
        logger.info("{}任务执行时监听之前", resultMsg);
    }

    @Override
    public void jobExecutionVetoed(JobExecutionContext context) {
        String resultMsg = getJobMsg(context);
        logger.info("{}任务执行时被否决并且不执行", resultMsg);
    }

    @Override
    public void jobWasExecuted(JobExecutionContext context, JobExecutionException jobException) {
        String resultMsg = getJobMsg(context);
        logger.info("{}任务执行时已完成", resultMsg);
        if (jobException != null) {
            logger.error("任务在执行过程中出现了异常:" + jobException.getMessage());
        }
    }

    private String getJobMsg(JobExecutionContext context) {
        String schedulerName = "";
        try {
            schedulerName = context.getScheduler().getSchedulerName();
        } catch (SchedulerException e) {
            logger.error("SchedulerException :" + e.getMessage());
        }
        JobDetail jobDetail = context.getJobDetail();
        Map<String, Object> map = new LinkedHashMap();
        map.put("schedulerName", schedulerName);
        map.put("jobName", jobDetail.getKey().getName());
        map.put("jobGroup", jobDetail.getKey().getGroup());
        map.put("jobClass", jobDetail.getJobClass().getName());
        return JSON.toJSONString(map);
    }
}
