package com.wixpress.ci.teamcity.maven.workspace;

import com.wixpress.ci.teamcity.maven.MavenBooter;
import com.wixpress.ci.teamcity.maven.workspace.fs.BuildTypeWorkspaceFilesystem;
import com.wixpress.ci.teamcity.maven.workspace.fs.BuildTypeWorkspaceFilesystemException;
import com.wixpress.ci.teamcity.maven.workspace.fs.FSWorkspaceFilesystem;
import jetbrains.buildServer.serverSide.SBuildType;
import jetbrains.buildServer.util.FileUtil;
import jetbrains.buildServer.vcs.VcsException;
import org.codehaus.plexus.util.FileUtils;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.Matchers;
import org.hamcrest.TypeSafeMatcher;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.sonatype.aether.util.artifact.DefaultArtifact;

import java.io.*;

import static com.wixpress.ci.teamcity.maven.Matchers.IsMavenModule;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author yoav
 * @since 2/15/12
 */
public class BuildTypeMavenWorkspaceReaderTest {

    MavenBooter mavenBooter = new MavenBooter();
    File repositoryRoot = new File("src/test/resources/projB");
    SBuildType buildType = mock(SBuildType.class);
    BuildTypeWorkspaceFilesystem workspaceFilesystem = new BuildTypeWorkspaceFilesystem(new File(System.getProperty( "java.io.tmpdir" )), buildType);

    @Before
    public void setupMock() throws VcsException, IOException {
        when(buildType.getFileContent("pom.xml")).thenReturn(readFile("pom.xml"));
        when(buildType.getFileContent("moduleA/pom.xml")).thenReturn(readFile("moduleA/pom.xml"));
        when(buildType.getFileContent("moduleB/pom.xml")).thenReturn(readFile("moduleB/pom.xml"));
    }

    private byte[] readFile(String relativePath) throws IOException {
        FileInputStream in = new FileInputStream("src/test/resources/projB/"+relativePath);
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int len;
            while ((len = in.read(buffer)) > 0) {
                out.write(buffer, 0, len);
            }
            return out.toByteArray();
        }
        finally {
            in.close();
        }

    }

    @Test
    public void testGetMavenModules() throws MavenWorkspaceReaderException {
        MavenWorkspaceReader workspaceReader = new MavenWorkspaceReader(workspaceFilesystem, mavenBooter.modelProcessor());
        assertThat(workspaceReader.getMavenModules(), allOf(
                hasItem(IsMavenModule("com.sonatype.example", "projB", "1.0.0-SNAPSHOT")),
                hasItem(IsMavenModule("com.sonatype.example", "moduleB", "1.0.0-SNAPSHOT")),
                hasItem(IsMavenModule("com.sonatype.example", "moduleA", "1.0.0-SNAPSHOT"))));
    }

    @Test
    public void testGetRootModules() throws MavenWorkspaceReaderException {
        MavenWorkspaceReader workspaceReader = new MavenWorkspaceReader(workspaceFilesystem, mavenBooter.modelProcessor());
        assertThat(workspaceReader.getRootModule(), IsMavenModule("com.sonatype.example", "projB", "1.0.0-SNAPSHOT"));
    }

    @Test
    public void testFindArtifactPositive() throws MavenWorkspaceReaderException {
        MavenWorkspaceReader workspaceReader = new MavenWorkspaceReader(workspaceFilesystem, mavenBooter.modelProcessor());
        assertThat(workspaceReader.findArtifact(new DefaultArtifact("com.sonatype.example", "projB", "", "pom", "1.0.0-SNAPSHOT")).getName(),
                is(new File(repositoryRoot, "pom.xml").getName()));
    }

    @Test
    public void testFindArtifactNegative() throws MavenWorkspaceReaderException {
        MavenWorkspaceReader workspaceReader = new MavenWorkspaceReader(workspaceFilesystem, mavenBooter.modelProcessor());
        assertThat(workspaceReader.findArtifact(new DefaultArtifact("com.sonatype.example", "non-existing", "", "pom", "1.0.0-SNAPSHOT")),
                nullValue());
    }

    @Test
    public void testFindVersionsPositive() throws MavenWorkspaceReaderException {
        MavenWorkspaceReader workspaceReader = new MavenWorkspaceReader(workspaceFilesystem, mavenBooter.modelProcessor());
        assertThat(workspaceReader.findVersions(new DefaultArtifact("com.sonatype.example", "projB", "", "pom", "1.0.0-SNAPSHOT")),
                hasItem("1.0.0-SNAPSHOT"));
    }

    @Test
    public void testFindVersionsNegative() throws MavenWorkspaceReaderException {
        MavenWorkspaceReader workspaceReader = new MavenWorkspaceReader(workspaceFilesystem, mavenBooter.modelProcessor());
        assertThat(workspaceReader.findVersions(new DefaultArtifact("com.sonatype.example", "non-existing", "", "pom", "1.0.0-SNAPSHOT")).size(),
                is(0));
    }

    @Test
    public void testGetRepository() throws MavenWorkspaceReaderException {
        MavenWorkspaceReader workspaceReader = new MavenWorkspaceReader(workspaceFilesystem, mavenBooter.modelProcessor());
        assertThat(workspaceReader.getRepository().getKey(),
                Matchers.<Object>is("com.sonatype.example:projB:pom:1.0.0-SNAPSHOT"));
    }

    @After
    public void cleanFiles() throws IOException {
        workspaceFilesystem.close();
    }

}
