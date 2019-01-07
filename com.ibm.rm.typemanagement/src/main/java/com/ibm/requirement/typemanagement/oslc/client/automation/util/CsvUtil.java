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

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opencsv.CSVWriter;
import com.opencsv.bean.ColumnPositionMappingStrategy;
import com.opencsv.bean.CsvToBean;
import com.opencsv.bean.CsvToBeanBuilder;
import com.opencsv.bean.StatefulBeanToCsv;
import com.opencsv.bean.StatefulBeanToCsvBuilder;
import com.opencsv.exceptions.CsvBadConverterException;
import com.opencsv.exceptions.CsvDataTypeMismatchException;
import com.opencsv.exceptions.CsvRequiredFieldEmptyException;

/**
 * This class implements the CSV import and export methods.
 *
 */
public class CsvUtil {

	public static final Logger logger = LoggerFactory.getLogger(CsvUtil.class);

	char seperator = CSVWriter.DEFAULT_SEPARATOR;
	char quoteChar = CSVWriter.DEFAULT_QUOTE_CHARACTER;
	char escapeChar = CSVWriter.NO_ESCAPE_CHARACTER;
	String lineEnd = CSVWriter.DEFAULT_LINE_END;

	public CsvUtil() {
		super();
	}

	public CsvUtil(char seperator, char quoteChar) {
		super();
		this.seperator = seperator;
		this.quoteChar = quoteChar;
	}

	public CsvUtil(char seperator, char quoteChar, char escapeChar) {
		super();
		this.seperator = seperator;
		this.quoteChar = quoteChar;
		this.escapeChar = escapeChar;
	}

	public char getSeperator() {
		return seperator;
	}

	public void setSeperator(char seperator) {
		this.seperator = seperator;
	}

	public char getQuoteChar() {
		return quoteChar;
	}

	public void setQuoteChar(char quoteChar) {
		this.quoteChar = quoteChar;
	}

	public char getEscapeChar() {
		return escapeChar;
	}

	public void setEscapeChar(char escapeChar) {
		this.escapeChar = escapeChar;
	}

	public String getLineEnd() {
		return lineEnd;
	}

	/**
	 * Write the configuration information out to a CSV file.
	 * 
	 * @param csvFilePath
	 * @param configurationList
	 * @return
	 * @throws CsvDataTypeMismatchException
	 * @throws CsvRequiredFieldEmptyException
	 */
	public boolean exportConfigurationList(String csvFilePath, List<CsvExportImportInformation> configurationList) {
		boolean result = false;
		try {
			FileWriter fileWriter = new FileWriter(csvFilePath);
			CSVWriter writer = new CSVWriter(fileWriter, getSeperator(), getQuoteChar(), getEscapeChar(), getLineEnd());
			String[] columnMapping = CsvExportImportInformation.getColumnMapping();
			ColumnPositionMappingStrategy<CsvExportImportInformation> strategy = new ColumnPositionMappingStrategy<CsvExportImportInformation>();
			strategy.setColumnMapping(columnMapping);
			strategy.setType(CsvExportImportInformation.class);
			StatefulBeanToCsv<CsvExportImportInformation> beanToCsv2 = new StatefulBeanToCsvBuilder<CsvExportImportInformation>(
					writer).withMappingStrategy(strategy).build();
			// Write the header
			writer.writeNext(strategy.getColumnMapping());
			beanToCsv2.write(configurationList);
			writer.flush();
			writer.close();
			result = true;
		} catch (CsvBadConverterException e) {
			logger.error("CsvBadConverterException");
			// e.printStackTrace();
		} catch (IOException e) {
			logger.error("IOException creating CSV output writer '{}'", csvFilePath);
			// e.printStackTrace();
		} catch (CsvDataTypeMismatchException e) {
			logger.error("CsvDataTypeMismatchException");
			// e.printStackTrace();
		} catch (CsvRequiredFieldEmptyException e) {
			logger.error("CsvRequiredFieldEmptyException");
			// e.printStackTrace();
		}
		return result;
	}

	/**
	 * We use annotations for CSV reading
	 * 
	 * @see http://opencsv.sourceforge.net/
	 * 
	 * @param fileName
	 * @return List<CsvExportImportInformation> or null;
	 */
	public List<CsvExportImportInformation> readConfigurations(String filePath) {
		List<CsvExportImportInformation> configs = null;

		try {
			FileReader reader = new FileReader(filePath);
			CsvToBean<CsvExportImportInformation> csvToBean = new CsvToBeanBuilder<CsvExportImportInformation>(reader)
					.withType(CsvExportImportInformation.class).withSeparator(getSeperator())
					.withQuoteChar(getQuoteChar()).withEscapeChar(getEscapeChar()).build();
			configs = new ArrayList<CsvExportImportInformation>();
			for (CsvExportImportInformation csvExportImportInformation : csvToBean) {
				configs.add(csvExportImportInformation);
			}
			reader.close();
		} catch (FileNotFoundException e) {
			logger.error("File not found '{}'", filePath);
			// e.printStackTrace();
			// throw(e);
		} catch (IOException e) {
			logger.error("IOException " + e.getMessage());
			// e.printStackTrace();
			// throw(e);
		}
		return configs;
	}

}
