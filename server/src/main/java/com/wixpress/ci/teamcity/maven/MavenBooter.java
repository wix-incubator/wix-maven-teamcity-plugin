package com.wixpress.ci.teamcity.maven;

import org.apache.maven.model.building.DefaultModelProcessor;
import org.apache.maven.model.building.ModelProcessor;
import org.apache.maven.model.io.DefaultModelReader;
import org.apache.maven.model.io.ModelReader;
import org.apache.maven.model.locator.DefaultModelLocator;
import org.apache.maven.model.locator.ModelLocator;
import org.apache.maven.properties.internal.EnvironmentUtils;
import org.apache.maven.repository.internal.MavenServiceLocator;
import org.apache.maven.settings.Settings;
import org.apache.maven.settings.building.*;
import org.apache.maven.settings.io.DefaultSettingsReader;
import org.apache.maven.settings.io.DefaultSettingsWriter;
import org.apache.maven.settings.validation.DefaultSettingsValidator;
import org.sonatype.aether.RepositorySystem;
import org.sonatype.aether.connector.file.FileRepositoryConnectorFactory;
import org.sonatype.aether.connector.wagon.WagonProvider;
import org.sonatype.aether.connector.wagon.WagonRepositoryConnectorFactory;
import org.sonatype.aether.spi.connector.RepositoryConnectorFactory;

import java.io.File;
import java.util.Properties;

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

    private LazySingleton<SettingsBuilder> settingsBuilder = new LazySingleton<SettingsBuilder>();
    private LazySingletonE<Settings, SettingsBuildingException> settings = new LazySingletonE<Settings, SettingsBuildingException>();
    private LazySingleton<Properties> systemProperties = new LazySingleton<Properties>();
    private LazySingleton<RepositorySystem> repositorySystem = new LazySingleton<RepositorySystem>();
    private LazySingleton<ModelProcessor> modelProcessor = new LazySingleton<ModelProcessor>();

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
     * @throws SettingsBuildingException - in case of error building the settings
     */
    public Settings settings() throws SettingsBuildingException {
        return settings.getSingleton(new SingletonConstructorE<Settings, SettingsBuildingException>() {
            public Settings create() throws SettingsBuildingException {
                File userSettingsFile = DEFAULT_USER_SETTINGS_FILE;
                File globalSettingsFile = DEFAULT_GLOBAL_SETTINGS_FILE;

                SettingsBuildingRequest settingsRequest = new DefaultSettingsBuildingRequest();
                settingsRequest.setGlobalSettingsFile(globalSettingsFile);
                settingsRequest.setUserSettingsFile(userSettingsFile);
                settingsRequest.setSystemProperties(systemProperties());

                SettingsBuildingResult settingsResult = settingsBuilder().build(settingsRequest);
                return settingsResult.getEffectiveSettings();
            }
        });
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
                singleton = singletonConstructor.create();
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
