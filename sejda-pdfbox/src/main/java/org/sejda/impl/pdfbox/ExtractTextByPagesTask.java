/*
 * Created on 10/25/13
 * Copyright 2013 by Edi Weissmann (edi.weissmann@gmail.com).
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
package org.sejda.impl.pdfbox;

import static org.sejda.common.ComponentsUtility.nullSafeCloseQuietly;
import static org.sejda.core.notification.dsl.ApplicationEventsNotifier.notifyEvent;
import static org.sejda.core.support.io.IOUtils.createTemporaryBuffer;
import static org.sejda.core.support.io.model.FileOutput.file;
import static org.sejda.core.support.prefix.NameGenerator.nameGenerator;
import static org.sejda.core.support.prefix.model.NameGenerationRequest.nameRequest;

import java.io.File;
import java.util.Set;

import org.sejda.core.support.io.MultipleOutputWriter;
import org.sejda.core.support.io.OutputWriters;
import org.sejda.impl.pdfbox.component.DefaultPdfSourceOpener;
import org.sejda.impl.pdfbox.component.PDDocumentHandler;
import org.sejda.impl.pdfbox.component.PdfTextExtractor;
import org.sejda.model.SejdaFileExtensions;
import org.sejda.model.exception.TaskException;
import org.sejda.model.input.PdfSource;
import org.sejda.model.input.PdfSourceOpener;
import org.sejda.model.parameter.ExtractTextByPagesParameters;
import org.sejda.model.pdf.encryption.PdfAccessPermission;
import org.sejda.model.task.BaseTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Extract text from pdf input, splitting input file by pages. Example: input.pdf -> page1-4.txt, page5-10.txt, page11.txt Implemented using PdfBox
 * 
 * @author Eduard Weissmann
 */
public class ExtractTextByPagesTask extends BaseTask<ExtractTextByPagesParameters> {

    private static final Logger LOG = LoggerFactory.getLogger(ExtractTextByPagesTask.class);

    private PDDocumentHandler documentHandler = null;
    private MultipleOutputWriter outputWriter;
    private PdfSourceOpener<PDDocumentHandler> documentLoader;

    public void before(ExtractTextByPagesParameters parameters) {
        documentLoader = new DefaultPdfSourceOpener();
        outputWriter = OutputWriters.newMultipleOutputWriter(parameters.isOverwrite());
    }

    public void execute(ExtractTextByPagesParameters parameters) throws TaskException {
        PdfSource<?> source = parameters.getSource();
        LOG.debug("Opening {}", source);
        documentHandler = source.open(documentLoader);
        documentHandler.getPermissions().ensurePermission(PdfAccessPermission.COPY_AND_EXTRACT);

        Set<Integer> pages = parameters.getPages(documentHandler.getNumberOfPages());

        int currentStep = 0;
        int totalSteps = pages.size();

        for (Integer current : pages) {
            currentStep++;
            LOG.debug("Extracting text from page {}", current);

            File tmpFile = createTemporaryBuffer();
            LOG.debug("Created output on temporary buffer {}", tmpFile);

            PdfTextExtractor textExtractor = new PdfTextExtractor(parameters.getTextEncoding(), current, current);
            textExtractor.extract(documentHandler.getUnderlyingPDDocument(), tmpFile);
            String outName = nameGenerator(parameters.getOutputPrefix()).generate(
                    nameRequest(SejdaFileExtensions.TXT_EXTENSION).page(current)
                            .originalName(parameters.getSource().getName()).fileNumber(currentStep));
            outputWriter.addOutput(file(tmpFile).name(outName));

            // close resource
            nullSafeCloseQuietly(textExtractor);
            notifyEvent(getNotifiableTaskMetadata()).stepsCompleted(currentStep).outOf(totalSteps);
        }

        parameters.getOutput().accept(outputWriter);
        LOG.debug("Text extracted from input documents and written to {}", parameters.getOutput());

    }

    public void after() {
        nullSafeCloseQuietly(documentHandler);
    }

}
