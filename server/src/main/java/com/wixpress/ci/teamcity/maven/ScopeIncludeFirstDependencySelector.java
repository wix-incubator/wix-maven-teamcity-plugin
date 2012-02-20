package com.wixpress.ci.teamcity.maven;

import org.sonatype.aether.collection.DependencyCollectionContext;
import org.sonatype.aether.collection.DependencySelector;
import org.sonatype.aether.graph.Dependency;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;

/**
 * Dependency Selector who includes the first artifacts by 
 * @author yoav
 * @since 2/20/12
 */
public class ScopeIncludeFirstDependencySelector implements DependencySelector {

    private final boolean transitive;
    private final boolean parentDependencyAtExcludedScope;
    private final Collection<String> included;
    private final Collection<String> excluded;

    /**
     * Creates a new selector using the specified includes and excludes.
     *
     * @param included The set of scopes to include, may be {@code null} or empty to include any scope.
     * @param excluded The set of scopes to exclude, may be {@code null} or empty to exclude no scope.
     */
    public ScopeIncludeFirstDependencySelector(Collection<String> included, Collection<String> excluded) {
        this(included, excluded, false, false);
    }

    /**
     * Creates a new selector using the specified excludes.
     *
     * @param excluded The set of scopes to exclude, may be {@code null} or empty to exclude no scope.
     */
    public ScopeIncludeFirstDependencySelector(String... excluded) {
        this(null, Arrays.asList(excluded));
    }

    private ScopeIncludeFirstDependencySelector(Collection<String> included, Collection<String> excluded, boolean transitive, boolean parentDependencyAtExcludedScope) {
        this.parentDependencyAtExcludedScope = parentDependencyAtExcludedScope;
        this.transitive = transitive;
        if (included != null) {
            this.included = new HashSet<String>();
            this.included.addAll(included);
        } else {
            this.included = Collections.emptySet();
        }
        if (excluded != null) {
            this.excluded = new HashSet<String>();
            this.excluded.addAll(excluded);
        } else {
            this.excluded = Collections.emptySet();
        }
    }

    public boolean selectDependency(Dependency dependency) {
        if (!parentDependencyAtExcludedScope) {
            return true;
        }

        String scope = dependency.getScope();
        return ((included.isEmpty() || included.contains(scope))
                && (excluded.isEmpty() || !excluded.contains(scope))) && !parentDependencyAtExcludedScope;
    }

    public DependencySelector deriveChildSelector(DependencyCollectionContext context) {
        if (context.getDependency() == null) {
            return this;
        }

        String scope = context.getDependency().getScope();
        boolean select = ((included.isEmpty() || included.contains(scope))
                && (excluded.isEmpty() || !excluded.contains(scope)));
        return new ScopeIncludeFirstDependencySelector(included, excluded, true, !select);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        } else if (null == obj || !getClass().equals(obj.getClass())) {
            return false;
        }

        ScopeIncludeFirstDependencySelector that = (ScopeIncludeFirstDependencySelector) obj;
        return transitive == that.transitive && parentDependencyAtExcludedScope == that.parentDependencyAtExcludedScope &&
                included.equals(that.included) && excluded.equals(that.excluded);
    }

    @Override
    public int hashCode() {
        int hash = 17;
        hash = hash * 31 + ( transitive ? 1 : 0 );
        hash = hash * 31 + ( parentDependencyAtExcludedScope ? 1 : 0 );
        hash = hash * 31 + included.hashCode();
        hash = hash * 31 + excluded.hashCode();
        return hash;
    }

}

