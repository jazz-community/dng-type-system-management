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
package com.ibm.requirement.typemanagement.oslc.client.automation;

/**
 * Various constants used in the application.
 *
 */
public interface DngTypeSystemManagementConstants {

	public static final String VERSIONINFO = "1.3";

	// Commands and parameters
	public static final String PARAMETER_COMMAND = "command";
	public static final String PARAMETER_COMMAND_DESCRIPTION = "The command to execute.";
	public static final String PARAMETER_COMMAND_EXAMPLE = "exportConfigurations";

	public static final String CMD_EXPORT_CONFIGURATIONS = "exportConfigurations";
	public static final String CMD_IMPORT_TYPE_SYSTEM = "importTypeSystem";
	public static final String CMD_DELIVER_TYPE_SYSTEM = "deliverTypeSystem";
	public static final String CMD_EXPORT_CONFIGURATIONS_BY_DESCRIPTION = "exportConfigurationsByDescription";
	public static final String CMD_EXPORT_ALL_CONFIGURATIONS_BY_DESCRIPTION = "exportAllConfigurationsByDescription";

	public static final String CMD_IMPORT_TYPESYSTEM_BY_DESCRIPTION = "importTypeSystemByDescription";
	public static final String CMD_DELIVER_TYPESYSTEM_BY_DESCRIPTION = "deliverTypeSystemByDescription";

	public static final String PARAMETER_URL = "url";
	public static final String PARAMETER_URL_DESCRIPTION = "The Public URI of the application.";
	public static final String PARAMETER_URL_EXAMPLE = "https://clm.example.com:9443/rm/";
	public static final String PARAMETER_URL_PROTOTYPE = "https://<server>:port/<context>/";

	public static final String PARAMETER_USER = "user";
	public static final String PARAMETER_USER_ID_DESCRIPTION = "The user ID of a user.";
	public static final String PARAMETER_USER_ID_EXAMPLE = "ADMIN";
	public static final String PARAMETER_USER_PROTOTYPE = "<userId>";

	public static final String PARAMETER_PASSWORD = "password";
	public static final String PARAMETER_PASSWORD_DESCRIPTION = "The password of the user.";
	public static final String PARAMETER_PASSWORD_EXAMPLE = "******";
	public static final String PARAMETER_PASSWORD_PROTOTYPE = "<password>";

	public static final String PARAMETER_CSV_FILE_PATH = "csvfile";
	public static final String PARAMETER_CSV_FILE_PATH_DESCRIPTION = "The path to a CSV file.";
	public static final String PARAMETER_CSV_FILE_PATH_EXAMPLE = "\"..\\example.csv\"";
	public static final String PARAMETER_CSV_FILE_PATH_PROTOTYPE = "\"<csv_file_path>\"";

	public static final String PARAMETER_PROJECT_AREA = "project";
	public static final String PARAMETER_PROJECT_AREA_DESCRIPTION = "A project Area name.";
	public static final String PARAMETER_PROJECT_AREA_EXAMPLE = "\"JKE Banking (Requirements Management)\"";
	public static final String PARAMETER_PROJECT_AREA_PROTOTYPE = "\"<project_area>\"";

	public static final String PARAMETER_CSV_DELIMITER = "csvDelimiter";
	public static final String PARAMETER_CSV_DELIMITER_DESCRIPTION = "The delimiter character to be used in the CSV file.";
	public static final String PARAMETER_CSV_DELIMITER_EXAMPLE = "\";\"";
	public static final String PARAMETER_CSV_DELIMITER_PROTOTYPE = "\"character\"";

	public static final String PARAMETER_SOURCE_TAG = "sourceTag";
	public static final String PARAMETER_SOURCE_TAG_DESCRIPTION = "The tag to identify the source configuration";
	public static final String PARAMETER_SOURCE_TAG_EXAMPLE = "TSSource_TS1";
	public static final String PARAMETER_TAG_PROTOTYPE = "<tag>";

	public static final String PARAMETER_TARGET_TAG = "targetTag";
	public static final String PARAMETER_TARGET_TAG_DESCRIPTION = "The tag to identify the target configuration";
	public static final String PARAMETER_TARGET_TAG_EXAMPLE = "TSTarget_TS1";

	// Sample
	public static final String CMD_SAMPLE = "sampleCommand";

	public static final String PARAMETER_SAMPLE_OPTION = "mandatorySampleOption";
	public static final String PARAMETER_SAMPLE_OPTION_DESCRIPTION = "Mandatory sample option";
	public static final String PARAMETER_SAMPLE_OPTION_EXAMPLE = "\"mandatory value\"";
	public static final String PARAMETER_SAMPLE_OPTION_PROTOTYPE = "<mandatoryOptionValue>";

