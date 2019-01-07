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

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URISyntaxException;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.SimpleSelector;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.wink.client.ClientResponse;
import org.eclipse.lyo.client.exception.ResourceNotFoundException;
import org.eclipse.lyo.client.oslc.jazz.JazzRootServicesHelper;

import net.oauth.OAuthException;

/**
 * Class with several utility methods that help to print and understand RDF
 * data.
 * 
 *
 */
public class RDFUtils {

	/**
	 * Print unprocessed response data
	 * 
	 * @param response
	 * @return
	 * @throws IOException
	 */
	public static String getRawResponse(final ClientResponse response) throws IOException {
		InputStream is = response.getEntity(InputStream.class);
		BufferedReader in = new BufferedReader(new InputStreamReader(is));
		String result = null;
		String line = null;
		while ((line = in.readLine()) != null) {
			if (result == null) {
				result = line;
			} else {
				result += "\n" + line;
			}
		}
		if (result == null) {
			result = "No data found";
		}
		return result;
	}

	/**
	 * Get the RDF statements from a response
	 * 
	 * @param response
	 * @param message  A message to be printed before the result
	 * @return
	 * @throws IOException
	 * @throws OAuthException
	 * @throws URISyntaxException
	 */
	public static String getRDFModelStatementsFromResponse(final ClientResponse response, final String message)
			throws IOException, OAuthException, URISyntaxException {

		Model model = ModelFactory.createDefaultModel();
		InputStream input = response.getEntity(InputStream.class);
		model.read(input, null);

		String result = getRDFModelStatements(model);
		if (result == null) {
			result = "No data found";
		}
		return message + "\n" + result;
	}

	/**
	 * Get the RDF statements from a response
	 * 
	 * @param response
	 * @return
	 * @throws IOException
	 * @throws OAuthException
	 * @throws URISyntaxException
	 */
	public static String getRDFModelStatementsFromResponse(final ClientResponse response)
			throws IOException, OAuthException, URISyntaxException {

		Model model = ModelFactory.createDefaultModel();
		InputStream input = response.getEntity(InputStream.class);
		model.read(input, null);

		String result = getRDFModelStatements(model);
		if (result == null) {
			result = "No data found";
		}
		return result;

	}

	/**
	 * Gets the RDF representation as XML as string with a message in front of it.
	 * 
	 * @param response
	 * @param message
	 * @return
	 * @throws IOException
	 * @throws OAuthException
	 * @throws URISyntaxException
	 */
	public static String getRDFRepresentation(final ClientResponse response, final String message)
			throws IOException, OAuthException, URISyntaxException {

		String result = getRDFRepresentation(response);
		if (result == null) {
			result = "No data found";
		}
		return "\n" + message + "\n" + result;
	}

	/**
	 * Gets the RDF representation as XML as string.
	 * 
	 * @param response
	 * @return
	 * @throws IOException
	 * @throws OAuthException
	 * @throws URISyntaxException
	 */
	public static String getRDFRepresentation(final ClientResponse response) {

		String result = null;
		try {
			Model model = ModelFactory.createDefaultModel();
			InputStream input = response.getEntity(InputStream.class);
			if (input.available() == 0) {
				return result;
			}
			model.read(input, null);

			result = getRDFRepresentation(model);
			if (result == null) {
				result = "No data found";
			}
		} catch (Exception e) {
			e.printStackTrace();
			result = "Exception: " + e.getMessage();
		}
		return result;
	}

	/**
	 * Get the statements of a RDF model as string.
	 * 
	 * @param model
	 * @return
	 */
	public static String getRDFModelStatements(final Model model) {
		String result = null;
		StmtIterator stmtIter = model.listStatements();
		while (stmtIter.hasNext()) {
			Statement stmt = stmtIter.next();
			if (result == null) {
				result = "\t Statement: " + stmt.toString();
			} else {
				result += "\n\t Statement: " + stmt.toString();
			}
			result += "\n\t\t Subject: " + stmt.getSubject() + " Predicate: " + stmt.getPredicate() + " Object: "
					+ stmt.getObject();
		}
		return result;
	}

	/**
	 * Get the RDF Representation of a model
	 * 
	 * @param rdfModel
	 * @return
	 */
	public static String getRDFRepresentation(final Model rdfModel) {
		ByteArrayOutputStream boas = new ByteArrayOutputStream();
		rdfModel.write(boas);
		return "\n" + boas.toString();
	}

	/**
	 * Custom method
	 * 
	 * @param namespace
	 * @param predicate
	 * @return
	 * @throws ResourceNotFoundException
	 */
	public static String getProperty(final JazzRootServicesHelper helper, final String namespace,
			final String predicate) {
		return getStatementForProperty(helper.getRdfModel(), namespace, predicate);
	}

	/**
	 * Get a statement for a property defined ba a namespace and a predicate as
	 * string
	 * 
	 * @param rdfModel
	 * @param namespace
	 * @param predicate
	 * @return
	 * @throws ResourceNotFoundException
	 */
	public static String getStatementForProperty(final Model rdfModel, final String namespace, final String predicate) {
		String returnVal = null;

		Property prop = rdfModel.createProperty(namespace, predicate);
		Statement stmt = rdfModel.getProperty((Resource) null, prop);
		if (stmt != null && stmt.getObject() != null)
			returnVal = stmt.getObject().toString();
		return returnVal;
	}

	/**
	 * Gets the statements for a property and returns the first found result as
	 * string.
	 * 
	 * example response, "http://jazz.net/ns/rm/dng/config#", "deliverySessionState"
	 * 
	 * 
	 * @param response
	 * @param nameSpace
	 * @param localName
	 * @return
	 * @throws URISyntaxException
	 */
	public static String getStatementForProperty(final ClientResponse response, final String nameSpace,
			final String localName) throws URISyntaxException {
		InputStream input = response.getEntity(InputStream.class);
		Model model = ModelFactory.createDefaultModel();
		model.read(input, null);

		StmtIterator statements = model
				.listStatements(new SimpleSelector(null, model.getProperty(nameSpace, localName), (RDFNode) null));

		while (statements.hasNext()) {
			Statement stmt = statements.next();
			return stmt.getObject().toString();
		}
		return null;
	}

}
