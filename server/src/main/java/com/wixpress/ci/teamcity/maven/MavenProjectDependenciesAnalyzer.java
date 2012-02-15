package com.wixpress.ci.teamcity.maven;

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

import static com.google.common.collect.Lists.newArrayList;

/**
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

    public ModuleDependencies getModuleDependencies(MavenModule mavenModule, RepositorySystemSession session) throws IOException, ModelBuildingException, ArtifactDescriptorException, DependencyCollectionException {
        DependencyNode moduleDependencyTree = getDependenciesOfModule(mavenModule, session);
        ModuleDependencies moduleDependencies = new ModuleDependencies(mavenModule, moduleDependencyTree);
        for (MavenModule module: mavenModule.getSubModules()) {
            moduleDependencies.getChildModuleDependencieses().add(getModuleDependencies(module, session));
        }
        return moduleDependencies;
    }
    
    private DependencyNode getDependenciesOfModule(MavenModule projectModule, RepositorySystemSession session) throws ArtifactDescriptorException, DependencyCollectionException {
        System.out.println( "------------------------------------------------------------" );
        System.out.printf("%s:%s:%s\n", projectModule.getGroupId(), projectModule.getArtifactId(), projectModule.getVersion());

        Artifact artifact = new DefaultArtifact(projectModule.getGroupId(), projectModule.getArtifactId(), "", "pom", projectModule.getVersion());

        CollectRequest collectRequest = new CollectRequest();
        collectRequest.setRoot(new Dependency(artifact, ""));
        for (RemoteRepository repo: remoteRepositories)
            collectRequest.addRepository( repo );

        CollectResult collectResult = repositorySystem.collectDependencies(session, collectRequest);

        return collectResult.getRoot();
    }

}
