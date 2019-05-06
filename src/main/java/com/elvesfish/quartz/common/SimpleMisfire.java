package com.elvesfish.quartz.common;

/**
 * @author: elvesfish
 * @date: 2019/5/5
 */
public enum SimpleMisfire {
    /**
     *
     */
    IGNORE_MISFIRES("-1", "以当前时间为触发频率立刻触发一次执行"),
    FIRE_NOW("1", "失效之后再恢复并马上执行"),
    NOW_WITH_EXISTING_COUNT("2", "以当前时间为触发频率立即触发执行"),
    NOW_WITH_REMAINING_COUNT("3", "以当前时间为触发频率立即触发执行,执行至FinalTIme的剩余周期次数"),
    NEXT_WITH_REMAINING_COUNT("4", "不触发立即执行,等待下次触发频率周期时刻"),
    NEXT_WITH_EXISTING_COUNT("5", "不触发立即执行,等待下次触发频率周期时刻");

    private String key;
    private String value;

    SimpleMisfire(String key, String value) {
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
