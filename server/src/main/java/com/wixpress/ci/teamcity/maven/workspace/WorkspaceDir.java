package com.wixpress.ci.teamcity.maven.workspace;

/**
 * @author yoav
 * @since 2/15/12
 */
public interface WorkspaceDir {

    public boolean exists();
    public WorkspaceDir getParent();

}
