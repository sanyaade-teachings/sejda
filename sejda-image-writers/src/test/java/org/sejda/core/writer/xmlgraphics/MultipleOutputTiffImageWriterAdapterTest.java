/*
 * Created on 25/set/2011
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
package org.sejda.core.writer.xmlgraphics;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;
import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.spy;

import java.awt.image.RenderedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.sejda.ImageTestUtils;
import org.sejda.core.writer.xmlgraphics.MultipleOutputTiffImageWriterAdapter.MultipleOutputTiffImageWriterAdapterBuilder;
import org.sejda.model.exception.TaskIOException;
import org.sejda.model.image.ImageColorType;
import org.sejda.model.parameter.image.PdfToMultipleTiffParameters;

/**
 * @author Andrea Vacondio
 * 
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest(MultipleOutputTiffImageWriterAdapter.class)
public class MultipleOutputTiffImageWriterAdapterTest {

    private MultipleOutputTiffImageWriterAdapter victim;

    @Before
    public void setUp() {
        victim = spy(new MultipleOutputTiffImageWriterAdapterBuilder().build());
    }

    @Test
    public void openStreamDestination() {
        OutputStream destination = mock(OutputStream.class);
        PdfToMultipleTiffParameters params = mock(PdfToMultipleTiffParameters.class);
        victim.openWriteDestination(destination, params);
        verify(victim).setOutputStream(destination);
    }

    @Test
    public void openFileDestination() throws TaskIOException, IOException {
        File destination = File.createTempFile("test", ".tmp");
        destination.deleteOnExit();
        PdfToMultipleTiffParameters params = mock(PdfToMultipleTiffParameters.class);
        victim.openWriteDestination(destination, params);
        verify(victim).setOutputStream(any(OutputStream.class));
    }

    @Test(expected = TaskIOException.class)
    public void writeNotOpened() throws TaskIOException {
        PdfToMultipleTiffParameters params = mock(PdfToMultipleTiffParameters.class);
        RenderedImage image = mock(RenderedImage.class);
        victim.write(image, params);
    }

    @Test
    public void supportMultiImage() {
        assertFalse(victim.supportMultiImage());
    }

    @Test
    public void write() throws IOException, TaskIOException {
        InputStream stream = getClass().getClassLoader().getResourceAsStream("image/test.tiff");
        File destination = File.createTempFile("test", ".tmp");
        destination.deleteOnExit();
        PdfToMultipleTiffParameters params = new PdfToMultipleTiffParameters(ImageColorType.GRAY_SCALE);
        RenderedImage image = ImageTestUtils.loadImage(stream, "test.tiff");
        victim.openWriteDestination(destination, params);
        victim.write(image, params);
        victim.closeDestination();
        victim.close();
        RenderedImage result = ImageTestUtils.loadImage(destination);
        assertTrue(result.getHeight() > 0);
        assertTrue(result.getWidth() > 0);
    }
}
