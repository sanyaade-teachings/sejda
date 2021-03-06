/*
 * Created on Sep 3, 2011
 * Copyright 2010 by Eduard Weissmann (edi.weissmann@gmail.com).
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at 
 * 
 * http://www.apache.org/licenses/LICENSE-2.0 
 * 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and 
 * limitations under the License. 
 */
package org.sejda.conversion;

import static org.sejda.common.XMLUtils.nullSafeGetStringAttribute;

import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathException;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.sejda.conversion.exception.ConversionException;
import org.sejda.model.exception.SejdaRuntimeException;
import org.sejda.model.input.PdfFileSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * Adapter for a list of {@link PdfFileSource}s. Provides a filePath based constructor. Will parse xml, csv config file formats, and list a directory contents
 * 
 * @author Eduard Weissmann
 * 
 */
public class PdfFileSourceListAdapter {

    private static final Logger LOG = LoggerFactory.getLogger(PdfFileSourceListAdapter.class);

    private final PdfInputFilesSourceFactory parserFactory = new PdfInputFilesSourceFactory();
    private final List<PdfFileSource> fileSourceList = new ArrayList<PdfFileSource>();
    private final File file;
    private Pattern pattern = Pattern.compile(".+");

    public PdfFileSourceListAdapter(String filePath) {
        file = new File(filePath);

        if (!file.exists()) {
            throw new ConversionException("File '" + file.getPath() + "' does not exist");
        }
    }

    public PdfFileSourceListAdapter filter(String filterRegExp) {
        if (StringUtils.isNotBlank(filterRegExp)) {
            LOG.debug("Applying regular expression: {}", filterRegExp);
            pattern = Pattern.compile(filterRegExp);
        }
        return this;
    }

    public List<PdfFileSource> getFileSourceList() {
        fileSourceList.addAll(parserFactory.createSource(file).getInputFiles(file));

        if (fileSourceList.isEmpty()) {
            throw new ConversionException("No input files specified in '" + file.getPath() + "'");
        }
        return fileSourceList;
    }

    /**
     * Factory for {@link PdfInputFilesSource}s. Depending on input {@link File} (folder, csv file, xml file), a different source will be created.
     * 
     * @author Eduard Weissmann
     * 
     */
    class PdfInputFilesSourceFactory {
        private static final String XML_EXTENSION = "xml";
        private static final String CSV_EXTENSION = "csv";

        PdfInputFilesSource createSource(File file) {
            String extension = FilenameUtils.getExtension(file.getName());

            if (file.isDirectory()) {
                return new FolderFileSourceListParser(PdfFileSourceListAdapter.this.pattern);
            } else if (CSV_EXTENSION.equalsIgnoreCase(extension)) {
                return new CsvFileSourceListParser();
            } else if (XML_EXTENSION.equalsIgnoreCase(extension)) {
                return new XmlFileSourceListParser();
            }

            throw new SejdaRuntimeException("Cannot read input file names from config file '" + file.getName()
                    + "'. Unsupported file format: " + extension);
        }
    }
}

/**
 * Source for {@link PdfFileSource} input files
 * 
 * @author Eduard Weissmann
 * 
 */
interface PdfInputFilesSource {

    List<PdfFileSource> getInputFiles(File file);
}

/**
 * Abstract base class of {@link PdfInputFilesSource}s
 * 
 * @author Eduard Weissmann
 * 
 */
abstract class AbstractPdfInputFilesSource implements PdfInputFilesSource {
    private static final Logger LOG = LoggerFactory.getLogger(AbstractPdfInputFilesSource.class);

    public List<PdfFileSource> getInputFiles(File file) {
        List<String> filenames = parseFileNames(file);
        LOG.trace("Input files: '" + StringUtils.join(filenames, "', '") + "'");
        try {
            return PdfFileSourceAdapter.fromStrings(filenames);
        } catch (SejdaRuntimeException e) {
            throw new ConversionException("Invalid filename found: " + e.getMessage(), e);
        }
    }

    protected abstract List<String> parseFileNames(File file);
}

/**
 * Produces the list of input files by listing a directory for files with pdf extension
 * 
 * @author Eduard Weissmann
 * 
 */
class FolderFileSourceListParser extends AbstractPdfInputFilesSource {

    protected static final String PDF_EXTENSION = "pdf";

    private Pattern pattern;

    FolderFileSourceListParser(Pattern pattern) {
        this.pattern = pattern;
    }

    @Override
    protected List<String> parseFileNames(File file) {
        List<File> files = Arrays.asList(file.listFiles(new FilenameFilter() {

            public boolean accept(File dir, String filename) {
                Matcher matcher = pattern.matcher(filename);
                String extension = FilenameUtils.getExtension(filename);
                return StringUtils.equalsIgnoreCase(extension, PDF_EXTENSION) && matcher.matches();
            }

        }));

        List<String> filenames = new ArrayList<String>();
        for (File current : files) {
            filenames.add(current.getAbsolutePath());
        }

        Collections.sort(filenames);

        return filenames;
    }

}

