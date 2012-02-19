/*******************************************************************************
 * Copyright (c) 2010, 2011 Sonatype, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.sonatype.org/legal/epl-v10.html
 *
 * Contributors:
 *    Sonatype, Inc. - initial API and implementation
 *******************************************************************************/
package com.wixpress.ci.teamcity.maven.listeners;

import com.wixpress.ci.teamcity.domain.MArtifact;
import com.wixpress.ci.teamcity.domain.MArtifactVisitor;

/**
 * A dependency visitor that dumps the graph to the console.
 */
public class LoggingModuleVisitor implements MArtifactVisitor {

    private ListenerLogger out;

    private String currentIndent = "";

    public LoggingModuleVisitor(ListenerLogger out)
    {
        this.out = out;
    }

    public boolean visitEnter(MArtifact mArtifact) {
        out.info( currentIndent + mArtifact );
        if ( currentIndent.length() <= 0 )
        {
            currentIndent = "+- ";
        }
        else
        {
            currentIndent = "|  " + currentIndent;
        }
        return true;
    }

    public boolean visitLeave(MArtifact mArtifact) {
        currentIndent = currentIndent.substring( 3, currentIndent.length() );
        return true;
    }
}
