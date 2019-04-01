package com.elvesfish.quartz.bean;

import java.io.Serializable;

public class QuartzResult implements Serializable {
    public static final String SUCCESS_CODE = "0";
    public static final String FAIL_CODE = "1";

    private String code;
    private String message;
    private Object data;

    public QuartzResult() {
        this.code = QuartzResult.SUCCESS_CODE;
        this.message = "";
    }

    public QuartzResult(String code, String message) {
        this.code = code;
        this.message = message;
    }

    public QuartzResult(String code, String message, Object data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }

}
