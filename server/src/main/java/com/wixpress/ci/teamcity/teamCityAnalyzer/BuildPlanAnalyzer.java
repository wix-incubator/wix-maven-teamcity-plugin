package com.wixpress.ci.teamcity.teamCityAnalyzer;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.wixpress.ci.teamcity.domain.*;
import jetbrains.buildServer.serverSide.ProjectManager;
import jetbrains.buildServer.serverSide.SBuildType;
import jetbrains.buildServer.serverSide.SFinishedBuild;

import java.util.*;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Lists.newLinkedList;
import static com.google.common.collect.Lists.transform;
import static com.google.common.collect.Maps.newHashMap;
import static com.google.common.collect.Sets.newHashSet;

/**
 * @author yoav
 * @since 2/26/12
 */
public class BuildPlanAnalyzer {
    
    private ProjectManager projectManager;
    
    public BuildPlanAnalyzer(ProjectManager projectManager) {
        this.projectManager = projectManager;
    }

    public List<MBuildPlanItem> sortBuildTypes(MModule module, BuildTypeId root) {
        Map<BuildTypeId, BuildTypeNode> graph = buildGraph(module, root);
        markNodesPendingChangesAndLastBuild(graph);
        markNodesNeedingRebuild(graph);
        markNodesNeedingRebuildingByDependencies(graph);        
        TopologicalSorter<BuildTypeNode> topologicalSorter = new TopologicalSorter<BuildTypeNode>(graph.values());
        List<BuildTypeNode> sortedNodes = topologicalSorter.sort();
        return transform(sortedNodes, new Function<BuildTypeNode, MBuildPlanItem>() {
            public MBuildPlanItem apply(BuildTypeNode input) {
                MBuildPlanItem mBuildPlanItem = new MBuildPlanItem(input.buildTypeId);
                if (input.needsBuild)
                    mBuildPlanItem.needsBuild(input.getDescriptionMessage());
                return mBuildPlanItem;
            }
        });
    }

    private void markNodesNeedingRebuildingByDependencies(Map<BuildTypeId, BuildTypeNode> graph) {
        for (BuildTypeNode buildTypeNode: graph.values()) {
            buildTypeNode.analyzeParentDependencies();
        }
    }

    private void markNodesNeedingRebuild(Map<BuildTypeId, BuildTypeNode> graph) {
        for (BuildTypeNode buildTypeNode: graph.values()) {
            if (buildTypeNode.latestBuildStart == null) 
                buildTypeNode.markNeedsBuild("No successful build found");
            else if (buildTypeNode.hasPendingChanges) 
                buildTypeNode.markNeedsBuild("Has pending changes");
        }
    }

    private void markNodesPendingChangesAndLastBuild(Map<BuildTypeId, BuildTypeNode> graph) {
        for (BuildTypeNode buildTypeNode: graph.values()) {
            SBuildType buildType = projectManager.findBuildTypeById(buildTypeNode.buildTypeId.getBuildTypeId());
            if (buildType != null) {
                buildTypeNode.hasPendingChanges = buildType.getPendingChanges().size() > 0;
                buildTypeNode.setLastSuccessfulBuild(buildType.getLastChangesSuccessfullyFinished());
            }
            else
                buildTypeNode.unknownBuildType = true;
        }
    }

    private Map<BuildTypeId, BuildTypeNode> buildGraph(MModule module, BuildTypeId root) {
        BuildTypeDependencyExtractor dependencyExtractor = new BuildTypeDependencyExtractor(root);
        module.accept(dependencyExtractor);
        return dependencyExtractor.graph;
    }

    private class BuildTypeDependencyExtractor implements MArtifactVisitor {

        Map<BuildTypeId, BuildTypeNode> graph = newHashMap();
        Deque<BuildTypeId> stack = newLinkedList();

        public BuildTypeDependencyExtractor(BuildTypeId root) {
            stack.push(root);
        }


        public boolean visitEnter(MArtifact mArtifact) {
            if (mArtifact instanceof MBuildTypeDependency) {
                MBuildTypeDependency buildTypeDependency = (MBuildTypeDependency)mArtifact;
                BuildTypeId buildTypeId = buildTypeDependency.getBuildTypeId();
                if (!("test".equals(buildTypeDependency.getScope()) || "provided".equals(buildTypeDependency.getScope()))) {
                    BuildTypeNode newNode = getGraphDependecy(buildTypeId);
                    if (!stack.peek().equals(buildTypeId))
                        getGraphDependecy(stack.peek()).addChild(newNode);
                }
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

        private void addToGraph(BuildTypeId buildTypeId) {
            getGraphDependecy(buildTypeId);
        }

        private BuildTypeNode getGraphDependecy(BuildTypeId buildTypeId) {
            if (graph.containsKey(buildTypeId))
                return graph.get(buildTypeId);
            else {
                BuildTypeNode node = new BuildTypeNode(buildTypeId);
                graph.put(buildTypeId, node);
                return node;
            }
        }
    }

    Joiner joiner = Joiner.on(", ");

    private class BuildTypeNode implements TopologicalSorter.Node<BuildTypeNode> {
        BuildTypeId buildTypeId;
        Set<BuildTypeNode> children = newHashSet();
        Set<BuildTypeNode> parents = newHashSet();
        boolean needsBuild = false;
        List<String> description = newArrayList();
        Date latestBuildStart;
        Date latestBuildFinished;
        boolean hasPendingChanges;
        boolean unknownBuildType;

        public BuildTypeNode(BuildTypeId buildTypeId) {
            this.buildTypeId = buildTypeId;
        }

        public void addChild(BuildTypeNode child) {
            children.add(child);
            child.parents.add(this);
        }

        public Iterable<BuildTypeNode> getChildren() {
            return children;
        }

        public void markNeedsBuild(String description) {
            this.needsBuild = true;
            this.description.add(description);
        }

        public void setLastSuccessfulBuild(SFinishedBuild lastSuccessfulBuild) {
            if (lastSuccessfulBuild != null) {
                latestBuildStart = lastSuccessfulBuild.getClientStartDate();
                latestBuildFinished = lastSuccessfulBuild.getFinishDate();
            }
        }

        public void analyzeParentDependencies() {
            for (BuildTypeNode parent: parents) {
                if (parent.latestBuildStart != null && this.latestBuildFinished != null && 
                        (this.latestBuildFinished.compareTo(parent.latestBuildStart) > 0))
                    parent.markNeedsBuild(String.format("[%s:%s] last build is newer", 
                            this.buildTypeId.getProjectName(), this.buildTypeId.getName()));
                if (needsBuild)
                    parent.markNeedsBuild(String.format("[%s:%s] require building", 
                            this.buildTypeId.getProjectName(), this.buildTypeId.getName()));
            }
        }

        public String getDescriptionMessage() {
            return joiner.join(description);
        }
    }
}
