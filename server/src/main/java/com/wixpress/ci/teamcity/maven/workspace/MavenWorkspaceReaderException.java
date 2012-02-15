package com.wixpress.ci.teamcity.maven.workspace;

/**
 * @author yoav
 * @since 2/14/12
 */
public class MavenWorkspaceReaderException extends Exception {
    public MavenWorkspaceReaderException(String message, Exception cause, Object... args) {
        super(String.format(message, args), cause);
    }

    public MavenWorkspaceReaderException(String message, Object... args) {
        super(String.format(message, args));
    }
}
