package com.test.quartz.quartz;

import org.quartz.Scheduler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

/**
 * @author xqli7@iflytek.com
 * @date 2019/2/13
 * @description: 初始化任务
 */
@Component
public class QuartzJobManager {
    @Autowired
    private Scheduler scheduler;


    /**
     * 初始化任务的创建
     * 判断任务是否存在，不存在则创建
     */
    @PostConstruct
    public void initJob() {
        //每分钟执行一次
        QuartzJobUtils.createJob(scheduler, TestQuartzJob.class.getSimpleName(), TestQuartzJob.class,
                "0 0/1 * * * ? ");
    }
}
