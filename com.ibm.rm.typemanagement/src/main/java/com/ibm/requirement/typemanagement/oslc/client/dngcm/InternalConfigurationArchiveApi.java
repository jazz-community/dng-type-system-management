package com.ibm.requirement.typemanagement.oslc.client.dngcm;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.net.URLEncoder;

import org.apache.http.client.HttpClient;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.eclipse.lyo.client.oslc.jazz.JazzFormAuthClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ibm.requirement.typemanagement.oslc.client.resources.Configuration;

import net.oauth.OAuthException;

/**
 * Internal API implementation to archive configurations 
 *
 */
public class InternalConfigurationArchiveApi {
	
	public static final Logger logger = LoggerFactory.getLogger(InternalConfigurationArchiveApi.class);
	public static final String localVersioning = "/localVersioning/configurations/archiveWithDescendants";

	public static boolean archiveWithDescendants(final JazzFormAuthClient client, final Configuration configuration)
			throws IOException, OAuthException, URISyntaxException {
		if (configuration == null) {
			logger.info("Configuration must not be null");
			return false;
		}
		return archiveWithDescendants(client, configuration.getAbout().toString());
	}


	public static boolean archiveWithDescendants(final JazzFormAuthClient client, final String configuration) {

		boolean result = false;
		if (configuration == null) {
			logger.info("Change set must not be null");
			return result;
		}
		//POST https://jazz.ibm.com:9443/rm/localVersioning/configurations/archiveWithDescendants?configurationUri=https%3A%2F%2Fjazz.ibm.com%3A9443%2Frm%2Fcm%2Fbaseline%2F_UdEZMN_9EeuEmPSHUJVGfg HTTP/1.1
		String url = client.getUrl();
		String param = null;
		try {
			 param = URLEncoder.encode(configuration, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			logger.error("Exception unsupported encoding: " + e.getMessage());
			return result;
		}
		final String finalUrl = url.concat(localVersioning).concat("?").concat("configurationUri=").concat(param);
		
		logger.debug("archive with descendants");
		HttpResponse response = null;
		try {
			HttpClient rawClient = client.getHttpClient();
			HttpUriRequest archive = new HttpPost(finalUrl);
			archive.addHeader("Accept", "*/*");
			response = rawClient.execute(archive);
			int statusCode = response.getStatusLine().getStatusCode();

			logger.debug("Status: " + Integer.toString(statusCode));
			/**
			 * 
			 * Behavior of POST
			 * 
			 * 200 archived.
			 *
			 * 
			 */
			switch (statusCode) {
			case 200:
				result = true;
				logger.debug("Result '{}'.", result);
				break;
			default:
				logger.debug("Unexpected return code.");
				break;
			}
		} catch (Exception e) {
			logger.error("Exception '{}'.", e.getMessage());
			e.printStackTrace();
		} finally {
			if (response != null) {
				response = null;
			}
		}
		return result;
	}


}
