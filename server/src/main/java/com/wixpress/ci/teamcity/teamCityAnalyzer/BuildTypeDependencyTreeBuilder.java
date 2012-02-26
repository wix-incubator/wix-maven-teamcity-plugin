package com.wixpress.ci.teamcity.teamCityAnalyzer;

import com.wixpress.ci.teamcity.domain.MBuildType;
import com.wixpress.ci.teamcity.domain.MBuildTypeDependency;
import com.wixpress.ci.teamcity.domain.MDependency;
import com.wixpress.ci.teamcity.domain.MModule;
import jetbrains.buildServer.serverSide.SBuildType;
import org.jetbrains.annotations.TestOnly;

import java.util.ArrayList;
import java.util.List;

import static com.google.common.collect.Lists.newArrayList;

/**
 * @author yoav
 * @since 2/26/12
 */
public class BuildTypeDependencyTreeBuilder {

    public MBuildType buildTree(MModule module, SBuildType buildType) {
        MBuildType root = new MBuildType(buildType);
        List<MBuildType> moduleTrees = extractAllModuleBuildTypeDependenciesTree(module, buildType);
        return mergeTrees(moduleTrees);
    }

    private MBuildType mergeTrees(List<MBuildType> moduleTrees) {
        return null;  //To change body of created methods use File | Settings | File Templates.
    }

    private List<MBuildType> extractAllModuleBuildTypeDependenciesTree(MModule module, SBuildType buildType) {
        List<MBuildType> moduleTrees = newArrayList();
        moduleTrees.add(extractModuleBuildTypeDependenciesTree(module, buildType));
        for (MModule subModule: module.getSubModules())
            moduleTrees.addAll(extractAllModuleBuildTypeDependenciesTree(subModule, buildType));
        return moduleTrees;
    }

    @TestOnly
    MBuildType extractModuleBuildTypeDependenciesTree(MModule module, SBuildType buildType) {
        MBuildType moduleBuildType = new MBuildType(buildType);
        for (MDependency dependency: module.getDependencyTree().getDependencies()) {
            MBuildType childBuildType = extractDependencyBuildType(dependency);
            if (childBuildType != null)
                moduleBuildType.getDependencies().add(childBuildType);
        }
        return moduleBuildType;
    }
    
    private MBuildType extractDependencyBuildType(MDependency dependency) {
        
        ArrayList<MBuildType> childBuildTypes = newArrayList();
        for (MDependency childDependency: dependency.getDependencies()) {
            MBuildType childBuildType = extractDependencyBuildType(childDependency);
            if (childBuildType != null)
                childBuildTypes.add(childBuildType);
        }
        
        if (dependency instanceof MBuildTypeDependency) {
            MBuildTypeDependency buildTypeDependency = (MBuildTypeDependency)dependency;
            MBuildType mBuildType = new MBuildType(buildTypeDependency.getBuildTypeId());
            mBuildType.setDependencies(childBuildTypes);
            return mBuildType;
        }
        else if (childBuildTypes.size() > 0) {
            MBuildType mBuildType = new MBuildType(String.format("unknown(%s:%s)", dependency.getGroupId(), dependency.getArtifactId()), "unknown", "", "");
            mBuildType.setDependencies(childBuildTypes);
            return mBuildType;
        }
        else
            return null;
    }
}
