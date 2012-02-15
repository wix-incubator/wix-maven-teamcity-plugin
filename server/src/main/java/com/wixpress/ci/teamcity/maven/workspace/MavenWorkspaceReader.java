package com.wixpress.ci.teamcity.maven.workspace;

import com.google.common.collect.Lists;
import org.apache.maven.model.InputSource;
import org.apache.maven.model.Model;
import org.apache.maven.model.building.FileModelSource;
import org.apache.maven.model.building.ModelBuildingException;
import org.apache.maven.model.building.ModelProcessor;
import org.sonatype.aether.artifact.Artifact;
import org.sonatype.aether.repository.WorkspaceReader;
import org.sonatype.aether.repository.WorkspaceRepository;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.google.common.collect.Lists.newArrayList;

/**
 * @author yoav
 * @since 2/14/12
 */
public class MavenWorkspaceReader implements WorkspaceReader {

    private WorkspaceRepository workspaceRepository;
    private ModelProcessor modelProcessor;
    private List<MavenModule> mavenModules = newArrayList();
    private MavenModule rootModule;
    private MavenWorkspaceListener listener;
    private WorkspaceFilesystem workspaceFilesystem;

    private static final MavenWorkspaceListener NULL_LISTENER = (MavenWorkspaceListener)Proxy
            .newProxyInstance(MavenWorkspaceReader.class.getClassLoader(), new Class<?>[]{MavenWorkspaceListener.class}, new InvocationHandler() {
                public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                    return null;//do nothing
                }
            });

    public MavenWorkspaceReader(WorkspaceFilesystem workspaceFilesystem, ModelProcessor modelProcessor, MavenWorkspaceListener listener) throws MavenWorkspaceReaderException {
        this.modelProcessor = modelProcessor;
        this.listener = listener;
        this.workspaceFilesystem = workspaceFilesystem;

        init();
    }

    public MavenWorkspaceReader(WorkspaceFilesystem workspaceFilesystem, ModelProcessor modelProcessor) throws MavenWorkspaceReaderException {
        this(workspaceFilesystem, modelProcessor, NULL_LISTENER);
    }

    private void init() throws MavenWorkspaceReaderException {
        WorkspaceFile projectPom = workspaceFilesystem.newFile(workspaceFilesystem.getRoot(), "pom.xml");
        try {
            if (projectPom.exists()) {
                Model model = loadLocalPom(projectPom);
                listener.loadedRootPomFile(model, projectPom);
                rootModule = readModuleToArtifacts(model, projectPom.getParent());
                workspaceRepository = new WorkspaceRepository("project-workspace-repository", model.getId());
            }
            else {
                listener.projectRootPomNotFound(projectPom);
                throw new MavenWorkspaceReaderException("failed finding project root pom [%s]", projectPom);
            }
        } catch (ModelBuildingException e) {
            listener.failureReadingPom(projectPom, e);
            throw new MavenWorkspaceReaderException("failed reading project root pom [%s]", e, projectPom);
        } catch (IOException e) {
            listener.failureReadingPom(projectPom, e);
            throw new MavenWorkspaceReaderException("failed reading project root pom [%s]", e, projectPom);
        }

    }

    private MavenModule readModuleToArtifacts(Model model, WorkspaceDir projectDir) {
        MavenModule mavenModule = toProjectModule(model);
        mavenModules.add(mavenModule);
        for (String module: model.getModules()) {
            WorkspaceDir moduleProjectDir = workspaceFilesystem.newDir(projectDir, module);
            WorkspaceFile modulePomFile = workspaceFilesystem.newFile(moduleProjectDir, "pom.xml");
            if (moduleProjectDir.exists() && modulePomFile.exists()) {
                try {
                    Model moduleModel = loadLocalPom(modulePomFile);
                    listener.loadedModulePomFile(model, module, modulePomFile);
                    mavenModule.addSubModule(readModuleToArtifacts(moduleModel, moduleProjectDir));
                } catch (ModelBuildingException e) {
                    listener.failureReadingModulePom(model.getId(), module, modulePomFile, e);
                } catch (IOException e) {
                    listener.failureReadingModulePom(model.getId(), module, modulePomFile, e);
                }
            }
            else if (!moduleProjectDir.exists())
                listener.moduleDirNotFound(model.getId(), module, moduleProjectDir);
            else
                listener.modulePomFileNotFound(model.getId(), module, moduleProjectDir);
        }
        return mavenModule;
    }

    private MavenModule toProjectModule(Model model) {
        return new MavenModule(model.getGroupId(), model.getArtifactId(), model.getVersion(), model.getPomFile());
    }

    private Model loadLocalPom(WorkspaceFile pomFile) throws ModelBuildingException, IOException {
        InputSource source = new InputSource();
        FileModelSource modelSource = new FileModelSource(pomFile.getFile());

        Map<String, Object> options = new HashMap<String, Object>();
        options.put( ModelProcessor.IS_STRICT, false );
        options.put( ModelProcessor.INPUT_SOURCE, source );
        options.put( ModelProcessor.SOURCE, modelSource );

        return modelProcessor.read(pomFile.getFile(), options);
    }


    public WorkspaceRepository getRepository() {
        return workspaceRepository;
    }

    public File findArtifact(Artifact artifact) {
        MavenModule mavenModule = findModule(artifact);
        if (mavenModule != null)
            return mavenModule.getPomFile();
        else
            return null;
    }

    private MavenModule findModule(Artifact artifact) {
        for (MavenModule mavenModule : mavenModules) {
            if (mavenModule.getGroupId().equals(artifact.getGroupId()) &&
                    mavenModule.getArtifactId().equals(artifact.getArtifactId()) &&
                    mavenModule.getVersion().equals(artifact.getVersion()) &&
                    artifact.getExtension().equals("pom"))
                return mavenModule;
        }
        return null;
    }

    public List<String> findVersions(Artifact artifact) {
        MavenModule mavenModule = findModule(artifact);
        if (mavenModule != null)
            return Lists.newArrayList(mavenModule.getVersion());
        else
            return Lists.newArrayList();
    }

    public MavenModule getRootModule() {
        return rootModule;
    }

    public List<MavenModule> getMavenModules() {
        return mavenModules;
    }

}
