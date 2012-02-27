package com.wixpress.ci.teamcity.maven;

import com.wixpress.ci.teamcity.domain.*;
import com.wixpress.ci.teamcity.maven.workspace.MavenModule;
import org.hamcrest.*;

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
    
    public static class ArtifactTreeMatcher<T extends Tree<T>> {
        private List<Matcher<T>> treeMatchers = newArrayList();

        public ArtifactTreeMatcher<T> get(final Matcher<T> artifactMatcher) {
            treeMatchers.add(artifactMatcher);
            return this;
        }

        public Matcher<T> match(final Matcher<? extends T> leafMatcher) {
            return new ArtifactTreeMatcher2(leafMatcher, true);
        }

        public Matcher<T> notMatch(final Matcher<? extends T> leafMatcher) {
            return new ArtifactTreeMatcher2(leafMatcher, false);
        }

        private class ArtifactTreeMatcher2 extends TypeSafeMatcher<T> {
            private final Matcher<? extends T> leafMatcher;
            private final boolean positive;

            public ArtifactTreeMatcher2(Matcher<? extends T> leafMatcher, boolean positive) {
                this.leafMatcher = leafMatcher;
                this.positive = positive;
            }

            @Override
            public boolean matchesSafely(T item) {
                T leaf = findLeaf(treeMatchers, item);
                return leaf != null && hasItem(leafMatcher).matches(leaf.getChildren()) == positive;
            }

            private T findLeaf(List<Matcher<T>> treeMatchers, T item) {
                T subModule = item;
                for (Matcher<T> treeMatcher: treeMatchers) {
                    subModule = findChild(subModule, treeMatcher);
                    if (subModule == null)
                        return null;

                }
                return subModule;
            }

            private T findChild(T branch, Matcher<T> treeMatcher) {
                for (T subModule: branch.getChildren())
                    if (treeMatcher.matches(subModule)) {
                        return subModule;
                    }
                return null;
            }

            public void describeTo(Description description) {
                description.appendText("Tree Matcher - children path\n");
                String ident = "\t";
                for (Matcher<T> branch: treeMatchers) {
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
                            match(name, buildTypeDependency.getBuildTypeId().getName(), "name", this) &&
                            match(projectName, buildTypeDependency.getBuildTypeId().getProjectName(), "projectName", this) &&
                            match(buildTypeId, buildTypeDependency.getBuildTypeId().getBuildTypeId(), "buildTypeId", this) &&
                            match(projectId, buildTypeDependency.getBuildTypeId().getProjectId(), "projectId", this) ;
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
    
    public static Matcher<MBuildPlanItem> IsMBuildPlanItem(final Matcher<BuildTypeId> buildTypeIdMatcher, final boolean needsBuild, final Matcher<String> descriptionMatcher) {
        return new TypeSafeMatcher<MBuildPlanItem>() {

            @Override
            public boolean matchesSafely(MBuildPlanItem item) {
                return buildTypeIdMatcher.matches(item.getBuildTypeId()) &&
                        item.isNeedsBuild() == needsBuild &&
                        descriptionMatches(item);
            }

            private boolean descriptionMatches(MBuildPlanItem item) {
                return !needsBuild || (descriptionMatcher.matches(item.getDescription()));
            }

            public void describeTo(Description description) {
                description.appendText("MBuildPlanItem(")
                        .appendDescriptionOf(buildTypeIdMatcher)
                        .appendText(",");
                if (needsBuild)
                        description.appendText("needsBuild - ").appendDescriptionOf(descriptionMatcher);
                description.appendText(")");
            }
        };
    }

    public static Matcher<BuildTypeId> IsBuildTypeId(final String name, final String projectName, final String buildTypeId, final String projectId) {
        return new TypeSafeMatcher<BuildTypeId>() {
            @Override
            public boolean matchesSafely(BuildTypeId item) {
                return item.getBuildTypeId().equals(buildTypeId) &&
                        item.getName().equals(name) &&
                        item.getProjectId().equals(projectId) &&
                        item.getProjectName().equals(projectName);
            }

            public void describeTo(Description description) {
                description.appendText("IsBuildTypeId(")
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

}
