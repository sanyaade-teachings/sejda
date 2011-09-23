/*
 * Created on 23/gen/2011
 * Copyright 2010 by Andrea Vacondio (andrea.vacondio@gmail.com).
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
package org.sejda.impl.itext;

import static org.sejda.core.support.io.model.FileOutput.file;
import static org.sejda.core.support.util.ComponentsUtility.nullSafeCloseQuietly;
import static org.sejda.impl.itext.util.ITextUtils.nullSafeClosePdfReader;
import static org.sejda.impl.itext.util.PageLabelUtils.getLabels;

import java.io.File;

import org.sejda.core.exception.TaskException;
import org.sejda.core.manipulation.model.input.PdfSource;
import org.sejda.core.manipulation.model.input.PdfSourceOpener;
import org.sejda.core.manipulation.model.parameter.SetPagesLabelParameters;
import org.sejda.core.manipulation.model.task.Task;
import org.sejda.core.support.io.SingleOutputWriterSupport;
import org.sejda.impl.itext.component.DefaultPdfCopier;
import org.sejda.impl.itext.component.input.PdfSourceOpeners;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.lowagie.text.pdf.PdfReader;

/**
 * Task that apply page labels to a input pdf.
 * 
 * @author Andrea Vacondio
 * 
 */
public class SetPagesLabelTask implements Task<SetPagesLabelParameters> {

    private static final Logger LOG = LoggerFactory.getLogger(SetPagesLabelTask.class);

    private PdfReader reader = null;
    private DefaultPdfCopier copier = null;
    private SingleOutputWriterSupport outputWriter;
    private PdfSourceOpener<PdfReader> sourceOpener;

    public void before(SetPagesLabelParameters parameters) {
        outputWriter = new SingleOutputWriterSupport();
        sourceOpener = PdfSourceOpeners.newPartialReadOpener();
    }

    public void execute(SetPagesLabelParameters parameters) throws TaskException {
        PdfSource source = parameters.getSource();
        LOG.debug("Opening {} ", source);
        reader = source.open(sourceOpener);

        File tmpFile = outputWriter.createTemporaryPdfBuffer();
        LOG.debug("Created output temporary buffer {} ", tmpFile);

        copier = new DefaultPdfCopier(reader, tmpFile, parameters.getVersion());
        copier.setCompression(parameters.isCompressXref());

        copier.addAllPages(reader);

        nullSafeClosePdfReader(reader);

        LOG.debug("Applying {} labels ", parameters.getLabels().size());
        copier.setPageLabels(getLabels(parameters.getLabels(), reader.getNumberOfPages()));

        nullSafeCloseQuietly(copier);

        outputWriter.flushSingleOutput(file(tmpFile).name(parameters.getOutputName()), parameters.getOutput(),
                parameters.isOverwrite());
        LOG.debug("Labels applied to {}", parameters.getOutput());
    }

    public void after() {
        nullSafeClosePdfReader(reader);
        nullSafeCloseQuietly(copier);
    }

}