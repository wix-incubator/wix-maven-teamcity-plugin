package com.wixpress.ci.teamcity.maven;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.wixpress.ci.teamcity.maven.workspace.MavenWorkspaceListener;
import com.wixpress.ci.teamcity.maven.workspace.MavenWorkspaceReader;
import com.wixpress.ci.teamcity.maven.workspace.MavenWorkspaceReaderException;
import com.wixpress.ci.teamcity.maven.workspace.WorkspaceFilesystem;
import org.apache.maven.model.building.DefaultModelProcessor;
import org.apache.maven.model.building.ModelProcessor;
import org.apache.maven.model.io.DefaultModelReader;
import org.apache.maven.model.locator.DefaultModelLocator;
import org.apache.maven.properties.internal.EnvironmentUtils;
import org.apache.maven.repository.internal.MavenRepositorySystemSession;
import org.apache.maven.repository.internal.MavenServiceLocator;
import org.apache.maven.settings.Profile;
import org.apache.maven.settings.Repository;
import org.apache.maven.settings.Settings;
import org.apache.maven.settings.building.*;
import org.apache.maven.settings.io.DefaultSettingsReader;
import org.apache.maven.settings.io.DefaultSettingsWriter;
import org.apache.maven.settings.validation.DefaultSettingsValidator;
import org.sonatype.aether.RepositoryListener;
import org.sonatype.aether.RepositorySystem;
import org.sonatype.aether.collection.DependencySelector;
import org.sonatype.aether.connector.file.FileRepositoryConnectorFactory;
import org.sonatype.aether.connector.wagon.WagonProvider;
import org.sonatype.aether.connector.wagon.WagonRepositoryConnectorFactory;
import org.sonatype.aether.repository.*;
import org.sonatype.aether.spi.connector.RepositoryConnectorFactory;
import org.sonatype.aether.transfer.TransferListener;
import org.sonatype.aether.util.graph.selector.AndDependencySelector;
import org.sonatype.aether.util.graph.selector.ExclusionDependencySelector;
import org.sonatype.aether.util.graph.selector.OptionalDependencySelector;
import org.sonatype.aether.util.repository.DefaultProxySelector;

import java.io.File;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import static com.google.common.collect.Lists.newArrayList;

/**
 * @author yoav
 * @since 2/14/12
 */
public class MavenBooter {
    // constants taken from MavenCli class at org.apache.maven:maven-embedder:3.0.4
    public static final String LOCAL_REPO_PROPERTY = "maven.repo.local";
    public static final String userHome = System.getProperty( "user.home" );
    public static final File userMavenConfigurationHome = new File( userHome, ".m2" );
    public static final File DEFAULT_USER_SETTINGS_FILE = new File( userMavenConfigurationHome, "settings.xml" );
    public static final File DEFAULT_GLOBAL_SETTINGS_FILE = new File( System.getProperty( "maven.home", System.getProperty( "user.dir", "" ) ), "conf/settings.xml" );
    public static final File DEFAULT_USER_TOOLCHAINS_FILE = new File( userMavenConfigurationHome, "toolchains.xml" );
    public static final String DEFAULT_LOCAL_REPO = new File(userMavenConfigurationHome, "repository").getAbsolutePath();
    public static final RemoteRepository DEFAULT_REMOTE_REPO = new RemoteRepository( "central", "default", "http://repo1.maven.org/maven2/" );

    private LazySingleton<SettingsBuilder> settingsBuilder = new LazySingleton<SettingsBuilder>();
    private LazySingletonE<Settings, SettingsBuildingException> settings = new LazySingletonE<Settings, SettingsBuildingException>();
    private LazySingleton<Properties> systemProperties = new LazySingleton<Properties>();
    private LazySingleton<RepositorySystem> repositorySystem = new LazySingleton<RepositorySystem>();
    private LazySingleton<ModelProcessor> modelProcessor = new LazySingleton<ModelProcessor>();
    private LazySingleton<List<RemoteRepository>> remoteRepositories = new LazySingleton<List<RemoteRepository>>();

