package com.wixpress.ci.teamcity.maven;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.wixpress.ci.teamcity.domain.MDependency;
import com.wixpress.ci.teamcity.domain.MModule;
import com.wixpress.ci.teamcity.maven.workspace.MavenModule;
import org.apache.maven.model.building.ModelBuildingException;
import org.sonatype.aether.RepositorySystem;
import org.sonatype.aether.RepositorySystemSession;
import org.sonatype.aether.artifact.Artifact;
import org.sonatype.aether.collection.CollectRequest;
import org.sonatype.aether.collection.CollectResult;
import org.sonatype.aether.collection.DependencyCollectionException;
import org.sonatype.aether.graph.Dependency;
import org.sonatype.aether.graph.DependencyNode;
import org.sonatype.aether.repository.RemoteRepository;
import org.sonatype.aether.resolution.ArtifactDescriptorException;
import org.sonatype.aether.util.artifact.DefaultArtifact;

import java.io.IOException;
import java.util.List;

/**
 * Analyses the dependencies of a (multi-module) maven project regardless of TeamCity or other maven projects
 * @author yoav
 * @since 2/15/12
 */
public class MavenProjectDependenciesAnalyzer {
    
    private List<RemoteRepository> remoteRepositories;
    private RepositorySystem repositorySystem;

    public MavenProjectDependenciesAnalyzer(List<RemoteRepository> remoteRepositories, RepositorySystem repositorySystem) {
        this.remoteRepositories = remoteRepositories;
        this.repositorySystem = repositorySystem;
    }
    
    public MModule getModuleDependencies(MavenModule mavenModule, RepositorySystemSession session, DependenciesAnalyzerListener listener) throws ArtifactDescriptorException, IOException, DependencyCollectionException, ModelBuildingException {
        ModuleDependencies moduleDependencies = doGetModuleDependencies(mavenModule, session, listener);
        return toMModule(moduleDependencies);
        
    }

    private MModule toMModule(ModuleDependencies moduleDependencies) {
        MModule mModule = new MModule(moduleDependencies.getMavenModule());
        mModule.setDependencyTree(toMDependencies(moduleDependencies.getDependencyTree()));
        mModule.setSubModules(Lists.transform(moduleDependencies.getChildModuleDependencieses(), new Function<ModuleDependencies, MModule>() {
            public MModule apply(ModuleDependencies input) {
                return toMModule(input);
            }
        }));
        return mModule;
    }

    private MDependency toMDependencies(DependencyNode dependencyTree) {
        MDependency mDependency = new MDependency(
                dependencyTree.getDependency().getArtifact().getGroupId(),
                dependencyTree.getDependency().getArtifact().getArtifactId(),
                dependencyTree.getDependency().getArtifact().getVersion(),
                dependencyTree.getDependency().getScope(),
                dependencyTree.getDependency().isOptional());
        mDependency.setDependencies(Lists.transform(dependencyTree.getChildren(), new Function<DependencyNode, MDependency>() {
            public MDependency apply(DependencyNode input) {
                return toMDependencies(input);
            }
        }));
        return mDependency;
    }

    private ModuleDependencies doGetModuleDependencies(MavenModule mavenModule, RepositorySystemSession session, DependenciesAnalyzerListener listener) throws IOException, ModelBuildingException, ArtifactDescriptorException, DependencyCollectionException {
        DependencyNode moduleDependencyTree = getDependenciesOfModule(mavenModule, session, listener);
        ModuleDependencies moduleDependencies = new ModuleDependencies(mavenModule, moduleDependencyTree);
        for (MavenModule module: mavenModule.getSubModules()) {
            moduleDependencies.getChildModuleDependencieses().add(doGetModuleDependencies(module, session, listener));
        }
        return moduleDependencies;
    }
    
    private DependencyNode getDependenciesOfModule(MavenModule projectModule, RepositorySystemSession session, DependenciesAnalyzerListener listener) throws ArtifactDescriptorException, DependencyCollectionException {
        listener.collectedDependencies(projectModule);

        Artifact artifact = new DefaultArtifact(projectModule.getGroupId(), projectModule.getArtifactId(), "", "pom", projectModule.getVersion());

        CollectRequest collectRequest = new CollectRequest();
        collectRequest.setRoot(new Dependency(artifact, ""));
        for (RemoteRepository repo: remoteRepositories)
            collectRequest.addRepository( repo );

        CollectResult collectResult = repositorySystem.collectDependencies(session, collectRequest);

        listener.collectingDependencies(projectModule);
        return collectResult.getRoot();
    }

}
