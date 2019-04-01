package com.elvesfish.quartz.bean;

import java.io.Serializable;
import java.util.Map;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TaskVo implements Serializable {
    /**
     * 任务名称
     */
    private String jobName;
    /**
     * 任务分组
     */
    private String jobGroup;
    /**
     * 任务分组
     */
    private String jobClass;
    /**
     * 优先级
     */
    private int triggerPriority;
    /**
     * 任务表达式
     */
    private String cronExpression;
    /**
     * 任务需要传递数据使用
     */
    private Map<String, Object> jobDataMap;
    /**
     * 任务描述
     */
    private String jobDescription;
    /**
     * 触发器描述
     */
    private String triggerDescription;
}
