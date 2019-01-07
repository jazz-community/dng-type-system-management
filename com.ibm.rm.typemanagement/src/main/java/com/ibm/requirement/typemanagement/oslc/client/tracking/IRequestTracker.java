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
 *    Ralph Schoon		- Small changes 
 *******************************************************************************/
package com.ibm.requirement.typemanagement.oslc.client.tracking;

import java.net.URI;
import java.net.URISyntaxException;

public interface IRequestTracker {

	public static final String TRACKER_VERDICT_PASSED = "http://open-services.net/ns/auto#passed";
	public static final String TRACKER_VERDICT_ERROR = "http://open-services.net/ns/auto#error";
	public static final String TRACKER_VERDICT_FAILED = "http://open-services.net/ns/auto#failed";
	public static final String TRACKER_VERDICT_UNAVAILABLE = "http://open-services.net/ns/auto#unavailable";

	public static final String TRACKER_STATE_IN_PROGRESS = "http://open-services.net/ns/auto#inProgress";
	public static final String TRACKER_STATE_COMPLETE = "http://open-services.net/ns/auto#complete";

	public static final String TRAKCER_VERDICT_URI = "http://open-services.net/ns/auto#verdict";
	public static final String TRACKER_STATE_URI = "http://open-services.net/ns/auto#state";
	public static final String TRACKER_MESSAGE_URI = "http://open-services.net/ns/core#message";
	public static final String TRACKER_REFERENCE_URI = "http://purl.org/dc/terms/references";

	public URI getVerdict() throws URISyntaxException;

	public URI getState() throws URISyntaxException;

	public String getMessage();

	public URI getReferences() throws URISyntaxException;

}
