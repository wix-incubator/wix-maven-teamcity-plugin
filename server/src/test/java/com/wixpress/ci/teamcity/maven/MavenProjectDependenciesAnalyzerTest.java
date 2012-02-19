package com.wixpress.ci.teamcity.maven;

import com.wixpress.ci.teamcity.domain.MDependency;
import com.wixpress.ci.teamcity.domain.MModule;
import com.wixpress.ci.teamcity.maven.listeners.*;
import com.wixpress.ci.teamcity.maven.workspace.MavenWorkspaceReader;
import com.wixpress.ci.teamcity.maven.workspace.MavenWorkspaceReaderException;
import com.wixpress.ci.teamcity.maven.workspace.WorkspaceFilesystem;
import com.wixpress.ci.teamcity.maven.workspace.fs.FSWorkspaceFilesystem;
import org.apache.maven.model.building.ModelBuildingException;
import org.apache.maven.repository.internal.MavenRepositorySystemSession;
import org.junit.Test;
import org.sonatype.aether.collection.DependencyCollectionException;
import org.sonatype.aether.resolution.ArtifactDescriptorException;

import java.io.File;
import java.io.IOException;

import static com.wixpress.ci.teamcity.maven.Matchers.IsArtifact;
import static com.wixpress.ci.teamcity.maven.Matchers.IsDependencyNode;
import static com.wixpress.ci.teamcity.maven.Matchers.IsMavenModule;
import static org.hamcrest.Matchers.hasItem;
import static org.junit.Assert.assertThat;

/**
 * @author yoav
 * @since 2/15/12
 */
public class MavenProjectDependenciesAnalyzerTest {
    
    MavenBooter mavenBooter = new MavenBooter();
    MavenProjectDependenciesAnalyzer mavenProjectDependenciesAnalyzer = new MavenProjectDependenciesAnalyzer(mavenBooter.remoteRepositories(), mavenBooter.repositorySystem());
    
    ListenerLogger listenerLogger = new ListenerLogger() {
        public void info(String message) {
            System.out.println(message);
        }

        public void progress(String message) {
            System.out.println(message);
        }

        public void error(String message) {
            System.err.println(message);
        }

        public void error(String message, Exception e) {
            System.err.println(message);
            e.printStackTrace();
        }
    };
    
    @Test
    public void getDependenciesOfProjA() throws MavenWorkspaceReaderException, ArtifactDescriptorException, IOException, DependencyCollectionException, ModelBuildingException {
        File repositoryRoot = new File("src/test/resources/projA");
        WorkspaceFilesystem workspaceFilesystem = new FSWorkspaceFilesystem(repositoryRoot);
        MavenWorkspaceReader workspaceReader =  mavenBooter.newWorkspaceReader(workspaceFilesystem, new LoggingMavenWorkspaceListener(listenerLogger));
        MavenRepositorySystemSession session = mavenBooter.newRepositorySystemSession(new LoggingTransferListener(listenerLogger), new LoggingRepositoryListener(listenerLogger));
        session.setWorkspaceReader(workspaceReader);

        MModule mModule = mavenProjectDependenciesAnalyzer.getModuleDependencies(workspaceReader.getRootModule(), session);

        assertThat(mModule, IsArtifact("com.sonatype.example", "projA", "1.0.0-SNAPSHOT"));
        assertThat(mModule.getDependencyTree(), IsArtifact("com.sonatype.example", "projA", "1.0.0-SNAPSHOT"));
        assertThat(mModule.getDependencyTree().getDependencies(), hasItem(Matchers.<MDependency>IsArtifact("org.apache.maven", "maven-model", "3.0.4")));


        mModule.accept(new LoggingModuleVisitor(listenerLogger));
    } 
    
    @Test
    public void getDependenciesOfProjB() throws MavenWorkspaceReaderException, ArtifactDescriptorException, IOException, DependencyCollectionException, ModelBuildingException {
        File repositoryRoot = new File("src/test/resources/projB");
        WorkspaceFilesystem workspaceFilesystem = new FSWorkspaceFilesystem(repositoryRoot);
        MavenWorkspaceReader workspaceReader =  mavenBooter.newWorkspaceReader(workspaceFilesystem, new LoggingMavenWorkspaceListener(listenerLogger));
        MavenRepositorySystemSession session = mavenBooter.newRepositorySystemSession(new LoggingTransferListener(listenerLogger), new LoggingRepositoryListener(listenerLogger));
        session.setWorkspaceReader(workspaceReader);

        MModule mModule = mavenProjectDependenciesAnalyzer.getModuleDependencies(workspaceReader.getRootModule(), session);

        assertThat(mModule, IsArtifact("com.sonatype.example", "projB", "1.0.0-SNAPSHOT"));
        assertThat(mModule.getDependencyTree(), IsArtifact("com.sonatype.example", "projB", "1.0.0-SNAPSHOT"));
        assertThat(mModule.getSubModules(), hasItem(Matchers.<MModule>IsArtifact("com.sonatype.example", "moduleA", "1.0.0-SNAPSHOT")));
        assertThat(mModule.getSubModules(), hasItem(Matchers.<MModule>IsArtifact("com.sonatype.example", "moduleB", "1.0.0-SNAPSHOT")));

        mModule.accept(new LoggingModuleVisitor(listenerLogger));
    }
}
