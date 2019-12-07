package com.dmytrobilokha.xmbt.boot;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.core.FileAppender;
import ch.qos.logback.core.rolling.FixedWindowRollingPolicy;
import ch.qos.logback.core.rolling.RollingFileAppender;
import ch.qos.logback.core.rolling.RollingPolicy;
import ch.qos.logback.core.rolling.SizeBasedTriggeringPolicy;

import ch.qos.logback.core.rolling.TriggeringPolicy;
import ch.qos.logback.core.util.FileSize;
import com.dmytrobilokha.xmbt.config.ConfigService;
import com.dmytrobilokha.xmbt.config.property.LogFilePathProperty;
import com.dmytrobilokha.xmbt.config.property.LogLevelProperty;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;

public class LoggerInitializer {

    @Nonnull
    private final ConfigService configService;

    public LoggerInitializer(@Nonnull ConfigService configService) {
        this.configService = configService;
    }

    void init() {
        LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
        PatternLayoutEncoder logEncoder = createLayoutEncoder(loggerContext);
        RollingFileAppender logFileAppender = createFileAppender(loggerContext, logEncoder);
        RollingPolicy logFilePolicy = createRollingPolicy(loggerContext, logFileAppender);
        TriggeringPolicy triggeringPolicy = createTriggeringPolicy();
        configureAndStartFileAppender(logFileAppender, logFilePolicy, triggeringPolicy);
        configureRootLogger(loggerContext, logFileAppender);
        LoggerFactory.getLogger(this.getClass()).info("Logger has been initialized successfully");
    }

    private void configureRootLogger(@Nonnull LoggerContext loggerContext, @Nonnull FileAppender logFileAppender) {
        Logger log = loggerContext.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME);
        log.detachAndStopAllAppenders();
        log.setAdditive(false);
        log.setLevel(configService.getProperty(LogLevelProperty.class).getValue());
        log.addAppender(logFileAppender);
    }

    @Nonnull
    private PatternLayoutEncoder createLayoutEncoder(@Nonnull LoggerContext loggerContext) {
        PatternLayoutEncoder logEncoder = new PatternLayoutEncoder();
        logEncoder.setContext(loggerContext);
        logEncoder.setPattern("%d{dd.MM.yy HH:mm:ss.SSS} %-5level [%thread] %logger{36} - %msg%n");
        logEncoder.start();
        return logEncoder;
    }

    @Nonnull
    private RollingFileAppender createFileAppender(
            @Nonnull LoggerContext loggerContext, @Nonnull PatternLayoutEncoder layoutEncoder) {
        RollingFileAppender logFileAppender = new RollingFileAppender();
        logFileAppender.setContext(loggerContext);
        logFileAppender.setName("logFile");
        logFileAppender.setEncoder(layoutEncoder);
        logFileAppender.setAppend(true);
        logFileAppender.setFile(configService.getProperty(LogFilePathProperty.class).getStringValue());
        return logFileAppender;
    }

    @Nonnull
    private RollingPolicy createRollingPolicy(
            @Nonnull LoggerContext loggerContext, @Nonnull FileAppender logFileAppender) {
        FixedWindowRollingPolicy logFilePolicy = new FixedWindowRollingPolicy();
        logFilePolicy.setContext(loggerContext);
        logFilePolicy.setParent(logFileAppender);
        logFilePolicy.setFileNamePattern(
                configService.getProperty(LogFilePathProperty.class).getStringValue() + ".%i.zip");
        logFilePolicy.setMinIndex(1);
        logFilePolicy.setMaxIndex(5);
        logFilePolicy.start();
        return logFilePolicy;
    }

    @Nonnull
    private TriggeringPolicy createTriggeringPolicy() {
        SizeBasedTriggeringPolicy triggeringPolicy = new SizeBasedTriggeringPolicy();
        triggeringPolicy.setMaxFileSize(new FileSize(10_485_760));
        triggeringPolicy.start();
        return triggeringPolicy;
    }

    private void configureAndStartFileAppender(
            @Nonnull RollingFileAppender fileAppender
            , @Nonnull  RollingPolicy rollingPolicy
            , @Nonnull  TriggeringPolicy triggeringPolicy
    ) {
        fileAppender.setRollingPolicy(rollingPolicy);
        fileAppender.setTriggeringPolicy(triggeringPolicy);
        fileAppender.start();
    }

}
