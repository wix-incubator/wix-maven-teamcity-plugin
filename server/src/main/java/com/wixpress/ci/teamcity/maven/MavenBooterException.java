package com.wixpress.ci.teamcity.maven;

/**
 * @author yoav
 * @since 2/15/12
 */
public class MavenBooterException extends RuntimeException{
    public MavenBooterException(String message, Exception cause, Object... args) {
        super(String.format(message, args), cause);
    }

    public MavenBooterException(String message, Object... args) {
        super(String.format(message, args));
    }
}