    public Properties systemProperties() {
        return systemProperties.getSingleton(new SingletonConstructor<Properties>() {
            public Properties create() {
                Properties systemProperties = new Properties();
                EnvironmentUtils.addEnvVars(systemProperties);
                return systemProperties;
            }
        });
    }

    public SettingsBuilder settingsBuilder() {
        return settingsBuilder.getSingleton(new SingletonConstructor<SettingsBuilder>() {
            public SettingsBuilder create() throws RuntimeException{
                DefaultSettingsBuilder defaultSettingsBuilder = new DefaultSettingsBuilder();
                defaultSettingsBuilder.setSettingsReader(new DefaultSettingsReader());
                defaultSettingsBuilder.setSettingsWriter(new DefaultSettingsWriter());
                defaultSettingsBuilder.setSettingsValidator(new DefaultSettingsValidator());
                return defaultSettingsBuilder;
            }
        });
    }

    /**
     * reads the Maven settings from the current computer and current user.
     * The settings are read from the current user's local repository settings.xml file and from the maven
     * installation conf/settings.xml file
     * @return maven settings object
     */
    public Settings settings() {
        try {
            return settings.getSingleton(new SingletonConstructorE<Settings, SettingsBuildingException>() {
                public Settings create() throws SettingsBuildingException {
                    File userSettingsFile = DEFAULT_USER_SETTINGS_FILE;
                    File globalSettingsFile = DEFAULT_GLOBAL_SETTINGS_FILE;
    
                    SettingsBuildingRequest settingsRequest = new DefaultSettingsBuildingRequest();
                    settingsRequest.setGlobalSettingsFile(globalSettingsFile);
                    settingsRequest.setUserSettingsFile(userSettingsFile);
                    settingsRequest.setSystemProperties(systemProperties());
    
                    SettingsBuildingResult settingsResult = settingsBuilder().build(settingsRequest);
                    Settings settings = settingsResult.getEffectiveSettings();
                    if (settings.getLocalRepository() == null)
                        settings.setLocalRepository(DEFAULT_LOCAL_REPO);
                    return settingsResult.getEffectiveSettings();
                }
            });
        } catch (SettingsBuildingException e) {
            throw new MavenBooterException("failed loading maven settings", e);
        }
    }

    /**
     * Auther repository system, built based on the example from http://www.eclipse.org/aether/
     * @return repository system
     */
    public RepositorySystem repositorySystem() {
        return repositorySystem.getSingleton(new SingletonConstructor<RepositorySystem>(){
            public RepositorySystem create() throws RuntimeException {
                MavenServiceLocator locator = new MavenServiceLocator();
                locator.addService( RepositoryConnectorFactory.class, FileRepositoryConnectorFactory.class );
                locator.addService( RepositoryConnectorFactory.class, WagonRepositoryConnectorFactory.class );
                locator.setServices( WagonProvider.class, new ManualWagonProvider() );

                return locator.getService( RepositorySystem.class );
            }
        });
    }

    /**
     * Auther session wired with the repository system and the local repository from settings
     * based on the example from http://www.eclipse.org/aether/
     * @param transferListener - listener to artifact transfer operations 
     * @param repositoryListener - listener to repository operations
     * @return auther session
     */
    public MavenRepositorySystemSession newRepositorySystemSession(TransferListener transferListener, RepositoryListener repositoryListener) {
        MavenRepositorySystemSession session = new MavenRepositorySystemSession();

        LocalRepository localRepo = new LocalRepository(settings().getLocalRepository());
        session.setLocalRepositoryManager( repositorySystem().newLocalRepositoryManager(localRepo) );
        DependencySelector depFilter =
            new AndDependencySelector(new ScopeIncludeFirstDependencySelector("test", "provided"), new OptionalDependencySelector(), new ExclusionDependencySelector() );
        session.setDependencySelector(depFilter);

        session.setTransferListener( transferListener );
        session.setRepositoryListener( repositoryListener );

//        uncomment to generate dirty trees
//        session.setDependencyGraphTransformer( null );

        if (settings().getActiveProxy() != null) {
            setupSessionProxy(session, settings().getActiveProxy());
        }
        
        return session;
    }
    
