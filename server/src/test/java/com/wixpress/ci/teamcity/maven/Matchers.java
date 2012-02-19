package com.wixpress.ci.teamcity.maven;

import com.wixpress.ci.teamcity.domain.IArtifact;
import com.wixpress.ci.teamcity.maven.workspace.MavenModule;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.sonatype.aether.artifact.Artifact;
import org.sonatype.aether.graph.DependencyNode;

/**
 * @author yoav
 * @since 2/15/12
 */
public class Matchers {

    public static Matcher<MavenModule> IsMavenModule(final String groupId, final String artifactId, final String version) {
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

    public static Matcher<DependencyNode> IsDependencyNode(final String groupId, final String artifactId, final String version) {
        return new TypeSafeMatcher<DependencyNode>() {
            @Override
            public boolean matchesSafely(DependencyNode mavenModule) {
                return groupId.equals(mavenModule.getDependency().getArtifact().getGroupId()) &&
                        artifactId.equals(mavenModule.getDependency().getArtifact().getArtifactId()) &&
                        version.equals(mavenModule.getDependency().getArtifact().getVersion());

            }

            public void describeTo(Description description) {
                description.appendText("DependencyNode(")
                        .appendText(groupId)
                        .appendText(":")
                        .appendText(artifactId)
                        .appendText(":")
                        .appendText(version);
            }
        };
    }

    public static <M extends IArtifact> Matcher<M> IsArtifact(final String groupId, final String artifactId, final String version) {
        return new TypeSafeMatcher<M>() {
            @Override
            public boolean matchesSafely(M mavenModule) {
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
