package com.test.quartz.quartz;

import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobExecutionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.quartz.QuartzJobBean;

/**
 * @author xqli7@iflytek.com
 * @date 2019/2/13
 * @description: 具体任务类
 */
@DisallowConcurrentExecution
public class TestQuartzJob extends QuartzJobBean {
    private static final Logger LOGGER = LoggerFactory.getLogger(TestQuartzJob.class);

    @Override
    protected void executeInternal(JobExecutionContext jobExecutionContext) {
        LOGGER.info("-----TestQuartzJob start");
        //这里写具体的实现业务逻辑
        try {
            //线程睡眠是因为需要测试，防止程序过快执行
            Thread.sleep(5000);
            System.out.println("任务执行了" + "================================");

        } catch (Exception e) {
            LOGGER.error("休眠时间异常", e);
        }

    }

}
