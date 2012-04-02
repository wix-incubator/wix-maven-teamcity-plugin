package com.wixpress.ci.teamcity.mavenAnalyzer.dao;

/**
 * @author yoav
 * @since 4/2/12
 */
public class BuildTypeCustomStorageDependenciesDaoException extends RuntimeException {
    public BuildTypeCustomStorageDependenciesDaoException(String message, Throwable e, Object... args) {
        super(String.format(message, args), e);
    }
}
