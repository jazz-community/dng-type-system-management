/*******************************************************************************
 * Copyright (c) 2012 - 2013, 2018 IBM Corporation.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Eclipse Distribution License v. 1.0 which accompanies this distribution.
 *
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html
 * and the Eclipse Distribution License is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * Contributors:
 *
 *    Ralph Schoon - Initial implementation
 *******************************************************************************/
package com.ibm.requirement.typemanagement.oslc.client.dngcm;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import org.eclipse.lyo.client.exception.ResourceNotFoundException;
import org.eclipse.lyo.client.oslc.jazz.JazzFormAuthClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.oauth.OAuthException;

/**
 * Represents a Project Area OSLC Service Provider
 *
 */
public class ProjectAreaOslcServiceProvider {

	public static final Logger logger = LoggerFactory.getLogger(ProjectAreaOslcServiceProvider.class);

	private String serviceProvider = null;
	private String projectAreaId = null;
	private String projectAreaName = null;

	public ProjectAreaOslcServiceProvider(final String serviceProviderUri, final String projectAreaName) {
		super();
		this.serviceProvider = serviceProviderUri;
		this.projectAreaName = projectAreaName;
		this.projectAreaId = calculateProjectAreaID();
	}

	public String getServiceProviderUrl() {
		return serviceProvider;
	}

	public URI getServiceProviderURI() throws URISyntaxException {
		if (this.serviceProvider == null) {
			return null;
		}
		return new URI(this.serviceProvider);
	}

	public void setProjectAreaId(String id) {
		projectAreaId = id;
	}

	public String getProjectAreaId() {
		return projectAreaId;
	}

	public String getProjectAreaName() {
		return projectAreaName;
	}

	/**
	 * Get the project area ID from a service provider URL.
	 * 
	 * @param serviceProviderUrl
	 * @return the project
	 */
	private String calculateProjectAreaID() {
		String serviceProviderUrl = getServiceProviderUrl();
		String[] pieces = serviceProviderUrl.split("/");
		int len = pieces.length;
		if (len < 2) {
			return null;
		}
		if ("services.xml".equals(pieces[len - 1])) {
			return pieces[len - 2];
		}
		return null;
	}

	/**
	 * Get the project area ID for a project area name
	 * 
	 * @param client
	 * @param oslcCatalogUrl
	 * @param projectAreaName
	 * @return
	 * @throws ResourceNotFoundException
	 * @throws IOException
	 * @throws OAuthException
	 * @throws URISyntaxException
	 */
	public static ProjectAreaOslcServiceProvider findProjectAreaOslcServiceProvider(final JazzFormAuthClient client,
			final String oslcCatalogUrl, final String projectAreaName)
			throws URISyntaxException, ResourceNotFoundException, IOException, OAuthException {
		String projectArea = projectAreaName;
		if (projectArea == null) {
			projectArea = "";
		}
		String projectAreaServiceProviderUrl = client.lookupServiceProviderUrl(oslcCatalogUrl, projectArea);
		ProjectAreaOslcServiceProvider projectAreaOslcService = new ProjectAreaOslcServiceProvider(
				projectAreaServiceProviderUrl, projectArea);
		return projectAreaOslcService;
	}
}