    public MavenWorkspaceReader newWorkspaceReader(WorkspaceFilesystem workspaceFilesystem, MavenWorkspaceListener workspaceListener) throws MavenWorkspaceReaderException {
        return new MavenWorkspaceReader(workspaceFilesystem, modelProcessor(), workspaceListener);
    }

    /**
     * reads the remote repositories as defined in the maven settings for the active profile and translates them into aether remote repositories
     * @return aether active remote repositories
     */
    public List<RemoteRepository> remoteRepositories() {
        return remoteRepositories.getSingleton(new SingletonConstructor<List<RemoteRepository>>(){
            public List<RemoteRepository> create()  {
                List<RemoteRepository> remoteRepositories = newArrayList();
                Set<String> activeProfiles = new HashSet<String> (settings().getActiveProfiles());
                for (Profile profile: settings().getProfiles()) {
                    if (activeProfiles.contains(profile.getId())) {
                        remoteRepositories.addAll(Lists.transform(profile.getRepositories(), new Function<Repository, RemoteRepository>() {
                            public RemoteRepository apply(Repository repository) {
                                RemoteRepository remoteRepository = new RemoteRepository(repository.getId(), "default", repository.getUrl());
                                if (repository.getSnapshots() != null)
                                    remoteRepository.setPolicy(true, new RepositoryPolicy(
                                            repository.getSnapshots().isEnabled(),
                                            repository.getSnapshots().getUpdatePolicy(),
                                            repository.getSnapshots().getChecksumPolicy()));
                                if (repository.getReleases() != null)
                                    remoteRepository.setPolicy(false, new RepositoryPolicy(
                                            repository.getReleases().isEnabled(),
                                            repository.getReleases().getUpdatePolicy(),
                                            repository.getReleases().getChecksumPolicy()));
                                return remoteRepository;
                            }
                        }));
                    }
                }
                if (remoteRepositories.size() == 0)
                    remoteRepositories.add(DEFAULT_REMOTE_REPO);
                return remoteRepositories;
            }
        });
    }
    
    private void setupSessionProxy(MavenRepositorySystemSession session, org.apache.maven.settings.Proxy proxySettings) {
        ((DefaultProxySelector)session.getProxySelector()).add(
                new Proxy(
                        proxySettings.getProtocol(),
                        proxySettings.getHost(),
                        proxySettings.getPort(),
                        new Authentication(
                                proxySettings.getUsername(),
                                proxySettings.getPassword())), 
                proxySettings.getNonProxyHosts());
    }


    public ModelProcessor modelProcessor() {
        return modelProcessor.getSingleton(new SingletonConstructor<ModelProcessor>() {
            public ModelProcessor create() throws RuntimeException {
                DefaultModelProcessor processor = new DefaultModelProcessor();
                processor.setModelLocator( new DefaultModelLocator() );
                processor.setModelReader( new DefaultModelReader() );
                return processor;
            }
        });
    }

    private class LazySingletonE<Singleton, E extends Exception> {
        Singleton singleton = null;

        public Singleton getSingleton(SingletonConstructorE<Singleton, E> singletonConstructor) throws E{
            if (singleton == null) {
                synchronized (this) {
                    if (singleton == null)
                        singleton = singletonConstructor.create();
                }
            }
            return singleton;
        }
    }

    private interface SingletonConstructorE<Singleton, E extends Exception> {
        Singleton create() throws E;
    }

    private class LazySingleton<Singleton> extends LazySingletonE<Singleton, RuntimeException>{}

    private interface SingletonConstructor<Singleton> extends SingletonConstructorE<Singleton, RuntimeException> {}
}
