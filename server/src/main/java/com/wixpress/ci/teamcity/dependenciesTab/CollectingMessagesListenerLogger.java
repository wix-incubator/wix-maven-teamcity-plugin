package com.wixpress.ci.teamcity.dependenciesTab;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.wixpress.ci.teamcity.maven.listeners.ListenerLogger;
import jetbrains.buildServer.serverSide.SBuildType;
import org.joda.time.DateTime;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

import static com.google.common.collect.Lists.newArrayList;

/**
 * @author yoav
 * @since 2/16/12
 */
public class CollectingMessagesListenerLogger implements ListenerLogger {

    private final List<ListenerMessage> messages = newArrayList();
    private DateTime lastMessageTime = new DateTime();

    public void info(String message) {
        synchronized (messages) {
            System.out.println(message);
            messages.add(new ListenerMessage(message, MessageType.info));
            lastMessageTime = new DateTime();
        }
    }

    public void progress(String message) {
        synchronized (messages) {
            System.out.println(message);
            messages.add(new ListenerMessage(message, MessageType.progress));
            lastMessageTime = new DateTime();
        }
    }

    public void error(String message) {
        synchronized (messages) {
            System.out.println(message);
            messages.add(new ListenerMessage(message, MessageType.error));
            lastMessageTime = new DateTime();
        }
    }

    public void error(String message, Exception e) {
        synchronized (messages) {
            System.out.println(message);
            e.printStackTrace();
            messages.add(new ListenerMessage(message, MessageType.error, e));
            lastMessageTime = new DateTime();
        }
    }

    public List<ListenerMessage> getMessages() {
        synchronized (messages) {
            return new ArrayList<ListenerMessage>(messages);
        }
    }

    public List<ListenerMessage> getMessages(Integer position) {
        synchronized (messages) {
            if (position < messages.size())
                return new ArrayList<ListenerMessage>(messages.subList(position, messages.size()));
            else
                return newArrayList();
        }
    }

    public void completedCollectingDependencies(SBuildType buildType) {
        synchronized (messages) {
            String message = String.format("completed collecting dependencies for %s:%s", buildType.getProject().getName(), buildType.getName());
            System.out.println(message);
            messages.add(new ListenerMessage(message, MessageType.info));
            lastMessageTime = new DateTime();
        }
    }

    public void failedCollectingDependencies(SBuildType buildType, Exception e) {
        synchronized (messages) {
            String message = String.format("failed collecting dependencies for %s:%s", buildType.getProject().getName(), buildType.getName());
            System.out.println(message);
            e.printStackTrace();
            messages.add(new ListenerMessage(message, MessageType.error, e));
            lastMessageTime = new DateTime();
        }
    }

    public static class ListenerMessage {
        private String messsage;
        private MessageType messageType;
        private ListenerException exception;

        public ListenerMessage() {
        }

        public ListenerMessage(String messsage, MessageType messageType) {
            this.messsage = messsage;
            this.messageType = messageType;
        }

        public ListenerMessage(String messsage, MessageType messageType, Exception exception) {
            this.messsage = messsage;
            this.messageType = messageType;
            this.exception = new ListenerException(exception);
        }

        public String getMesssage() {
            return messsage;
        }

        public void setMesssage(String messsage) {
            this.messsage = messsage;
        }

        public MessageType getMessageType() {
            return messageType;
        }

        public void setMessageType(MessageType messageType) {
            this.messageType = messageType;
        }

        public ListenerException getException() {
            return exception;
        }

        public void setException(ListenerException exception) {
            this.exception = exception;
        }
    }

    public static class ListenerException {
        private String exceptionClass;
        private String exceptionMessage;
        private ListenerException cause;

        public ListenerException() {
        }

        public ListenerException(Throwable exception) {
            this.exceptionClass = exception.getClass().getName();
            this.exceptionMessage = exception.getMessage();
            if (exception.getCause() != null && exception.getCause() != exception)
                cause = new ListenerException(exception.getCause());
        }

        public String getExceptionClass() {
            return exceptionClass;
        }

        public void setExceptionClass(String exceptionClass) {
            this.exceptionClass = exceptionClass;
        }

        public String getExceptionMessage() {
            return exceptionMessage;
        }

        public void setExceptionMessage(String exceptionMessage) {
            this.exceptionMessage = exceptionMessage;
        }

        public ListenerException getCause() {
            return cause;
        }

        public void setCause(ListenerException cause) {
            this.cause = cause;
        }
    }

    public enum MessageType {info, progress, error}
}
