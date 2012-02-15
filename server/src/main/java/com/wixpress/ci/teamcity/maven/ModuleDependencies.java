package com.wixpress.ci.teamcity.maven;

import com.wixpress.ci.teamcity.maven.workspace.MavenModule;
import org.sonatype.aether.graph.DependencyNode;

import java.util.List;

import static com.google.common.collect.Lists.newArrayList;

/**
* @author yoav
* @since 2/15/12
*/
public class ModuleDependencies {
    private MavenModule mavenModule;
    private DependencyNode dependencyTree;
    private List<ModuleDependencies> childModuleDependencieses = newArrayList();

    public ModuleDependencies(MavenModule mavenModule, DependencyNode dependencyTree) {
        this.mavenModule = mavenModule;
        this.dependencyTree = dependencyTree;
    }

    public MavenModule getMavenModule() {
        return mavenModule;
    }

    public DependencyNode getDependencyTree() {
        return dependencyTree;
    }

    public List<ModuleDependencies> getChildModuleDependencieses() {
        return childModuleDependencieses;
    }
}