/**
 * Produces the list of input files by parsing a csv file
 * 
 * @author Eduard Weissmann
 * 
 */
class CsvFileSourceListParser extends AbstractPdfInputFilesSource {
    private static final Logger LOG = LoggerFactory.getLogger(CsvFileSourceListParser.class);

    @Override
    protected List<String> parseFileNames(File file) {
        try {
            return doParseFileNames(file);
        } catch (Exception e) {
            LOG.error("Can't extract filesnames", e);
            throw new ConversionException("Can't extract filenames from '" + file.getName() + "'. Reason:"
                    + e.getMessage(), e);
        }
    }

    protected List<String> doParseFileNames(File file) throws IOException {
        List<String> resultingFileNames = new ArrayList<String>();

        List<String> lines = IOUtils.readLines(new FileInputStream(file));
        for (String eachLine : lines) {
            String[] splitLine = StringUtils.split(eachLine.toString(), ",");
            resultingFileNames.addAll(Arrays.asList(splitLine));
        }

        return resultingFileNames;
    }
}

/**
 * Produces the list of input files by parsing a xml file
 * 
 * @author Eduard Weissmann
 * 
 */
class XmlFileSourceListParser extends AbstractPdfInputFilesSource {
    private static final Logger LOG = LoggerFactory.getLogger(XmlFileSourceListParser.class);

    private XPathFactory xpathFactory = XPathFactory.newInstance();

    @Override
    protected List<String> parseFileNames(File file) {
        try {
            return doParseFileNames(file);
        } catch (Exception e) {
            LOG.error("Can't extract filenames", e);
            throw new ConversionException("Can't extract filenames from '" + file.getName() + "'. Reason:"
                    + e.getMessage(), e);
        }
    }

    protected List<String> doParseFileNames(File file) throws IOException, SAXException, ParserConfigurationException,
            XPathException {
        DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance();
        domFactory.setNamespaceAware(true);
        DocumentBuilder builder = domFactory.newDocumentBuilder();
        Document doc = builder.parse(file);

        List<String> result = new ArrayList<String>();

        result.addAll(parseSingleFiles(doc));
        result.addAll(parseFileSets(doc, file));

        return result;
    }

    /**
     * Parse fileset definitions <filelist><fileset>[...]</fileset></filelist> ignoring the rest of the document
     * 
     * @param doc
     * @return a list of string matching the contents of the <filelist><fileset> tags in the document
     * @throws XPathExpressionException
     */
    private List<String> parseFileSets(Document doc, File configFile) throws XPathExpressionException {
        List<String> result = new ArrayList<String>();

        NodeList nodeList = getNodeListMatchingXpath("//filelist/fileset/file", doc);
        for (int i = 0; i < nodeList.getLength(); i++) {
            Node node = nodeList.item(i);
            Node fileSet = node.getParentNode();

            String parentDirPath = nullSafeGetStringAttribute(fileSet, "dir");
            if (parentDirPath == null) {
                parentDirPath = configFile.getAbsoluteFile().getParent();
            }

            String filePath = extractFilePath(node);

            // warn if file in fileset is using absolute path mode
            if (FilenameUtils.getPrefixLength(filePath) > 0) {
                LOG.warn("File "
                        + filePath
                        + " in fileset "
                        + StringUtils.defaultIfBlank(nullSafeGetStringAttribute(fileSet, "dir"), "")
                        + " seems to be an absolute path. Will _not_ be resolved relative to the <fileset>, but as an absolute path. Normally you would want to use relative paths in a //filelist/fileset/file, and absolute paths in a //filelist/file.");
            }

            result.add(FilenameUtils.concat(parentDirPath, filePath));
        }

        return result;
    }

    private String extractFilePath(Node fileNode) {
        String password = nullSafeGetStringAttribute(fileNode, "password");
        String value = nullSafeGetStringAttribute(fileNode, "value");
        return value + (password == null ? "" : PdfFileSourceAdapter.PASSWORD_SEPARATOR_CHARACTER + password);
    }

    /**
     * Parse single file definitions <filelist><file>[...]</file></filelist> ignoring the rest of the document
     * 
     * @param doc
     * @return a list of string matching the contents of the <filelist><file> tags in the document
     * @throws XPathExpressionException
     */
    private List<String> parseSingleFiles(Document doc) throws XPathExpressionException {
        List<String> result = new ArrayList<String>();

        NodeList nodeList = getNodeListMatchingXpath("//filelist/file", doc);
        for (int i = 0; i < nodeList.getLength(); i++) {
            result.add(extractFilePath(nodeList.item(i)));
        }

        return result;
    }

    private NodeList getNodeListMatchingXpath(String xpathString, Document doc) throws XPathExpressionException {
        return (NodeList) xpathFactory.newXPath().evaluate(xpathString, doc, XPathConstants.NODESET);
    }
}
