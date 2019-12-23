package com.test.quartz.application;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanNameGenerator;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.stereotype.Component;

/**
 * @author xqli7@iflytek.com
 * @date 2019/2/13
 * @description: springboot启动类
 */
@SpringBootApplication
@ComponentScan(basePackages = "com.test.quartz.*")
@EnableScheduling
public class CenterApplication {
    private static final Logger LOG = LoggerFactory.getLogger(CenterApplication.class);

    private static final String START_PACKAGE = "com.test.quartz.";

    public static class CustomGenerator implements BeanNameGenerator {
        @Override
        public String generateBeanName(BeanDefinition beanDefinition, BeanDefinitionRegistry beanDefinitionRegistry) {
            String beanName = beanDefinition.getBeanClassName();
            if (beanName.startsWith(START_PACKAGE)) {
                return beanName;
            }
            try {
                Class<?> aClass = Class.forName(beanName);
                Component annotation = aClass.getAnnotation(Component.class);
                if (annotation == null) {
                    return beanName;
                }
                String value = annotation.value();
                if (StringUtils.isNotBlank(value)) {
                    return value;
                }
            } catch (Exception e) {
                LOG.error("generateBeanName method throw a exception", e);
            }
            return beanName;
        }
    }


    public static void main(String... args) {
        ApplicationContext ctx = new SpringApplicationBuilder(CenterApplication.class)
                .beanNameGenerator(new CustomGenerator())
                .run(args);
        String[] activeProfiles = ctx.getEnvironment().getActiveProfiles();
        for (String profile : activeProfiles) {
            LOG.info("Spring Boot 使用profile为: application-{}.yml", profile);
        }
    }
}
