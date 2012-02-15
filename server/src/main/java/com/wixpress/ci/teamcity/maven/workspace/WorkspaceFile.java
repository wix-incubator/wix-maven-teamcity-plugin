package com.wixpress.ci.teamcity.maven.workspace;

import java.io.File;

/**
 * @author yoav
 * @since 2/15/12
 */
public interface WorkspaceFile {

    public boolean exists();

    public File getFile();

    public WorkspaceDir getParent();
}
