package com.ibm.requirement.typemanagement.oslc.client.dngcm;

import java.util.List;

import org.eclipse.lyo.client.oslc.jazz.JazzFormAuthClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ibm.requirement.typemanagement.oslc.client.automation.util.CsvConfigurtionArchiveInformation;

public class ConfigurationArchiveUtil {
	public static final Logger logger = LoggerFactory.getLogger(ConfigurationArchiveUtil.class);
	
	public static boolean archiveConfigurations(JazzFormAuthClient client,
			List<CsvConfigurtionArchiveInformation> configurations, final int testmode) {
		for (CsvConfigurtionArchiveInformation confToArchive : configurations) {
			
			String conf = confToArchive.getConfiguration();
			if(canbeArchived(client,conf, testmode)) {
				logger.info("Archiving '{}'", conf); // Archive Configuration.
				InternalConfigurationArchiveApi.archiveWithDescendants(client, conf, testmode);
			} else {
				logger.info("Skipping '{}'", conf); // Archive Configuration.				
			}
		}
		return true;
	}

	private static boolean canbeArchived(JazzFormAuthClient client, String confToArchive, final int testmode) {
		CallStatus dependent = DependentConfigurationsApi.hasNoDependentConfiguration(client, confToArchive, testmode);
		if(dependent.callSuccess && dependent.getNoResults() == 0)
			return true;
		return false;
	}

}
