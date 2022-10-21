package com.ibm.requirement.typemanagement.oslc.client.dngcm;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.net.URLEncoder;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.eclipse.lyo.client.oslc.jazz.JazzFormAuthClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ibm.requirement.typemanagement.oslc.client.resources.Configuration;

import net.oauth.OAuthException;

/**
 * Internal API implementation to archive configurations Usage of this API voids
 * support.
 * 
 * @deprecated
 * 
 *
 */
public class InternalConfigurationArchiveApi {

	public static final Logger logger = LoggerFactory.getLogger(InternalConfigurationArchiveApi.class);
	public static final String archiveWithDescendants = "/localVersioning/configurations/archiveWithDescendants";

	/**
	 * @param client
	 * @param configuration
	 * @return
	 * @throws IOException
	 * @throws OAuthException
	 * @throws URISyntaxException
	 */
	public static CallStatus archiveWithDescendants(final JazzFormAuthClient client, final Configuration configuration,
			final int testmode) throws IOException, OAuthException, URISyntaxException {
		if (configuration == null) {
			String message = "Error: parameter 'configuration' must not be null";
			logger.info(message);
			CallStatus status = new CallStatus(message);
			return status;
		}
		return archiveWithDescendants(client, configuration.getAbout().toString(), testmode);
	}

	/**
	 * @param client
	 * @param configuration
	 * @return
	 */
	public static CallStatus archiveWithDescendants(final JazzFormAuthClient client, final String configuration,
			final int testmode) {
		CallStatus resultStatus = new CallStatus();
		InputStream input = null;
		if (configuration == null) {
			String message = "Error: parameter 'configuration' must not be null";
			logger.info(message);
			resultStatus.setMessage(message);
			return resultStatus;
		}

		if (testmode == 3) {
			resultStatus.setCallSuccess(true);
			resultStatus.setCallResult(true);
			return resultStatus;
		}

		//
		// POST
		// https://jazz.ibm.com:9443/rm/localVersioning/configurations/archiveWithDescendants?configurationUri=https%3A%2F%2Fjazz.ibm.com%3A9443%2Frm%2Fcm%2Fbaseline%2F_UdEZMN_9EeuEmPSHUJVGfg
		// HTTP/1.1
		String url = client.getUrl();
		String param = null;
		try {
			param = URLEncoder.encode(configuration, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			String message = "Exception unsupported encoding: " + e.getMessage();
			logger.error(message);
			resultStatus.setMessage(message);
			return resultStatus;
		}

		if (testmode != 0) {
			param = param.concat("Fail");
		}

		final String finalUrl = url.concat(archiveWithDescendants).concat("?").concat("configurationUri=")
				.concat(param);

		logger.debug("archive with descendants");
		HttpResponse response = null;
		try {
			if (testmode == 2) {
				throw new Exception("TestException");
			}
			HttpClient rawClient = client.getHttpClient();
			HttpUriRequest archive = new HttpPost(finalUrl);
			archive.addHeader("Accept", "*/*");
			response = rawClient.execute(archive);
			final HttpEntity entity = response.getEntity();
			if (entity != null) {
				input = entity.getContent();
			}
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
				resultStatus.setCallSuccess(true);
				String aResult = response.toString();
				if (input != null) {
					input.close();
				}
				logger.debug("Result '{}'.", aResult);
				break;
			default:
				String message = "Unexpected return code '" + statusCode + "'.";
				logger.debug(message);
				if (input != null) {
					input.close();
				}
				resultStatus.setMessage(message);
				break;
			}
		} catch (Exception e) {
			String message = "Exception " + e.getMessage();
			// logger.error(message);
			// e.printStackTrace();
			resultStatus.setMessage(message);
			return resultStatus;
		} finally {
			if (response != null) {
				response = null;
			}
		}
		return resultStatus;
	}

}
