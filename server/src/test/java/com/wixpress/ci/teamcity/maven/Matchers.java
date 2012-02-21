package com.wixpress.ci.teamcity.maven;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.wixpress.ci.teamcity.domain.IArtifact;
import com.wixpress.ci.teamcity.domain.MDependency;
import com.wixpress.ci.teamcity.domain.MModule;
import com.wixpress.ci.teamcity.maven.workspace.MavenModule;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.sonatype.aether.artifact.Artifact;
import org.sonatype.aether.graph.DependencyNode;

import java.util.List;

import static java.util.Arrays.asList;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.hasItem;

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
    
    public static Matcher<MModule> IsMModule(final String groupId, final String artifactId, final String version, final Matcher<Iterable<MDependency>> dependencies, final Matcher<Iterable<MModule>> subModules) {
        return new TypeSafeMatcher<MModule>() {
            @Override
            public boolean matchesSafely(MModule mavenModule) {
                return groupId.equals(mavenModule.getGroupId()) &&
                        artifactId.equals(mavenModule.getArtifactId()) &&
                        version.equals(mavenModule.getVersion()) &&
                        subModules.matches(mavenModule.getSubModules()) &&
                        dependencies.matches(mavenModule.getDependencyTree().getDependencies());

            }

            public void describeTo(Description description) {
                description.appendText("MModule(")
                        .appendText(groupId)
                        .appendText(":")
                        .appendText(artifactId)
                        .appendText(":")
                        .appendText(version)
                        .appendText("{dependencies:").appendDescriptionOf(dependencies).appendText(", ")
                        .appendText("subModules:").appendDescriptionOf(subModules).appendText("}")
                ;
            }
        };
    }

    public static Matcher<MDependency> IsMDependency(final String groupId, final String artifactId, final String version, final Matcher<Iterable<MDependency>> dependencies) {
        return new TypeSafeMatcher<MDependency>() {
            @Override
            public boolean matchesSafely(MDependency mavenModule) {
                return groupId.equals(mavenModule.getGroupId()) &&
                        artifactId.equals(mavenModule.getArtifactId()) &&
                        version.equals(mavenModule.getVersion()) &&
                        dependencies.matches(mavenModule.getDependencies());

            }

            public void describeTo(Description description) {
                description.appendText("MDependency(")
                        .appendText(groupId)
                        .appendText(":")
                        .appendText(artifactId)
                        .appendText(":")
                        .appendText(version)
                        .appendText("{dependencies:").appendDescriptionOf(dependencies).appendText(", ")
                ;
            }
        };
    }
    
    public static Matcher<Iterable<MModule>> subModules(Matcher<MModule> ... items) {
        Iterable<Matcher<? extends Iterable<MModule>>> transform = Iterables.transform(asList(items), new Function<Matcher<MModule>, Matcher<? extends Iterable<MModule>>>() {
            public Matcher<Iterable<MModule>> apply(Matcher<MModule> input) {
                return hasItem(input);
            }
        });
        return org.hamcrest.Matchers.allOf(transform);
    }
    
    public static Matcher<Iterable<MDependency>> dependencies(Matcher<Iterable<MDependency>> ... items) {
//        Iterable<Matcher<? extends Iterable<MDependency>>> transform = Iterables.transform(asList(items), new Function<Matcher<MDependency>, Matcher<? extends Iterable<MDependency>>>() {
//            public Matcher<Iterable<MDependency>> apply(Matcher<MDependency> input) {
//                return hasItem(input);
//            }
//        });
        return org.hamcrest.Matchers.allOf(items);
    }
}
