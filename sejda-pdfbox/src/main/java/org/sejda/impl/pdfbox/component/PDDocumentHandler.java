/*
 * Created on 29/ago/2011
 * Copyright 2011 by Andrea Vacondio (andrea.vacondio@gmail.com).
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
package org.sejda.impl.pdfbox.component;

import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.sejda.impl.pdfbox.util.ViewerPreferencesUtils.getPageLayout;
import static org.sejda.impl.pdfbox.util.ViewerPreferencesUtils.getPageMode;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;

import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.exceptions.COSVisitorException;
import org.apache.pdfbox.exceptions.CryptographyException;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentInformation;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.encryption.BadSecurityHandlerException;
import org.apache.pdfbox.pdmodel.encryption.DecryptionMaterial;
import org.apache.pdfbox.pdmodel.encryption.StandardDecryptionMaterial;
import org.apache.pdfbox.pdmodel.interactive.viewerpreferences.PDViewerPreferences;
import org.sejda.core.Sejda;
import org.sejda.model.exception.TaskException;
import org.sejda.model.exception.TaskIOException;
import org.sejda.model.pdf.PdfVersion;
import org.sejda.model.pdf.viewerpreference.PdfPageLayout;
import org.sejda.model.pdf.viewerpreference.PdfPageMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Wrapper over a {@link PDDocument}.
 * 
 * @author Andrea Vacondio
 */
public class PDDocumentHandler implements Closeable {

    private static final Logger LOG = LoggerFactory.getLogger(PDDocumentHandler.class);

    private PDDocument document;
    private PDDocumentAccessPermission permissions;

    /**
     * Creates a new handler using the given document as underlying {@link PDDocument}.
     * 
     * @param document
     * @param password
     * @throws TaskIOException
     */
    public PDDocumentHandler(PDDocument document, String password) throws TaskIOException {
        if (document == null) {
            throw new IllegalArgumentException("PDDocument cannot be null.");
        }
        this.document = document;
        decryptPDDocumentIfNeeded(password);
        permissions = new PDDocumentAccessPermission(document);
    }

    /**
     * Creates a new handler with an empty underlying {@link PDDocument}.
     * 
     * @throws TaskIOException
     */
    public PDDocumentHandler() throws TaskIOException {
        try {
            this.document = new PDDocument();
        } catch (IOException e) {
            throw new TaskIOException("An error occurred creating the PDDocument.", e);
        }
        permissions = new PDDocumentAccessPermission(document);
    }

    private void decryptPDDocumentIfNeeded(String password) throws TaskIOException {
        if (document.isEncrypted() && isNotBlank(password)) {
            DecryptionMaterial decryptionMaterial = new StandardDecryptionMaterial(password);
            LOG.trace("Decrypting input document");
            try {
                document.openProtection(decryptionMaterial);
            } catch (IOException e) {
                throw new TaskIOException("An error occurred reading cryptographic information.", e);
            } catch (BadSecurityHandlerException e) {
                throw new TaskIOException("Unable to decrypt the document.", e);
            } catch (CryptographyException e) {
                throw new TaskIOException("Unable to decrypt the document.", e);
            }
        }
    }

    /**
     * set the creator on the underlying {@link PDDocument}
     */
    public void setCreatorOnPDDocument() {
        document.getDocumentInformation().setCreator(Sejda.CREATOR);
    }

    /**
     * Set the document information on the underlying {@link PDDocument}
     * 
     * @param info
     */
    public void setDocumentInformation(PDDocumentInformation info) {
        document.setDocumentInformation(info);
    }

    /**
     * @return access permissions granted to this document.
     */
    public PDDocumentAccessPermission getPermissions() {
        return permissions;
    }

    /**
     * Sets the given page layout on the underlying {@link PDDocument}.
     * 
     * @param layout
     */
    public void setPageLayoutOnDocument(PdfPageLayout layout) {
        document.getDocumentCatalog().setPageLayout(getPageLayout(layout));
        LOG.trace("Page layout set to '{}'", layout);
    }

    /**
     * Sets the given page mode on the underlying {@link PDDocument}.
     * 
     * @param mode
     */
    public void setPageModeOnDocument(PdfPageMode mode) {
        document.getDocumentCatalog().setPageMode(getPageMode(mode));
        LOG.trace("Page mode set to '{}'", mode);
    }

    /**
     * Sets the version on the underlying {@link PDDocument}.
     * 
     * @param version
     */
    public void setVersionOnPDDocument(PdfVersion version) {
        if (version != null) {
            document.getDocument().setVersion((float) version.getVersionAsDouble());
            document.getDocument().setHeaderString(version.getVersionHeader());
            LOG.trace("Version set to '{}'", version);
        }
    }

    /**
     * Set compression of the XRef table on underlying {@link PDDocument}.
     * 
     * @param compress
     */
    public void compressXrefStream(boolean compress) {
        if (compress) {
            LOG.warn("Xref Compression not yet supported by PDFBox");
        }
    }

    /**
     * @return the view preferences for the underlying {@link PDDocument}.
     */
    public PDViewerPreferences getViewerPreferences() {
        PDViewerPreferences retVal = document.getDocumentCatalog().getViewerPreferences();
        if (retVal == null) {
            retVal = new PDViewerPreferences(new COSDictionary());
        }
        return retVal;
    }

    public void setViewerPreferences(PDViewerPreferences preferences) {
        document.getDocumentCatalog().setViewerPreferences(preferences);
    }

    public void close() throws IOException {
        document.close();
    }

    /**
     * Saves the underlying {@link PDDocument} removing security from it.
     * 
     * @param file
     * @throws TaskException
     */
    public void saveDecryptedPDDocument(File file) throws TaskException {
        savePDDocument(file, true);
    }

    /**
     * Saves the underlying {@link PDDocument} to the given file.
     * 
     * @param file
     * @throws TaskException
     */
    public void savePDDocument(File file) throws TaskException {
        savePDDocument(file, false);
    }

    private void savePDDocument(File file, boolean decrypted) throws TaskException {
        try {
            if (decrypted) {
                document.setAllSecurityToBeRemoved(decrypted);
            }
            LOG.trace("Saving document to {}", file);
            document.save(file.getAbsolutePath());
        } catch (COSVisitorException e) {
            throw new TaskException("An error occured saving to temporary file.", e);
        } catch (IOException e) {
            throw new TaskIOException("Unable to save to temporary file.", e);
        }
    }

    public int getNumberOfPages() {
        return document.getDocumentCatalog().getAllPages().size();
    }

    public PDDocument getUnderlyingPDDocument() {
        return document;
    }

    /**
     * Import an existing page to the underlying {@link PDDocument}
     * 
     * @param page
     * @throws TaskIOException
     */
    public void importPage(PDPage page) throws TaskIOException {
        PDPage imported;
        try {
            imported = document.importPage(page);
        } catch (IOException e) {
            throw new TaskIOException("An error occurred copying the page.", e);
        }
        imported.setCropBox(page.findCropBox());
        imported.setMediaBox(page.findMediaBox());
        imported.setResources(page.findResources());
        imported.setRotation(page.findRotation());
    }

    public PDPage getPage(int pageNumber) {
        return (PDPage) document.getDocumentCatalog().getAllPages().get(pageNumber - 1);
    }

}
