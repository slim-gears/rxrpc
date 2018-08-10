package com.slimgears.rxrpc.apt.util;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;
import ch.qos.logback.core.AppenderBase;
import com.google.common.collect.ImmutableMap;
import org.slf4j.LoggerFactory;

import javax.annotation.processing.Messager;
import javax.tools.Diagnostic;

public class MessagerAppender extends AppenderBase<ILoggingEvent> {
    private final static ImmutableMap<Level, Diagnostic.Kind> loglevelMap = ImmutableMap.<Level, Diagnostic.Kind>builder()
            .put(Level.TRACE, Diagnostic.Kind.OTHER)
            .put(Level.DEBUG, Diagnostic.Kind.OTHER)
            .put(Level.INFO, Diagnostic.Kind.NOTE)
            .put(Level.WARN, Diagnostic.Kind.WARNING)
            .put(Level.ERROR, Diagnostic.Kind.ERROR)
            .build();

    private final Messager messager;

    public interface SelfClosable extends AutoCloseable {
        void close();
    }

    public static SelfClosable install(Messager messager) {
        Appender<ILoggingEvent> appender = new MessagerAppender(messager);
        Logger logger = (Logger)LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
        logger.addAppender(appender);
        return () -> logger.detachAppender(appender);
    }

    private MessagerAppender(Messager messager) {
        this.messager = messager;
    }

    @Override
    protected void append(ILoggingEvent eventObject) {
        Diagnostic.Kind kind = loglevelMap.get(eventObject.getLevel());
        this.messager.printMessage(kind, eventObject.getFormattedMessage());
    }
}
