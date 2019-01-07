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
 *    Matthias Buettgen - Initial implementation
 *******************************************************************************/
package com.ibm.requirement.typemanagement.oslc.client.resources;

import java.net.URI;

import org.eclipse.lyo.oslc4j.core.annotation.OslcNamespace;
import org.eclipse.lyo.oslc4j.core.annotation.OslcResourceShape;
import org.eclipse.lyo.oslc4j.core.model.AbstractResource;

/**
 * Can be used to retrieve the basic information for any OSLC types.
 *
 */
@OslcNamespace("http://www.w3.org/ns/ldp/")
@OslcResourceShape(title = "Basic Container Shape", describes = "http://www.w3.org/ns/ldp/BasicContainer")
public class Artifact extends AbstractResource {

	public Artifact() {
		super();
	}

	public Artifact(URI about) {
		super(about);
	}

}
