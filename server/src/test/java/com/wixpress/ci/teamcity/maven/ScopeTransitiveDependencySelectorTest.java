package com.wixpress.ci.teamcity.maven;

import org.junit.Test;
import org.sonatype.aether.RepositorySystemSession;
import org.sonatype.aether.collection.DependencyCollectionContext;
import org.sonatype.aether.collection.DependencySelector;
import org.sonatype.aether.graph.Dependency;
import org.sonatype.aether.util.artifact.DefaultArtifact;

import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

/**
 * @author yoav
 * @since 2/21/12
 */
public class ScopeTransitiveDependencySelectorTest {

    DependencySelector selector = new ScopeTransitiveDependencySelector("test");
    Dependency compile = new Dependency(new DefaultArtifact("group", "compile", "", "1.2"), "compile");
    Dependency test = new Dependency(new DefaultArtifact("group", "test", "", "1.2"), "test");

    @Test
    public void compileInc() {
        assertThat(selector.selectDependency(compile), is(true));
    }

    @Test
    public void testInc() {
        assertThat(selector.selectDependency(test), is(true));
    }

    @Test
    public void compileCompileInc() {
        selector = selector.deriveChildSelector(context(compile));
        assertThat(selector.selectDependency(compile), is(true));
    }

    @Test
    public void compileTestExc() {
        selector = selector.deriveChildSelector(context(compile));
        assertThat(selector.selectDependency(test), is(true));
    }

    @Test
    public void CompileCompileCompileExc() {
        selector = selector.deriveChildSelector(context(compile));
        selector = selector.deriveChildSelector(context(compile));
        assertThat(selector.selectDependency(compile), is(true));
    }

    @Test
    public void CompileCompileTestExc() {
        selector = selector.deriveChildSelector(context(compile));
        selector = selector.deriveChildSelector(context(compile));
        assertThat(selector.selectDependency(test), is(false));
    }

    @Test
    public void CompileTestCompileExc() {
        selector = selector.deriveChildSelector(context(compile));
        selector = selector.deriveChildSelector(context(test));
        assertThat(selector.selectDependency(compile), is(false));
    }

    private DependencyCollectionContext context(final Dependency dependency) {
        return new DependencyCollectionContext() {
            public RepositorySystemSession getSession() {
                return null;
            }

            public Dependency getDependency() {
                return dependency;
            }

            public List<Dependency> getManagedDependencies() {
                return null;
            }
        };
    }
}
