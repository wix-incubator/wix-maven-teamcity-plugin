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

import org.sonatype.aether.AbstractRepositoryListener;
import org.sonatype.aether.RepositoryEvent;

/**
 * A simplistic repository listener that logs events to the console.
 */
public class LoggingRepositoryListener
    extends AbstractRepositoryListener
{

    private ListenerLogger out;

    public LoggingRepositoryListener(ListenerLogger out)
    {
        this.out = out;
    }

    public void artifactDeployed( RepositoryEvent event )
    {
        out.info("Deployed " + event.getArtifact() + " to " + event.getRepository());
    }

    public void artifactDeploying( RepositoryEvent event )
    {
        out.info("Deploying " + event.getArtifact() + " to " + event.getRepository());
    }

    public void artifactDescriptorInvalid( RepositoryEvent event )
    {
        out.error( "Invalid artifact descriptor for " + event.getArtifact(), event.getException());
    }

    public void artifactDescriptorMissing( RepositoryEvent event )
    {
        out.info("Missing artifact descriptor for " + event.getArtifact());
    }

    public void artifactInstalled( RepositoryEvent event )
    {
        out.info("Installed " + event.getArtifact() + " to " + event.getFile());
    }

    public void artifactInstalling( RepositoryEvent event )
    {
        out.info("Installing " + event.getArtifact() + " to " + event.getFile());
    }

    public void artifactResolved( RepositoryEvent event )
    {
//        out.info("Resolved artifact " + event.getArtifact() + " from " + event.getRepository());
    }

    public void artifactDownloading( RepositoryEvent event )
    {
        out.info("Downloading artifact " + event.getArtifact() + " from " + event.getRepository());
    }

    public void artifactDownloaded( RepositoryEvent event )
    {
        out.info("Downloaded artifact " + event.getArtifact() + " from " + event.getRepository());
    }

    public void artifactResolving( RepositoryEvent event )
    {
//        out.info("Resolving artifact " + event.getArtifact());
    }

    public void metadataDeployed( RepositoryEvent event )
    {
        out.info("Deployed " + event.getMetadata() + " to " + event.getRepository());
    }

    public void metadataDeploying( RepositoryEvent event )
    {
        out.info("Deploying " + event.getMetadata() + " to " + event.getRepository());
    }

    public void metadataInstalled( RepositoryEvent event )
    {
        out.info("Installed " + event.getMetadata() + " to " + event.getFile());
    }

    public void metadataInstalling( RepositoryEvent event )
    {
        out.info("Installing " + event.getMetadata() + " to " + event.getFile());
    }

    public void metadataInvalid( RepositoryEvent event )
    {
        out.info("Invalid metadata " + event.getMetadata());
    }

    public void metadataResolved( RepositoryEvent event )
    {
//        out.info("Resolved metadata " + event.getMetadata() + " from " + event.getRepository());
    }

    public void metadataResolving( RepositoryEvent event )
    {
//        out.info("Resolving metadata " + event.getMetadata() + " from " + event.getRepository());
    }

}
