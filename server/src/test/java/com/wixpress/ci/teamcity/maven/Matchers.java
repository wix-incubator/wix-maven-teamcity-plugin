package com.wixpress.ci.teamcity.maven;

import com.wixpress.ci.teamcity.domain.IArtifact;
import com.wixpress.ci.teamcity.domain.MBuildTypeDependency;
import com.wixpress.ci.teamcity.domain.MDependency;
import com.wixpress.ci.teamcity.domain.MModule;
import com.wixpress.ci.teamcity.maven.workspace.MavenModule;
import org.hamcrest.*;
import org.sonatype.aether.graph.DependencyNode;

import java.util.List;

import static com.google.common.collect.Lists.newArrayList;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.nullValue;

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

    public static <M extends IArtifact> Matcher<M> IsArtifact(final String groupId, final String artifactId, final String version) {
        return new TypeSafeMatcher<M>() {
            @Override
            public boolean matchesSafely(M mavenModule) {
                return groupId.equals(mavenModule.getGroupId()) &&
                        artifactId.equals(mavenModule.getArtifactId()) &&
                        version.equals(mavenModule.getVersion());

            }

            public void describeTo(Description description) {
                description.appendText("Artifact(")
                        .appendText(groupId)
                        .appendText(":")
                        .appendText(artifactId)
                        .appendText(":")
                        .appendText(version)
                        .appendText(")");
            }
        };
    }
    
    public static Matcher<MModule> IsModule(final String groupId, final String artifactId, final String version) {
        return IsArtifact(groupId, artifactId, version);
    }
    
    public static ArtifactTreeMatcher ArtifactTreeMatcher() {
        return new ArtifactTreeMatcher();
    }
    public static class ArtifactTreeMatcher {
        private List<Matcher<? extends IArtifact>> treeMatchers = newArrayList();

        public ArtifactTreeMatcher get(final Matcher<? extends IArtifact> artifactMatcher) {
            treeMatchers.add(artifactMatcher);
            return this;
        }

        public Matcher<MModule> match(final Matcher<? extends IArtifact> leafMatcher) {
            return new ArtifactTreeMatcher2(leafMatcher, true);
        }

        public Matcher<MModule> notMatch(final Matcher<? extends IArtifact> leafMatcher) {
            return new ArtifactTreeMatcher2(leafMatcher, false);
        }

        private class ArtifactTreeMatcher2 extends TypeSafeMatcher<MModule> {
            private final Matcher<? extends IArtifact> leafMatcher;
            private final boolean positive;

            public ArtifactTreeMatcher2(Matcher<? extends IArtifact> leafMatcher, boolean positive) {
                this.leafMatcher = leafMatcher;
                this.positive = positive;
            }

            @Override
            public boolean matchesSafely(MModule item) {
                IArtifact leaf = findLeaf(treeMatchers, item);
                return leaf != null && hasItem(leafMatcher).matches(leaf.getChildren()) == positive;
            }

            private IArtifact findLeaf(List<Matcher<? extends IArtifact>> treeMatchers, IArtifact item) {
                IArtifact subModule = item;
                for (Matcher<? extends IArtifact> treeMatcher: treeMatchers) {
                    subModule = findChild(subModule, treeMatcher);
                    if (subModule == null)
                        return null;

                }
                return subModule;
            }

            private IArtifact findChild(IArtifact branch, Matcher<? extends IArtifact> treeMatcher) {
                for (IArtifact subModule: branch.getChildren())
                    if (treeMatcher.matches(subModule)) {
                        return subModule;
                    }
                return null;
            }

            public void describeTo(Description description) {
                description.appendText("ArtifactTreeMatcher\n");
                String ident = "\t";
                for (Matcher<? extends IArtifact> branch: treeMatchers) {
                    description.appendText(ident).appendDescriptionOf(branch).appendText("\n");
                    ident += "\t";
                }
                description.appendText(ident).appendDescriptionOf(leafMatcher);
            }
        }
    }

    public static Matcher<MDependency> IsMDependency(final String groupId, final String artifactId, final String version) {
        return new TypeSafeMatcher<MDependency>() {
            @Override
            public boolean matchesSafely(MDependency mavenModule) {
                boolean match = match(groupId, mavenModule.getGroupId(), "groupId", this) &&
                        match(artifactId, mavenModule.getArtifactId(), "artifactId", this) &&
                        match(version, mavenModule.getVersion(), "version", this);
                if (!match)
                    System.out.println("failed match on:" + StringDescription.asString(this));
                return match;

            }

            public void describeTo(Description description) {
                description.appendText("MDependency(")
                        .appendText(groupId)
                        .appendText(":")
                        .appendText(artifactId)
                        .appendText(":")
                        .appendText(version)
                ;
            }
        };
    }

    public static Matcher<MDependency> IsMBuildTypeDependency (
            final String groupId,
            final String artifactId,
            final String version,
            final String projectName,
            final String name,
            final String projectId,
            final String buildTypeId) {
        return new TypeSafeMatcher<MDependency>() {
            @Override
            public boolean matchesSafely(MDependency mavenModule) {
                if (mavenModule instanceof MBuildTypeDependency) {
                    MBuildTypeDependency buildTypeDependency = (MBuildTypeDependency)mavenModule;
                    boolean match = match(groupId, mavenModule.getGroupId(), "groupId", this) &&
                            match(artifactId, mavenModule.getArtifactId(), "artifactId", this) &&
                            match(version, mavenModule.getVersion(), "version", this) &&
                            match(name, buildTypeDependency.getName(), "name", this) &&
                            match(projectName, buildTypeDependency.getProjectName(), "projectName", this) &&
                            match(buildTypeId, buildTypeDependency.getBuildTypeId(), "buildTypeId", this) &&
                            match(projectId, buildTypeDependency.getProjectId(), "projectId", this) ;
                    if (!match)
                        System.out.println("failed match on:" + StringDescription.asString(this));
                    return match;
                }
                else {
                    return false;
                }
            }

            public void describeTo(Description description) {
                description.appendText("MBuildTypeDependency(")
                        .appendText(groupId)
                        .appendText(":")
                        .appendText(artifactId)
                        .appendText(":")
                        .appendText(version)
                        .appendText("-")
                        .appendText(projectName)
                        .appendText(":")
                        .appendText(name)
                        .appendText("(")
                        .appendText(projectId)
                        .appendText("-")
                        .appendText(buildTypeId)
                        .appendText(")")
                ;
            }
        };
    }

    private static <V> boolean match(V value, V toValue, String field, SelfDescribing parentMatcher) {
        boolean match = value.equals(toValue);
        if (!match)
            System.out.println(String.format("failed match [%s=%s] on [%s]", field, value, StringDescription.asString(parentMatcher)));
        return match;
    }

    private static <V> boolean match(Matcher<V> value, V toValue, String field, SelfDescribing parentMatcher) {
        boolean match = value.matches(toValue);
        if (!match)
            System.out.println(String.format("failed match [%s=%s] on [%s]", field, value, StringDescription.asString(parentMatcher)));
        return match;
    }
    
    
}
