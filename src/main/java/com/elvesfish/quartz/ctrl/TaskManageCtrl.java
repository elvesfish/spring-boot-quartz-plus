package com.elvesfish.quartz.ctrl;


import com.elvesfish.quartz.bean.QuartzResult;
import com.elvesfish.quartz.bean.TaskInfo;
import com.elvesfish.quartz.bean.TaskUpdateVo;
import com.elvesfish.quartz.bean.TaskVo;
import com.elvesfish.quartz.exception.ErrorEnum;
import com.elvesfish.quartz.exception.ServiceException;
import com.elvesfish.quartz.service.TaskService;

import org.apache.commons.lang3.StringUtils;
import org.quartz.CronExpression;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping(value = "/scheduler-job")
public class TaskManageCtrl {

    @Autowired
    private TaskService taskService;

    public static final String JOBINFO_ERROR = "Job不存在, jobName:{%s},jobGroup:{%s}";

    /**
     * 任务列表
     */
    @GetMapping("/list")
    public QuartzResult list() {
        Map<String, Object> map = new HashMap();
        List<TaskInfo> infos = taskService.list();
        map.put("rows", infos);
        map.put("total", infos.size());
        return new QuartzResult(QuartzResult.SUCCESS_CODE, "查询定时任务列表成功", map);
    }

    /**
     * 保存定时任务
     */
    @PostMapping("/save")
    public QuartzResult save(@RequestBody TaskVo info) {
        QuartzResult chkMsg = chkMsg(info);
        if (!chkMsg.getCode().equals(QuartzResult.SUCCESS_CODE)) {
            return chkMsg;
        }
        try {
            taskService.addJob(info);
        } catch (ServiceException e) {
            return new QuartzResult(QuartzResult.FAIL_CODE, e.getMessage());
        }
        return new QuartzResult(QuartzResult.SUCCESS_CODE, "保存定时任务成功");
    }

    /**
     * 修改定时任务
     */
    @PutMapping("/edit")
    public QuartzResult edit(@RequestBody TaskVo info) {
        QuartzResult chkMsg = chkMsg(info);
        if (!chkMsg.getCode().equals(QuartzResult.SUCCESS_CODE)) {
            return chkMsg;
        }
        try {
            taskService.edit(info);
        } catch (ServiceException e) {
            return new QuartzResult(QuartzResult.FAIL_CODE, e.getMessage());
        }
        return new QuartzResult(QuartzResult.SUCCESS_CODE, "修改定时任务成功");
    }

    /**
     * 删除定时任务
     */
    @DeleteMapping("/delete")
    public QuartzResult delete(@RequestBody TaskUpdateVo info) {
        QuartzResult chkMsg = chkSimpleMsg(info);
        if (!chkMsg.getCode().equals(QuartzResult.SUCCESS_CODE)) {
            return chkMsg;
        }
        try {
            boolean flag = taskService.delete(info.getJobName(), info.getJobGroup());
            if (!flag) {
                return new QuartzResult(QuartzResult.FAIL_CODE, String.format(JOBINFO_ERROR, info.getJobName(), info.getJobGroup()));
            }
        } catch (ServiceException e) {
            return new QuartzResult(QuartzResult.FAIL_CODE, e.getMessage());
        }
        return new QuartzResult(QuartzResult.SUCCESS_CODE, "删除定时任务成功");
    }

    /**
     * 暂停定时任务
     */
    @PutMapping("/pause")
    public QuartzResult pause(@RequestBody TaskUpdateVo info) {
        QuartzResult chkMsg = chkSimpleMsg(info);
        if (!chkMsg.getCode().equals(QuartzResult.SUCCESS_CODE)) {
            return chkMsg;
        }
        try {
            boolean flag = taskService.pause(info.getJobName(), info.getJobGroup());
            if (!flag) {
                return new QuartzResult(QuartzResult.FAIL_CODE, String.format(JOBINFO_ERROR, info.getJobName(), info.getJobGroup()));
            }
        } catch (ServiceException e) {
            return new QuartzResult(QuartzResult.FAIL_CODE, e.getMessage());
        }
        return new QuartzResult(QuartzResult.SUCCESS_CODE, "暂停定时任务成功");
    }

    /**
     * 重启定时任务
     */
    @PutMapping("/resume")
    public QuartzResult resume(@RequestBody TaskUpdateVo info) {
        QuartzResult chkMsg = chkSimpleMsg(info);
        if (!chkMsg.getCode().equals(QuartzResult.SUCCESS_CODE)) {
            return chkMsg;
        }
        try {
            boolean flag = taskService.resume(info.getJobName(), info.getJobGroup());
            if (!flag) {
                return new QuartzResult(QuartzResult.FAIL_CODE, String.format(JOBINFO_ERROR, info.getJobName(), info.getJobGroup()));
            }
        } catch (ServiceException e) {
            return new QuartzResult(QuartzResult.FAIL_CODE, e.getMessage());
        }
        return new QuartzResult(QuartzResult.SUCCESS_CODE, "重启定时任务成功");
    }

    private QuartzResult chkMsg(TaskVo info) {
        String jobName = info.getJobName();
        String jobGroup = info.getJobGroup();
        String jobClass = info.getJobClass();
        String cronExpress = info.getCronExpression();
        int triggerPriority = info.getTriggerPriority();
        if (StringUtils.isBlank(jobName)) {
            return new QuartzResult(ErrorEnum.ERROR_1001.getErrorCode(), ErrorEnum.ERROR_1001.getErrorMsg());
        }
        if (StringUtils.isBlank(jobGroup)) {
            return new QuartzResult(ErrorEnum.ERROR_1002.getErrorCode(), ErrorEnum.ERROR_1002.getErrorMsg());
        }
        if (StringUtils.isBlank(jobClass)) {
            return new QuartzResult(ErrorEnum.ERROR_1003.getErrorCode(), ErrorEnum.ERROR_1003.getErrorMsg());
        }
        if (StringUtils.isBlank(cronExpress)) {
            return new QuartzResult(ErrorEnum.ERROR_1004.getErrorCode(), ErrorEnum.ERROR_1004.getErrorMsg());
        }
        if (!CronExpression.isValidExpression(cronExpress)) {
            return new QuartzResult(ErrorEnum.ERROR_1005.getErrorCode(), ErrorEnum.ERROR_1005.getErrorMsg());
        }
        if (triggerPriority != 0 && triggerPriority > 10) {
            return new QuartzResult(ErrorEnum.ERROR_1006.getErrorCode(), ErrorEnum.ERROR_1006.getErrorMsg());
        }
        return new QuartzResult();
    }

    private QuartzResult chkSimpleMsg(TaskUpdateVo info) {
        String jobName = info.getJobName();
        String jobGroup = info.getJobGroup();
        if (StringUtils.isBlank(jobName)) {
            return new QuartzResult(ErrorEnum.ERROR_1001.getErrorCode(), ErrorEnum.ERROR_1001.getErrorMsg());
        }
        if (StringUtils.isBlank(jobGroup)) {
            return new QuartzResult(ErrorEnum.ERROR_1002.getErrorCode(), ErrorEnum.ERROR_1002.getErrorMsg());
        }
        return new QuartzResult();
    }
}
