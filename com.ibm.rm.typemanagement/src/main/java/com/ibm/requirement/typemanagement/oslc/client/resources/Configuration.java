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
import java.util.Date;

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
 * Represents a RM configuration such as a stream, baseline or change set
 *
 */
@OslcNamespace(DngTypeSystemManagementConstants.CONFIGUTATION_MANAGEMENT_NAMESPACE)
@OslcResourceShape(title = "Configuration Shape", describes = DngTypeSystemManagementConstants.CONFIGUTATION_MANAGEMENT_NAMESPACE
		+ DngTypeSystemManagementConstants.CONFIGUTATION_MANAGEMENT_CONFIGURATION_TYPE)
public class Configuration extends AbstractResource {

	private Boolean isBaseline = null;
	private Boolean isChangeSet = null;
	private Boolean isStream = null;

	private String title;
	private URI serviceProvider;
	private URI projectArea;
	private URI baselines;
	private String identifier;
	private URI previousBaseline;
	private URI accessContext;
	private URI creator;
	private String description;
	private URI component;
	private URI acceptedBy;
	private URI selections;
	private Date created;
	private URI changesets;
	private URI overrides;

	// Artificial
	private String componentName = "Name not set";

	// TODO: Can we get the component name?
	public Configuration() {
		super();
	}

