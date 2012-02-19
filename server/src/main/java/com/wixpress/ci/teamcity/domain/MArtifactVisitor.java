package com.wixpress.ci.teamcity.domain;

/**
 * @author yoav
 * @since 2/19/12
 */
public interface MArtifactVisitor {
    boolean visitEnter(MArtifact mArtifact);

    boolean visitLeave(MArtifact mArtifact);
}
