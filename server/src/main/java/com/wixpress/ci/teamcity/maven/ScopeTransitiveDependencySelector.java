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
public class ScopeTransitiveDependencySelector implements DependencySelector {

    private final Transitivity transitivity;
    private final boolean parentSelected;
    private final Collection<String> included;
    private final Collection<String> excluded;

    /**
     * Creates a new selector using the specified includes and excludes.
     *
     * @param included The set of scopes to include, may be {@code null} or empty to include any scope.
     * @param excluded The set of scopes to exclude, may be {@code null} or empty to exclude no scope.
     */
    public ScopeTransitiveDependencySelector(Collection<String> included, Collection<String> excluded) {
        this(included, excluded, Transitivity.root, false);
    }

    /**
     * Creates a new selector using the specified excludes.
     *
     * @param excluded The set of scopes to exclude, may be {@code null} or empty to exclude no scope.
     */
    public ScopeTransitiveDependencySelector(String... excluded) {
        this(null, Arrays.asList(excluded));
    }

    private ScopeTransitiveDependencySelector(Collection<String> included, Collection<String> excluded, Transitivity transitivity, boolean parentSelected) {
        this.parentSelected = parentSelected;
        this.transitivity = transitivity;
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
        switch (transitivity) {
            case root: return true;
            case direct: return true;
            default: {
                String scope = dependency.getScope();
                return ((included.isEmpty() || included.contains(scope))
                        && (excluded.isEmpty() || !excluded.contains(scope))) && parentSelected;
            }
        }
    }

    public DependencySelector deriveChildSelector(DependencyCollectionContext context) {
        if (context.getDependency() == null) {
            return this;
        }

        String scope = context.getDependency().getScope();
        boolean select = ((included.isEmpty() || included.contains(scope))
                && (excluded.isEmpty() || !excluded.contains(scope)));
        return new ScopeTransitiveDependencySelector(included, excluded, transitivity.next(), select);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ScopeTransitiveDependencySelector that = (ScopeTransitiveDependencySelector) o;

        if (parentSelected != that.parentSelected) return false;
        if (excluded != null ? !excluded.equals(that.excluded) : that.excluded != null) return false;
        if (included != null ? !included.equals(that.included) : that.included != null) return false;
        if (transitivity != that.transitivity) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = transitivity != null ? transitivity.hashCode() : 0;
        result = 31 * result + (parentSelected ? 1 : 0);
        result = 31 * result + (included != null ? included.hashCode() : 0);
        result = 31 * result + (excluded != null ? excluded.hashCode() : 0);
        return result;
    }

    private enum Transitivity {
        root(){
            @Override
            public Transitivity next() {
                return direct;
            }
        },
        direct(){
            @Override
            public Transitivity next() {
                return transitive;
            }
        },
        transitive(){
            @Override
            public Transitivity next() {
                return transitive;
            }
        };

        public abstract Transitivity next();

    }

}

