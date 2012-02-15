package com.wixpress.ci.teamcity.maven.workspace.fs;

import com.wixpress.ci.teamcity.maven.workspace.WorkspaceDir;
import com.wixpress.ci.teamcity.maven.workspace.WorkspaceFile;

import java.io.File;

/**
 * @author yoav
 * @since 2/15/12
 */
public class FSWorkspaceFile implements WorkspaceFile {
    private final File file;
    private WorkspaceDir parent;

    FSWorkspaceFile(WorkspaceDir dir, String fileName) {
        file = new File(((FSWorkspaceDir)dir).getDir(), fileName);
        parent = dir;
    }

    public boolean exists() {
        return file.exists();
    }

    public File getFile() {
        return file;
    }

    public WorkspaceDir getParent() {
        return parent;
    }

    @Override
    public String toString() {
        return new StringBuilder()
                .append("FSWorkspaceFile(")
                .append(file.getAbsoluteFile())
                .append(')')
                .toString();
    }
}
