package com.wixpress.ci.teamcity.maven.workspace;

import com.wixpress.ci.teamcity.maven.MavenBooter;
import com.wixpress.ci.teamcity.maven.workspace.fs.FSWorkspaceFilesystem;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.Matchers;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Test;
import org.sonatype.aether.util.artifact.DefaultArtifact;

import java.io.File;
import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

/**
 * @author yoav
 * @since 2/15/12
 */
public class MavenWorkspaceReaderTest {
    
    MavenBooter mavenBooter = new MavenBooter();
    File repositoryRoot = new File("src/test/resources/projB");
    WorkspaceFilesystem workspaceFilesystem = new FSWorkspaceFilesystem(repositoryRoot);
    
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
        assertThat(workspaceReader.findArtifact(new DefaultArtifact("com.sonatype.example", "projB", "", "pom", "1.0.0-SNAPSHOT")).getAbsoluteFile(),
                is(new File(repositoryRoot, "pom.xml").getAbsoluteFile()));
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

    private Matcher<MavenModule> IsMavenModule(final String groupId, final String artifactId, final String version) {
        return new TypeSafeMatcher<MavenModule>() {
            @Override
            public boolean matchesSafely(MavenModule mavenModule) {
                return groupId.equals(mavenModule.getGroupId()) &&
                        artifactId.equals(mavenModule.getArtifactId()) &&
                        version.equals(mavenModule.getVersion());

            }

            public void describeTo(Description description) {
                description.appendText("MavenModule(")
                        .appendText(groupId)
                        .appendText(":")
                        .appendText(artifactId)
                        .appendText(":")
                        .appendText(version);
            }
        };
    }
}
