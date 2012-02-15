package com.wixpress.ci.teamcity.maven.workspace.fs;

/**
 * @author yoav
 * @since 2/15/12
 */
public class BuildTypeWorkspaceFilesystemException extends RuntimeException{
    public BuildTypeWorkspaceFilesystemException(String message, Exception cause, Object... args) {
        super(String.format(message, args), cause);
    }

    public BuildTypeWorkspaceFilesystemException(String message, Object... args) {
        super(String.format(message, args));
    }
}
