/*
 * Created on 17/set/2010
 *
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
package org.sejda.core.service;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.io.InputStream;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.sejda.TestUtils;
import org.sejda.core.context.DefaultSejdaContext;
import org.sejda.core.context.SejdaContext;
import org.sejda.model.exception.TaskException;
import org.sejda.model.input.PdfStreamSource;
import org.sejda.model.parameter.EncryptParameters;
import org.sejda.model.pdf.PdfVersion;
import org.sejda.model.pdf.encryption.PdfAccessPermission;
import org.sejda.model.pdf.encryption.PdfEncryption;
import org.sejda.model.task.Task;

import com.lowagie.text.pdf.PdfReader;
import com.lowagie.text.pdf.PdfWriter;

/**
 * Test unit for the encrypt task
 * 
 * @author Andrea Vacondio
 * 
 */
@Ignore
@SuppressWarnings("unchecked")
public abstract class EncryptTaskTest extends PdfOutEnabledTest implements TestableTask<EncryptParameters> {
    private DefaultTaskExecutionService victim = new DefaultTaskExecutionService();

    private SejdaContext context = mock(DefaultSejdaContext.class);
    private EncryptParameters parameters;

    @Before
    public void setUp() {
        setUpParameters();
        TestUtils.setProperty(victim, "context", context);
    }

    /**
     * Set up of the encrypt parameters
     * 
     */
    private void setUpParameters() {
        parameters = new EncryptParameters(PdfEncryption.AES_ENC_128);
        parameters.setCompress(true);
        parameters.setOutputPrefix("test_prefix_");
        parameters.setVersion(PdfVersion.VERSION_1_6);
        InputStream stream = getClass().getClassLoader().getResourceAsStream("pdf/test_file.pdf");
        PdfStreamSource source = PdfStreamSource.newInstanceWithPassword(stream, "test_file.pdf", "test");
        parameters.addSource(source);
        parameters.setOverwrite(true);
    }

    @Test
    public void testExecuteOwner() throws TaskException, IOException {
        parameters.setOwnerPassword("test");
        parameters.addPermission(PdfAccessPermission.COPY_AND_EXTRACT);
        when(context.getTask(parameters)).thenReturn((Task) getTask());
        initializeNewStreamOutput(parameters);
        victim.execute(parameters);
        PdfReader reader = getReaderFromResultStream("test_prefix_test_file.pdf", "test".getBytes());
        assertCreator(reader);
        assertVersion(reader, PdfVersion.VERSION_1_6);
        assertTrue(reader.isEncrypted());
        assertTrue((reader.getPermissions() & PdfWriter.ALLOW_COPY) == PdfWriter.ALLOW_COPY);
        assertFalse((reader.getPermissions() & PdfWriter.ALLOW_ASSEMBLY) == PdfWriter.ALLOW_ASSEMBLY);
        reader.close();
    }

    protected EncryptParameters getParameters() {
        return parameters;
    }

}
