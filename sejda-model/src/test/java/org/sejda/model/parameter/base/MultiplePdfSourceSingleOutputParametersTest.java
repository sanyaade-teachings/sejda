/*
 * Created on 30/ott/2011
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
package org.sejda.model.parameter.base;

import org.junit.Test;
import org.sejda.TestUtils;

/**
 * @author Andrea Vacondio
 * 
 */
public class MultiplePdfSourceSingleOutputParametersTest {

    @Test
    public void testEquals() {
        MockMultiplePdfSourceSingleOutputParameters eq1 = new MockMultiplePdfSourceSingleOutputParameters();
        MockMultiplePdfSourceSingleOutputParameters eq2 = new MockMultiplePdfSourceSingleOutputParameters();
        MockMultiplePdfSourceSingleOutputParameters eq3 = new MockMultiplePdfSourceSingleOutputParameters();
        MockMultiplePdfSourceSingleOutputParameters diff = new MockMultiplePdfSourceSingleOutputParameters();
        diff.setOutputName("ChuckNorris");
        TestUtils.testEqualsAndHashCodes(eq1, eq2, eq3, diff);
    }

    private class MockMultiplePdfSourceSingleOutputParameters extends MultiplePdfSourceSingleOutputParameters {
        // nothing
    }
}
