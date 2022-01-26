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
package com.ibm.requirement.typemanagement.oslc.client.automation.util;

import java.net.URISyntaxException;

import com.ibm.requirement.typemanagement.oslc.client.resources.Configuration;
import com.opencsv.bean.CsvBindByName;

/**
 * This class carries the information that is stored in a CSV file or read from
 * it. Annotations are used for the import and export process.
 * 
 * @see http://opencsv.sourceforge.net/
 *
 */
public class CsvExportImportInformation {

	@CsvBindByName
	private String source;
	// @CsvBindByName
	// private String sourceTitle;
	// @CsvBindByName
	// private String sourceDescription;
	// @CsvBindByName
	// private String sourceComponent;
	// @CsvBindByName
	// private String sourceComponentName;
	@CsvBindByName
	private String target;
	@CsvBindByName
	private String targetTitle;
	@CsvBindByName
	private String targetDescription;
	@CsvBindByName
	private String targetComponentName;
	// @CsvBindByName
	// private String targetComponent;
	@CsvBindByName
	private String projectAreaName;

	/**
	 * The preferred order of columns for human usage
	 * 
	 * @return
	 */
	public static String[] getColumnMapping() {
		return new String[] { "source", "target", "targetTitle", "targetDescription", "targetComponentName",
				"projectAreaName" };
	}

	public CsvExportImportInformation() {
		super();
	}

	@SuppressWarnings("deprecation")
	public CsvExportImportInformation(final Configuration source, final Configuration target,
			final String projectAreaName) throws URISyntaxException {
		super();
		this.projectAreaName = projectAreaName;
		if (source != null) {
			this.source = source.getAbout().toString();
			// this.sourceTitle = source.getTitle();
			// this.sourceDescription = source.getDescription();
			// this.sourceComponent = source.getComponent().toString();
			// this.sourceComponentName = source.getComponentName();
		}
		if (target != null) {
			this.target = target.getAbout().toString();
			this.targetTitle = target.getTitle();
			this.targetDescription = target.getDescription();
			// this.targetComponent = target.getComponent().toString();
			this.targetComponentName = target.getComponentName();
		}
	}

	// public String getTargetComponent() {
	// return targetComponent;
	// }

	// public void setTargetComponent(String targetComponent) {
	// this.targetComponent = targetComponent;
	// }

	public String getTargetComponentName() {
		return targetComponentName;
	}

	public void setTargetComponentName(String targetComponentName) {
		this.targetComponentName = targetComponentName;
	}

	public String getSource() {
		return source;
	}

	public void setSource(String source) {
		this.source = source;
	}

	// public String getSourceTitle() {
	// return sourceTitle;
	// }
	//
	// public void setSourceTitle(String sourceTitle) {
	// this.sourceTitle = sourceTitle;
	// }
	//
	// public String getSourceDescription() {
	// return sourceDescription;
	// }
	//
	// public void setSourceDescription(String sourceDescription) {
	// this.sourceDescription = sourceDescription;
	// }

	public String getTarget() {
		return target;
	}

	public void setTarget(String target) {
		this.target = target;
	}

	public String getTargetTitle() {
		return targetTitle;
	}

	public void setTargetTitle(String targetTitle) {
		this.targetTitle = targetTitle;
	}

	public String getTargetDescription() {
		return targetDescription;
	}

	public void setTargetDescription(String targetDescription) {
		this.targetDescription = targetDescription;
	}

	public String getProjectAreaName() {
		return projectAreaName;
	}

	public void setProjectAreaName(String projectAreaName) {
		this.projectAreaName = projectAreaName;
	}
}
