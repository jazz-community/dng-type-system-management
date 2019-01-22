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
package com.ibm.requirement.typemanagement.oslc.client.resources;

import java.net.URI;

import org.eclipse.lyo.oslc4j.core.annotation.OslcDescription;
import org.eclipse.lyo.oslc4j.core.annotation.OslcNamespace;
import org.eclipse.lyo.oslc4j.core.annotation.OslcOccurs;
import org.eclipse.lyo.oslc4j.core.annotation.OslcPropertyDefinition;
import org.eclipse.lyo.oslc4j.core.annotation.OslcRange;
import org.eclipse.lyo.oslc4j.core.annotation.OslcReadOnly;
import org.eclipse.lyo.oslc4j.core.annotation.OslcResourceShape;
import org.eclipse.lyo.oslc4j.core.annotation.OslcTitle;
import org.eclipse.lyo.oslc4j.core.annotation.OslcValueType;
import org.eclipse.lyo.oslc4j.core.model.AbstractResource;
import org.eclipse.lyo.oslc4j.core.model.Occurs;
import org.eclipse.lyo.oslc4j.core.model.OslcConstants;
import org.eclipse.lyo.oslc4j.core.model.ValueType;

import com.ibm.requirement.typemanagement.oslc.client.automation.DngTypeSystemManagementConstants;

/**
 * Represents a RM component
 *
 */
@OslcNamespace(DngTypeSystemManagementConstants.CONFIGUTATION_MANAGEMENT_NAMESPACE)
@OslcResourceShape(title = "Component Shape", describes = DngTypeSystemManagementConstants.CONFIGUTATION_MANAGEMENT_NAMESPACE
		+ DngTypeSystemManagementConstants.CONFIGUTATION_MANAGEMENT_COMPONENT_TYPE)
public class Component extends AbstractResource {

	private String title;
	private URI serviceProvider;
	private URI configurations;
	private URI projectArea;
	private URI accessContext;

	public Component() {
		super();
	}

	public Component(URI about) {
		super(about);
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public void setServiceProvider(URI serviceProvider) {
		this.serviceProvider = serviceProvider;
	}

	public void setConfigurations(URI configurations) {
		this.configurations = configurations;
	}

	public void setProjectArea(URI projectArea) {
		this.projectArea = projectArea;
	}

	public void setAccessContext(URI accessContext) {
		this.accessContext = accessContext;
	}

	@OslcDescription("A name for the component.")
	@OslcTitle("Title")
	@OslcPropertyDefinition(OslcConstants.DCTERMS_NAMESPACE + DngTypeSystemManagementConstants.DC_PROPERTY_NAME_TITLE)
	@OslcReadOnly
	@OslcOccurs(Occurs.ExactlyOne)
	@OslcValueType(ValueType.XMLLiteral)
	public String getTitle() {
		return title;
	}

	@OslcDescription("The scope of a resource is a URI for the resource's OSLC Service Provider.")
	@OslcTitle("Service Provider")
	@OslcPropertyDefinition(OslcConstants.OSLC_CORE_NAMESPACE
			+ DngTypeSystemManagementConstants.CONFIGUTATION_MANAGEMENT_PROPERTY_SERVICEPROVIDER)
	@OslcRange(OslcConstants.TYPE_SERVICE_PROVIDER)
	public URI getServiceProvider() {
		return serviceProvider;
	}

	@OslcDescription("Available Configurations such as streams and baselines.")
	@OslcTitle("Configurations")
	@OslcPropertyDefinition(DngTypeSystemManagementConstants.CONFIGUTATION_MANAGEMENT_NAMESPACE
			+ DngTypeSystemManagementConstants.CONFIGUTATION_MANAGEMENT_PROPERTY_CONFIGURATIONS)
	@OslcReadOnly
	public URI getConfigurations() {
		return configurations;
	}

	@OslcDescription("Project area process.")
	@OslcTitle("Projectarea")
	@OslcPropertyDefinition(DngTypeSystemManagementConstants.PROCESS_NAMESPACE
			+ DngTypeSystemManagementConstants.PROCESS_NAMESPACE_PROPERTY_PROJECT_AREA)
	@OslcReadOnly
	public URI getProjectArea() {
		return projectArea;
	}

	@OslcDescription("Access context.")
	@OslcTitle("Projectarea")
	@OslcPropertyDefinition(DngTypeSystemManagementConstants.CORE_ACCESS_CONTEXT_NAMESPACE + "accessContext")
	@OslcReadOnly
	public URI getAccessContext() {
		return accessContext;
	}
}