	public static final String PARAMETER_SAMPLE_OPTION_OPT = "optionalSampleOption";
	public static final String PARAMETER_SAMPLE_OPTION_OPT_DESCRIPTION = "Optional sample option";
	public static final String PARAMETER_SAMPLE_OPTION_OPT_EXAMPLE = "\"optional value\"";
	public static final String PARAMETER_SAMPLE_OPTION_OPT_PROTOTYPE = "<optionalOptionValue>";

	// Namespaces and types
	public static final String IBM_RDF_NAMESPACE = "http://www.ibm.com/xmlns/rdm/rdf/";
	public static final String JAZZ_PROCESS_MANAGMENT_NAMESPACE = "http://jazz.net/ns/process#";
	public static final String DMG_CONFIGURATION_MANAGEMENT_NAMESPACE = "http://jazz.net/ns/rm/dng/config#";
	public static final String PROCESS_NAMESPACE = "http://jazz.net/ns/process#";
	public static final String DELIVERY_SESSION_TYPE = "DeliverySession";
	public static final String DELIVERY_SESSION_STATE = "deliverySessionState";
	public static final String CONFIGUTATION_MANAGEMENT_NAMESPACE = "http://open-services.net/ns/config#";
	public static final String JAZZ_PROCESS_NAMESPACE = "http://jazz.net/ns/process#";
	public static final String CORE_ACCESS_CONTEXT_NAMESPACE = "http://open-services.net/ns/core/acc#";
	public static final String CONFIGUTATION_MANAGEMENT_COMPONENT_TYPE = "Component";
	public static final String CONFIGUTATION_MANAGEMENT_CONFIGURATION_TYPE = "Configuration";
	public static final String CONFIGUTATION_MANAGEMENT_CHANGESET_TYPE = "Changeset";
	public static final String CONFIGUTATION_MANAGEMENT_PROPERTY_CONFIGURATIONS = "configurations";
	public static final String CONFIGUTATION_MANAGEMENT_PROPERTY_SERVICEPROVIDER = "serviceProvider";
	public static final String DNG_TYPES_NAMESPACE = "http://www.ibm.com/xmlns/rdm/types/";
	public static final String TYPE_IMPORT_SESSION_TYPE = "TypeImportSession";
	public static final String DNG_TASK_NAMESPACE = "http://jazz.net/ns/rm/dng/task#";
	public static final String DNG_TASK_TYPE = "Task";
	public static final String OSLC_CONFIGUTATION_MANAGEMENT_NAMESPACE_PREFIX = "oslc_config";
	public static final String CM_LINKED_DATA_PLATFORM_CONTAINER_NAMESPACE = "http://www.w3.org/ns/ldp#";
	public static final String PROPERTY_NAME_LDP_CONTAINS_PROPERTY_NAME = "contains";

	// Headers
	public static final String DOORS_REQUEST_TYPE_HEADER = "DoorsRP-Request-Type";
	public static final String DOORS_REQUEST_TYPE_HEADER_VALUE = "public 2.0";
	public static final String ACCEPT_HEADER = "Accept";
	public static final String OSLC_VERSION_2_HEADER_VALUE = "2.0";
	public static final String CONTENT_TYPE_HEADER = "Content-Type";
	public static final String DNG_CM_CONFIGURATION_CONTEXT_HEADER = "Configuration-Context";
	public static final String CONTENT_TYPE_APPLICATION_JSON = "application/json";

	// Property namespaces and names for model searches
	public static final String DC_PROPERTY_NAME_TITLE = "title";

	public static final String RDF_PROPERTY_MEMBER_NAME = "member";

	public static final String CM_PROVIDER_CREATION_FACTORY = "creation";
	public static final String CM_SERVICE_PROVIDER_PROPERTY_NAME = "cmServiceProviders";
	public static final String PROCESS_NAMESPACE_PROPERTY_PROJECT_AREA = "projectArea";

	public static final String CMD_ARCHIVE_CONFIGURATIONS = "archiveConfigurations";

	public static final String CMD_ANALYZE_CONFIGURATIONS = "analyzeConfigurations";

	public static final String PARAMETER_PROCESS_ITEMS_LIMIT = "processItemsLimit";
	public static final String PARAMETER_PROCESS_ITEMS_LIMIT_DESCRIPTION = "Limit the maximum amount of items to be processed.";
	public static final String PARAMETER_PROCESS_ITEMS_LIMIT_PROTOTYPE = "<processItemsLimit>";
	public static final String PARAMETER_PROCESS_ITEMS_LIMIT_EXAMPLE = "1000";
}
