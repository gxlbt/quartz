package com.test.quartz.quartz;

import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.curator.shaded.com.google.common.collect.Lists;
import org.apache.curator.shaded.com.google.common.collect.Maps;
import org.apache.http.client.utils.DateUtils;
import org.quartz.*;
import org.quartz.impl.matchers.GroupMatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.quartz.QuartzJobBean;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @author xqli7@iflytek.com
 * @date 2019/2/13
 * @description:
 */
public class QuartzJobUtils {
    private static final Logger LOG = LoggerFactory.getLogger(QuartzJobUtils.class);

    private QuartzJobUtils() {
        super();
    }

    /**
     * 创建quartz定时任务
     *
     * @param scheduler      调度器
     * @param jobClass       任务class,必须继承QuartzJobBean
     * @param cronExpression cron表达式
     * @param jobDataMap     任务信息
     */
    public static void createJob(Scheduler scheduler, String name, Class<? extends QuartzJobBean> jobClass, String cronExpression, JobDataMap jobDataMap) {
        //任务所属分组
        String group = "TEST";
        LOG.info("----createJob start ，name:{},group:{}", name, group);
        if (exist(scheduler, name, group)) {
            LOG.info("----createJob fail ,job already existed，name:{},group:{}", name, group);
            return;
        }
        //cron表达式封装
        //missfire处理 withMisfireHandlingInstructionDoNothing 错过触发时间时，不执行执行的，等待下一个执行时间
        CronScheduleBuilder scheduleBuilder = CronScheduleBuilder.cronSchedule(cronExpression).withMisfireHandlingInstructionDoNothing();
        //创建任务
        JobDetail jobDetail;
        if (jobDataMap != null) {
            //requestRecovery(true)指在集群中，一个scheduler执行job失败，将会被另外一个scheduler执行
            jobDetail = JobBuilder.newJob(jobClass).withIdentity(name, group).usingJobData(jobDataMap).requestRecovery(true).build();
        } else {
            jobDetail = JobBuilder.newJob(jobClass).withIdentity(name, group).requestRecovery(true).build();
        }
        //创建任务触发器
        Trigger trigger = TriggerBuilder.newTrigger().withIdentity(name, group).withSchedule(scheduleBuilder).build();
        //将触发器与任务绑定到调度器内
        try {
            Date firstFireTime = scheduler.scheduleJob(jobDetail, trigger);
            LOG.info("----createJob success，firstFireTime:{}", DateFormatUtils.format(firstFireTime, "yyyy-MM-dd HH:mm:ss"));
        } catch (SchedulerException e) {
            LOG.error("-----createJob exception", e);
        }
    }

    /**
     * 创建quartz定时任务
     *
     * @param scheduler      调度器
     * @param jobClass       任务class,必须继承QuartzJobBean
     * @param cronExpression cron表达式
     */
    public static void createJob(Scheduler scheduler, String name, Class<? extends QuartzJobBean> jobClass, String cronExpression) {
        createJob(scheduler, name, jobClass, cronExpression, null);
    }

    public static void close(Scheduler scheduler, JobExecutionContext context) {
        TriggerKey triggerKey = context.getTrigger().getKey();
        JobKey jobKey = context.getJobDetail().getKey();
        LOG.info("----quartzJob close，jobName：{}", jobKey.getName());
        try {
            // 停止触发器
            scheduler.pauseTrigger(triggerKey);
            // 移除触发器
            scheduler.unscheduleJob(triggerKey);
            // 删除任务
            scheduler.deleteJob(jobKey);
            LOG.info("----quartzJob success");
        } catch (SchedulerException e) {
            LOG.error("----quartzJob close exception", e);
        }
    }

    /**
     * 判断任务是否存在
     *
     * @param scheduler 调度器
     * @param name      任务名称
     * @param group     任务分组
     * @return true==存在 false==不存在
     */
    public static boolean exist(Scheduler scheduler, String name, String group) {
        JobDetail jobDetail;
        JobKey jobKey = new JobKey(name, group);
        TriggerKey triggerKey = new TriggerKey(name, group);
        try {
            jobDetail = scheduler.getJobDetail(jobKey);
            if (jobDetail != null) {
                if (Trigger.TriggerState.ERROR.equals(scheduler.getTriggerState(triggerKey))) {
                    LOG.info("-----定时任务状态异常，恢复状态，job name :{}", name);
                    scheduler.resumeJob(jobKey);
                }
                return true;
            }
        } catch (SchedulerException e) {
            LOG.error("-----exist exception", e);
        }
        return false;
    }

    /**
     * 获取定时任务列表
     *
     * @param scheduler 任务调度器
     * @return
     */
    public static List<JobView> jobs(Scheduler scheduler) {
        List<JobView> jobViews = Lists.newArrayList();
        try {
            for (String groupName : scheduler.getJobGroupNames()) {
                for (JobKey jobKey : scheduler.getJobKeys(GroupMatcher.jobGroupEquals(groupName))) {
                    String jobName = jobKey.getName();
                    String jobGroup = jobKey.getGroup();

                    // name and group
                    JobView jobView = new JobView();
                    jobView.setName(jobName);
                    jobView.setGroup(jobGroup);

                    // params
                    JobDetail qJobDetail = scheduler.getJobDetail(jobKey);
                    if (null != qJobDetail.getJobDataMap()) {
                        Map<String, Object> params = Maps.newHashMap();
                        params.putAll(qJobDetail.getJobDataMap());
                        jobView.setParams(params);
                    }
                    List<? extends Trigger> triggers = scheduler.getTriggersOfJob(jobKey);
                    // 应该只有一个
                    for (Trigger trigger : triggers) {
                        trigger.getNextFireTime();
                        if (trigger instanceof CronTrigger) {
                            CronTrigger cronTrigger = (CronTrigger) trigger;
                            String cronExpr = cronTrigger.getCronExpression();
                            jobView.setCronExpression(cronExpr);
                        }
                        jobView.setNextFireTime(DateUtils.formatDate(trigger.getNextFireTime(), "yyyy-MM-dd HH:mm:ss"));
                        Trigger.TriggerState state = scheduler.getTriggerState(trigger.getKey());
                        jobView.setStatus(state.name());
                    }
                    jobViews.add(jobView);
                }
            }
        } catch (Exception e) {
            return Collections.emptyList();
        }
        return jobViews;
    }
}