	public Configuration(URI about) {
		super(about);
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public void setServiceProvider(URI serviceProvider) {
		this.serviceProvider = serviceProvider;
	}

	public void setProjectArea(URI projectArea) {
		this.projectArea = projectArea;
	}

	public void setBaselines(URI baselines) {
		this.baselines = baselines;
	}

	public void setIdentifier(String identifier) {
		this.identifier = identifier;
	}

	public void setPreviousBaseline(URI previousBaseline) {
		this.previousBaseline = previousBaseline;
	}

	public void setAccessContext(URI accessContext) {
		this.accessContext = accessContext;
	}

	public void setCreator(URI creator) {
		this.creator = creator;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public void setComponent(URI component) {
		this.component = component;
	}

	public void setAcceptedBy(URI acceptedBy) {
		this.acceptedBy = acceptedBy;
	}

	public void setSelections(URI selections) {
		this.selections = selections;
	}

	public void setCreated(Date created) {
		this.created = created;
	}

	public void setChangesets(URI changesets) {
		this.changesets = changesets;
	}

	public boolean isBaseline() {
		if (isBaseline == null) {
			computeType(this.getAbout());
		}
		return isBaseline;
	}

	public boolean isChangeset() {
		if (isChangeSet == null) {
			computeType(this.getAbout());
		}
		return isChangeSet;
	}

	public boolean isStream() {
		if (isStream == null) {
			computeType(this.getAbout());
		}
		return isStream;
	}

	/**
	 * Compute the type of the object.
	 * 
	 * @param about
	 */
	private void computeType(final URI about) {
		String configuration = about.toString();
		if (configuration.contains("stream")) {
			isBaseline = false;
			isChangeSet = false;
			isStream = true;
			return;
		}
		if (configuration.contains("baseline")) {
			isBaseline = true;
			isChangeSet = false;
			isStream = false;
			return;
		}
		if (configuration.contains("changeset")) {
			isBaseline = false;
			isChangeSet = true;
			isStream = false;
			return;
		}
	}

	public void setComponentName(String componentName) {
		this.componentName = componentName;
	}

	public String getComponentName() {
		return this.componentName;
	}

	public URI setComponent() {
		return overrides;
	}

	public void setOverrides(URI overrides) {
		this.overrides = overrides;
	}

	@OslcDescription("The configuration that this configuration overrides.")
	@OslcPropertyDefinition(DngTypeSystemManagementConstants.CONFIGUTATION_MANAGEMENT_NAMESPACE + "overrides")
	@OslcReadOnly
	@OslcOccurs(Occurs.ZeroOrOne)
	@OslcTitle("Overrides")
	public URI getOverrides() {
		return overrides;
	}

	@OslcDescription("A name for the configuration.")
	@OslcPropertyDefinition(OslcConstants.DCTERMS_NAMESPACE + DngTypeSystemManagementConstants.DC_PROPERTY_NAME_TITLE)
	@OslcReadOnly
	@OslcOccurs(Occurs.ExactlyOne)
	@OslcTitle("Title")
	@OslcValueType(ValueType.XMLLiteral)
	public String getTitle() {
		return title;
	}

	@OslcDescription("The scope of a resource is a URI for the resource's OSLC Service Provider.")
	@OslcPropertyDefinition(OslcConstants.OSLC_CORE_NAMESPACE
			+ DngTypeSystemManagementConstants.CONFIGUTATION_MANAGEMENT_PROPERTY_SERVICEPROVIDER)
	@OslcRange(OslcConstants.TYPE_SERVICE_PROVIDER)
	@OslcTitle("Service Provider")
	public URI getServiceProvider() {
		return serviceProvider;
	}

	@OslcDescription("The project area this item belongs to.")
	@OslcPropertyDefinition(DngTypeSystemManagementConstants.JAZZ_PROCESS_NAMESPACE
			+ DngTypeSystemManagementConstants.PROCESS_NAMESPACE_PROPERTY_PROJECT_AREA)
	@OslcReadOnly
	@OslcOccurs(Occurs.ExactlyOne)
	@OslcTitle("ProjectArea")
	public URI getProjectArea() {
		return projectArea;
	}

	@OslcDescription("The baseline Factory and Query for this configuration.")
	@OslcPropertyDefinition(DngTypeSystemManagementConstants.CONFIGUTATION_MANAGEMENT_NAMESPACE + "baselines")
	@OslcReadOnly
	@OslcOccurs(Occurs.ExactlyOne)
	@OslcTitle("Baseline Factory")
	public URI getBaselines() {
		return baselines;
	}

	@OslcDescription("An unique identifyer for the object.")
	@OslcPropertyDefinition(OslcConstants.DCTERMS_NAMESPACE + "identifier")
	@OslcReadOnly
	@OslcOccurs(Occurs.ExactlyOne)
	@OslcTitle("Identifier")
	@OslcValueType(ValueType.XMLLiteral)
	public String getIdentifier() {
		return identifier;
	}

	@OslcDescription("The previous baseline.")
	@OslcPropertyDefinition(DngTypeSystemManagementConstants.CONFIGUTATION_MANAGEMENT_NAMESPACE + "previousBaseline")
	@OslcReadOnly
	@OslcOccurs(Occurs.ZeroOrOne)
	@OslcTitle("Previous Baseline")
	public URI getPreviousBaseline() {
		return previousBaseline;
	}

	// <acc:accessContext
	// rdf:resource="https://clm.example.com:9443/rm/acclist#_5i4eMKYbEei-7e3SSZvGSg"/>
	@OslcDescription("The Access Context for the configuration.")
	@OslcPropertyDefinition(DngTypeSystemManagementConstants.CORE_ACCESS_CONTEXT_NAMESPACE + "accessContext")
	@OslcReadOnly
	@OslcOccurs(Occurs.ExactlyOne)
	@OslcTitle("Access Context")
	public URI getAccessContext() {
		return accessContext;
	}

	@OslcDescription("The creator of the configuration.")
	@OslcPropertyDefinition(OslcConstants.DCTERMS_NAMESPACE + "creator")
	@OslcReadOnly
	@OslcOccurs(Occurs.ExactlyOne)
	@OslcTitle("Creator")
	public URI getCreator() {
		return creator;
	}

	@OslcDescription("A description.")
	@OslcPropertyDefinition(OslcConstants.DCTERMS_NAMESPACE + "description")
	@OslcReadOnly
	@OslcOccurs(Occurs.ZeroOrOne)
	@OslcTitle("Description")
	@OslcValueType(ValueType.XMLLiteral)
	public String getDescription() {
		return description;
	}

	@OslcDescription("The component this configuration refers to.")
	@OslcPropertyDefinition(DngTypeSystemManagementConstants.CONFIGUTATION_MANAGEMENT_NAMESPACE + "component")
	@OslcReadOnly
	@OslcOccurs(Occurs.ExactlyOne)
	@OslcTitle("component")
	public URI getComponent() {
		return component;
	}

	@OslcDescription("Accepted by.")
	@OslcPropertyDefinition(DngTypeSystemManagementConstants.CONFIGUTATION_MANAGEMENT_NAMESPACE + "acceptedBy")
	@OslcReadOnly
	@OslcOccurs(Occurs.ExactlyOne)
	@OslcTitle("Accepted by")
	public URI getAcceptedBy() {
		return acceptedBy;
	}

	@OslcDescription("Selections.")
	@OslcPropertyDefinition(DngTypeSystemManagementConstants.CONFIGUTATION_MANAGEMENT_NAMESPACE + "selections")
	@OslcReadOnly
	@OslcOccurs(Occurs.ExactlyOne)
	@OslcTitle("Selections")
	public URI getSelections() {
		return selections;
	}

	@OslcDescription("Date and time the configuration was created.")
	@OslcPropertyDefinition(OslcConstants.DCTERMS_NAMESPACE + "created")
	@OslcReadOnly
	@OslcOccurs(Occurs.ZeroOrOne)
	@OslcTitle("Creation Date")
	@OslcValueType(ValueType.DateTime)
	public Date getCreated() {
		return created;
	}

	@OslcDescription("The changesets.")
	@OslcPropertyDefinition(DngTypeSystemManagementConstants.DMG_CONFIGURATION_MANAGEMENT_NAMESPACE + "changesets")
	@OslcReadOnly
	@OslcOccurs(Occurs.ExactlyOne)
	@OslcTitle("Change Set Factory")
	public URI getChangesets() {
		return changesets;
	}

}
