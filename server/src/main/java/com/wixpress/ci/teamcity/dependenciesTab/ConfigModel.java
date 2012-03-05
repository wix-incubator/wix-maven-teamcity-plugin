package com.wixpress.ci.teamcity.dependenciesTab;

import com.wixpress.ci.teamcity.domain.DependenciesTabConfig;
import jetbrains.buildServer.configuration.ChangeListener;
import jetbrains.buildServer.configuration.FileWatcher;
import jetbrains.buildServer.log.Loggers;
import jetbrains.buildServer.serverSide.ServerPaths;
import org.codehaus.jackson.map.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * @author yoav
 * @since 3/5/12
 */
public class ConfigModel {

    public static final String CONFIG_FILE_NAME = "dependencies-config.json";
    
    private ServerPaths serverPaths;
    private ReadWriteLock readWriteLock = new ReentrantReadWriteLock();
    private DependenciesTabConfig config;
    private ObjectMapper objectMapper;
    private final File configFile;
    
    public ConfigModel(ServerPaths serverPaths, ObjectMapper objectMapper) {
        this.serverPaths = serverPaths;
        this.objectMapper = objectMapper;
        configFile = new File(serverPaths.getConfigDir(), CONFIG_FILE_NAME);
        load();

        FileWatcher configFileWatcher = new FileWatcher(configFile);
        configFileWatcher.start();
        configFileWatcher.registerListener(new ChangeListener() {
            public void changeOccured(String s) {
                load();
            }
        });
    }

    public DependenciesTabConfig getConfig() {
        readWriteLock.readLock().lock();
        try {
            return config;
        }
        finally {
            readWriteLock.readLock().unlock();
        }
    }

    public void updateConfig(DependenciesTabConfig config) {
        readWriteLock.writeLock().lock();
        try {
            this.config = config;
            save();
        }
        finally {
            readWriteLock.writeLock().unlock();
        }
    }

    private void load() {
        readWriteLock.writeLock().lock();
        try {
            if (configFile.isFile()) {
                Loggers.SERVER.info(String.format("Loading Dependencies Tab configuration from [%s]", configFile));
                try {
                    config = objectMapper.readValue(configFile, DependenciesTabConfig.class);
                } catch (Exception e) {
                    Loggers.SERVER.error(String.format("Failed loading Dependencies Tab configuration from [%s]. Assuming initial (empty) config", configFile), e);
                    config = new DependenciesTabConfig();
                }
            }
            else {
                Loggers.SERVER.warn(String.format("Dependencies Tab configuration  not found [%s]. Assuming initial (empty) config", configFile));
                config = new DependenciesTabConfig();
            }
        }
        finally {
            readWriteLock.writeLock().unlock();
        }
    }
    
    private void save() {
        readWriteLock.writeLock().lock();
        try {
            try {
                objectMapper.writeValue(configFile, config);
            } catch (IOException e) {
                Loggers.SERVER.error(String.format("Failed saving Dependencies Tab configuration to [%s]", configFile), e);
            }
        }
        finally {
            readWriteLock.writeLock().unlock();
        }
    }
    

}
