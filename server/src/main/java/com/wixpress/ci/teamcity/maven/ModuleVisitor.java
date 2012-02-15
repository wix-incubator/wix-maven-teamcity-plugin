package com.wixpress.ci.teamcity.maven;

import org.sonatype.aether.graph.DependencyVisitor;

/**
 * @author yoav
 * @since 2/15/12
 */
public interface ModuleVisitor {
    /**
     * Notifies the visitor of a node visit before its children have been processed.
     *
     * @param node The Module node being visited, must not be {@code null}.
     * @return {@code true} to visit child nodes of the specified node as well, {@code false} to skip children.
     */
    boolean visitEnter( ModuleDependencies node );

    /**
     * Notifies the visitor of a node visit after its children have been processed. Note that this method is always
     * invoked regardless whether any children have actually been visited.
     *
     * @param node The Module node being visited, must not be {@code null}.
     * @return {@code true} to visit siblings nodes of the specified node as well, {@code false} to skip siblings.
     */
    boolean visitLeave( ModuleDependencies node );

    DependencyVisitor getDependencyVisitor();
}
