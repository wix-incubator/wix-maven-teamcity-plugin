package com.wixpress.ci.teamcity.teamCityAnalyzer;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;
import com.wixpress.ci.teamcity.dependenciesTab.ConfigModel;
import com.wixpress.ci.teamcity.domain.*;
import com.wixpress.ci.teamcity.mavenAnalyzer.dao.DependenciesDao;
import com.wixpress.ci.teamcity.teamCityAnalyzer.entity.BuildTypeDependencies;
import jetbrains.buildServer.serverSide.ProjectManager;
import jetbrains.buildServer.serverSide.SBuildType;
import jetbrains.buildServer.serverSide.SFinishedBuild;
import jetbrains.buildServer.vcs.SVcsModification;

import java.util.*;

import static com.google.common.collect.Lists.*;
import static com.google.common.collect.Sets.newHashSet;

/**
 * @author yoav
 * @since 2/26/12
 */
public class BuildPlanAnalyzer {
    
    private ProjectManager projectManager;
    private ConfigModel configModel;
    private DependenciesDao dependenciesDao;

    private SimplePatternMatcher patternMatcher = new SimplePatternMatcher();
    
    public BuildPlanAnalyzer(ProjectManager projectManager, ConfigModel configModel, DependenciesDao dependenciesDao) {
        this.projectManager = projectManager;
        this.configModel = configModel;
        this.dependenciesDao = dependenciesDao;
    }

    public List<MBuildPlanItem> getBuildPlan(BuildTypeDependencies buildTypeDependencies) {
        Graph graph = buildGraph(buildTypeDependencies);
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

    private void markNodesNeedingRebuildingByDependencies(Graph graph) {
        for (BuildTypeNode buildTypeNode: graph.values()) {
            buildTypeNode.analyzeParentDependencies();
        }
    }

    private void markNodesNeedingRebuild(Graph graph) {
        for (BuildTypeNode buildTypeNode: graph.values()) {
            if (buildTypeNode.latestBuildStart == null) 
                buildTypeNode.markNeedsBuild("No successful build found");
            else if (buildTypeNode.hasPendingChanges) 
                buildTypeNode.markNeedsBuild("Has pending changes");
        }
    }

    private void markNodesPendingChangesAndLastBuild(Graph graph) {
        for (BuildTypeNode buildTypeNode: graph.values()) {
            SBuildType buildType = projectManager.findBuildTypeById(buildTypeNode.buildTypeId.getBuildTypeId());
            if (buildType != null) {
                buildTypeNode.hasPendingChanges = checkPendingChanges(buildType);
                buildTypeNode.setLastSuccessfulBuild(buildType.getLastChangesSuccessfullyFinished());
            }
            else
                buildTypeNode.unknownBuildType = true;
        }
    }

    private boolean checkPendingChanges(SBuildType buildType) {
        for (SVcsModification sVcsModification: buildType.getPendingChanges()) {
            if (!isIgnoredChange(sVcsModification.getDescription()))
                return true;
        }
        return false;
    }

    private boolean isIgnoredChange(String description) {
        for (String commitToIgnore : configModel.getConfig().getCommitsToIgnore()) {
            if (patternMatcher.wildcardMatch(description, commitToIgnore))
                return true;
        }
        return false;
    }

    private Graph buildGraph(BuildTypeDependencies buildTypeDependencies) {
        Graph graph = new Graph();
        graph.addBuildTypeDependencies(buildTypeDependencies);

        Set<BuildTypeId> pendingAdditionBuildTypeIds = graph.pendingAdditionBuildTypeIds();
        while (graph.pendingAdditionBuildTypeIds().size() > 0) {

            for (BuildTypeId dependentBuildType: pendingAdditionBuildTypeIds) {
                SBuildType buildType = projectManager.findBuildTypeById(dependentBuildType.getBuildTypeId());
                BuildTypeDependencies dependentDependencies = dependenciesDao.loadBuildDependencies(buildType);
                graph.addBuildTypeDependencies(dependentDependencies);
            }
            pendingAdditionBuildTypeIds = graph.pendingAdditionBuildTypeIds();
        }

        return graph;
    }

    private class Graph extends HashMap<BuildTypeId, BuildTypeNode> {
        
        private final Set<BuildTypeId> addedBuildTypes = newHashSet();
        
        public BuildTypeNode getGraphDependency(BuildTypeId buildTypeId) {
            if (containsKey(buildTypeId))
                return get(buildTypeId);
            else {
                BuildTypeNode node = new BuildTypeNode(buildTypeId);
                put(buildTypeId, node);
                return node;
            }
        }
        
        public void addBuildTypeDependencies(BuildTypeDependencies buildTypeDependencies) {
            BuildTypeNode buildTypeNode = getGraphDependency(buildTypeDependencies.getBuildTypeId());
            for (BuildTypeId childBuildTypeId: buildTypeDependencies.getDependencies()) {
                BuildTypeNode childNode = getGraphDependency(childBuildTypeId);
                buildTypeNode.addChild(childNode);
            }
            addedBuildTypes.add(buildTypeDependencies.getBuildTypeId());
        }
        
        public Set<BuildTypeId> pendingAdditionBuildTypeIds() {
            return new HashSet<BuildTypeId>(Sets.difference(keySet(), addedBuildTypes));
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
        
        public String toString() {
            StringBuilder sb = new StringBuilder()
                    .append(buildTypeId.getProjectId()).append("/").append(buildTypeId.getBuildTypeId()).append(", ");
            if (children.size() > 0) {
                Iterable<String> childIds = Iterables.transform(children, new ExtractBuildTypeIdStringFunction());
                sb.append("children:[");
                joiner.appendTo(sb, childIds);
                sb.append("], ");
            }
            if (parents.size() > 0) {
                Iterable<String> parentIds = Iterables.transform(parents, new ExtractBuildTypeIdStringFunction());
                sb.append("parents:[");
                joiner.appendTo(sb, parentIds);
                sb.append("], ");
            }
            if (needsBuild)
                sb.append("needs build, ");
            if (hasPendingChanges)
                sb.append("has pending changes, ");
            if (unknownBuildType)
                sb.append("unknown build type, ");
            sb.append("description:[");
            joiner.appendTo(sb, description);
            sb.append("]");
            return sb.toString();
                    
        }

        private class ExtractBuildTypeIdStringFunction implements Function<BuildTypeNode, String> {
            public String apply(BuildTypeNode input) {
                return input.buildTypeId.getProjectId() + "/" + input.buildTypeId.getBuildTypeId();
            }
        }
    }
}
