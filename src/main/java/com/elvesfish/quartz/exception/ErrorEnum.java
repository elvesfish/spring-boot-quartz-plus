package com.elvesfish.quartz.exception;

public enum ErrorEnum {
    /**
     * job名称不能为空
     */
    ERROR_1001("1001", "job名称不能为空"),
    /**
     * job分组不能为空
     */
    ERROR_1002("1002", "job分组不能为空"),
    /**
     * job类不能为空
     */
    ERROR_1003("1003", "job类不能为空"),
    /**
     * 表达式不能为空
     */
    ERROR_1004("1004", "表达式不能为空"),
    /**
     * Cron表达式格式不正确
     */
    ERROR_1005("1005", "Cron表达式格式不正确"),
    /**
     * 优先级范围为1-10
     */
    ERROR_1006("1006", "优先级范围为1-10")
    ;

    ErrorEnum(String errorCode, String errorMsg) {
        this.errorCode = errorCode;
        this.errorMsg = errorMsg;
    }

    private String errorCode;
    private String errorMsg;

    public String getErrorCode() {
        return errorCode;
    }

    public String getErrorMsg() {
        return errorMsg;
    }

}
