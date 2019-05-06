package com.elvesfish.quartz.common;

/**
 * @author: elvesfish
 * @date: 2019/5/5
 */
public enum TriggerType {

    /**
     *
     */
    CRON("0", "表达式触发器"),
    SIMPLE("1", "简单触发器");
    private String key;
    private String value;

    TriggerType(String key, String value) {
        this.key = key;
        this.value = value;
    }

    public String getKey() {
        return key;
    }

    public String getValue() {
        return value;
    }
}
