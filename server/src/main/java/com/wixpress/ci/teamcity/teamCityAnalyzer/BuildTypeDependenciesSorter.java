package com.wixpress.ci.teamcity.teamCityAnalyzer;

import com.wixpress.ci.teamcity.domain.*;

import java.util.*;

import static com.google.common.collect.Lists.newLinkedList;
import static com.google.common.collect.Maps.newHashMap;
import static com.google.common.collect.Sets.newHashSet;

/**
 * @author yoav
 * @since 2/26/12
 */
public class BuildTypeDependenciesSorter {

    public List<BuildTypeId> sortBuildTypes(MModule module, BuildTypeId root) {
        BuildTypeDependencyExtractor dependencyExtractor = new BuildTypeDependencyExtractor(root);
        module.accept(dependencyExtractor);
        TopologicalSorter topologicalSorter = new TopologicalSorter(dependencyExtractor.graph);
        return topologicalSorter.sort();
    }
    
    private class BuildTypeDependencyExtractor implements MArtifactVisitor {

        Map<BuildTypeId, Set<BuildTypeId>> graph = newHashMap();
        Deque<BuildTypeId> stack = newLinkedList();

        public BuildTypeDependencyExtractor(BuildTypeId root) {
            stack.push(root);
        }


        public boolean visitEnter(MArtifact mArtifact) {
            if (mArtifact instanceof MBuildTypeDependency) {
                MBuildTypeDependency buildTypeDependency = (MBuildTypeDependency)mArtifact;
                BuildTypeId buildTypeId = buildTypeDependency.getBuildTypeId();
                getGraphDependecy(buildTypeId);
                if (!stack.peek().equals(buildTypeId))
                    getGraphDependecy(stack.peek()).add(buildTypeId);
                stack.push(buildTypeId);
            }
            return true;
        }

        public boolean visitLeave(MArtifact mArtifact) {
            if (mArtifact instanceof MBuildTypeDependency) {
                stack.pop();
            }
            return true;
        }

        private Set<BuildTypeId> getGraphDependecy(BuildTypeId buildTypeId) {
            if (graph.containsKey(buildTypeId))
                return graph.get(buildTypeId);
            else {
                Set<BuildTypeId> dependencies = newHashSet();
                graph.put(buildTypeId, dependencies);
                return dependencies;
            }
        }
    }
    
    
}
