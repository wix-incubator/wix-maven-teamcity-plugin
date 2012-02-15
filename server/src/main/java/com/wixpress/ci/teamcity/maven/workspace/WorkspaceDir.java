package com.wixpress.ci.teamcity.maven.workspace;

import com.sun.org.apache.xml.internal.utils.FastStringBuffer;

/**
 * @author yoav
 * @since 2/15/12
 */
public interface WorkspaceDir {

    public boolean exists();
    public WorkspaceDir getParent();

}
