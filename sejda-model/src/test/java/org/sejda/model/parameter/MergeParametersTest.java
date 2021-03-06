/*
 * Created on 11/ago/2011
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
package org.sejda.model.parameter;

import static org.mockito.Mockito.mock;

import java.io.InputStream;

import org.junit.Before;
import org.junit.Test;
import org.sejda.TestUtils;
import org.sejda.model.input.PdfMergeInput;
import org.sejda.model.input.PdfSource;
import org.sejda.model.input.PdfStreamSource;
import org.sejda.model.output.SingleTaskOutput;
import org.sejda.model.pdf.page.PageRange;

/**
 * @author Andrea Vacondio
 * 
 */
public class MergeParametersTest {

    private SingleTaskOutput<?> output;

    @Before
    public void setUp() {
        output = mock(SingleTaskOutput.class);
    }

    @Test
    public void testEquals() {
        MergeParameters eq1 = new MergeParameters();
        MergeParameters eq2 = new MergeParameters();
        MergeParameters eq3 = new MergeParameters();
        MergeParameters diff = new MergeParameters(true, true);
        TestUtils.testEqualsAndHashCodes(eq1, eq2, eq3, diff);
    }

    @Test
    public void testInvalidParametersNullSource() {
        MergeParameters victim = new MergeParameters(false, true);

        victim.setOutput(output);
        victim.addInput(new PdfMergeInput(null));
        TestUtils.assertInvalidParameters(victim);
    }

    @Test
    public void testInvalidParametersInvalidRange() {
        MergeParameters victim = new MergeParameters();
        victim.setOutput(output);
        victim.setOutputName("name");
        InputStream stream = mock(InputStream.class);
        PdfSource<InputStream> input = PdfStreamSource.newInstanceNoPassword(stream, "name");
        PdfMergeInput mergeInput = new PdfMergeInput(input);
        PageRange range = new PageRange(3, 2);
        mergeInput.addPageRange(range);
        victim.addInput(mergeInput);
        TestUtils.assertInvalidParameters(victim);
    }

    @Test
    public void testInvalidParametersIntersectingRanges() {
        MergeParameters victim = new MergeParameters();
        victim.setOutput(output);
        InputStream stream = mock(InputStream.class);
        PdfSource<InputStream> input = PdfStreamSource.newInstanceNoPassword(stream, "name");
        PdfMergeInput mergeInput = new PdfMergeInput(input);
        PageRange range1 = new PageRange(1, 20);
        PageRange range2 = new PageRange(10, 30);
        mergeInput.addPageRange(range1);
        mergeInput.addPageRange(range2);
        victim.addInput(mergeInput);
        TestUtils.assertInvalidParameters(victim);
    }
}
