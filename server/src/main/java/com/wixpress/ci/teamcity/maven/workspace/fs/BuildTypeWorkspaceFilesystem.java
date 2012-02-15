package com.wixpress.ci.teamcity.maven.workspace.fs;

import com.wixpress.ci.teamcity.maven.workspace.WorkspaceDir;
import com.wixpress.ci.teamcity.maven.workspace.WorkspaceFile;
import com.wixpress.ci.teamcity.maven.workspace.WorkspaceFilesystem;
import jetbrains.buildServer.serverSide.SBuildType;
import jetbrains.buildServer.vcs.VcsException;
import org.codehaus.plexus.util.FileUtils;

import java.io.Closeable;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.UUID;

/**
 * @author yoav
 * @since 2/15/12
 */
public class BuildTypeWorkspaceFilesystem implements WorkspaceFilesystem, Closeable {

    private SBuildType buildType;
    private File tempDir;
    private File rootDir;

    public BuildTypeWorkspaceFilesystem(File tempDir, SBuildType buildType) {
        this.buildType = buildType;
        this.tempDir = tempDir;
        this.rootDir = new File(tempDir, UUID.randomUUID().toString());
    }

    public WorkspaceDir getRoot() {
        FSWorkspaceDir dir = new FSWorkspaceDir(rootDir);
        createDir(dir);
        return dir;
    }

    public WorkspaceFile newFile(WorkspaceDir root, String fileName) {
        FSWorkspaceFile file = new FSWorkspaceFile(root, fileName);
        extractFile(file);
        return file;
    }

    public WorkspaceDir newDir(WorkspaceDir projectDir, String dirName) {
        FSWorkspaceDir dir = new FSWorkspaceDir(projectDir, dirName);
        createDir(dir);
        return dir;
    }

    private void extractFile(FSWorkspaceFile file) {
        String relativePath = file.getRelativePath();
        try {
            byte[] fileContent = buildType.getFileContent(relativePath);
            FileOutputStream fs = new FileOutputStream(file.getFile());
            try {
                fs.write(fileContent);
                fs.flush();
            } finally {
                fs.close();
            }
        } catch (IOException e) {
            throw new BuildTypeWorkspaceFilesystemException("failed extracting file [%s] for build [%s]", e, relativePath, buildType.getName());
        } catch (VcsException e) {
            throw new BuildTypeWorkspaceFilesystemException("failed extracting file [%s] for build [%s]", e, relativePath, buildType.getName());
        }
    }

    private void createDir(FSWorkspaceDir dir) {
        dir.getDir().mkdirs();
    }

    public void close() throws IOException {
        FileUtils.deleteDirectory(rootDir);
    }
}
