/*
 * Created on 03/lug/2010
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
package org.sejda.core.support.prefix.processor;

import static org.junit.Assert.assertEquals;
import static org.sejda.core.support.prefix.model.NameGenerationRequest.nameRequest;

import org.junit.Test;

/**
 * Test case for BasenamePrefixProcessor
 * 
 * @author Andrea Vacondio
 * 
 */
public class BasenamePrefixProcessorTest extends BasePrefixProcessorTest {

    private BasenamePrefixProcessor victim = new BasenamePrefixProcessor();

    @Test
    public void testComplexProcess() {
        String prefix = "prefix_[BASENAME]";
        String originalName = "name";
        String expected = "prefix_name";
        assertEquals(expected, victim.process(prefix, nameRequest().originalName(originalName)));
    }

    @Test
    public void testComplexProcessStripExtension() {
        String prefix = "prefix_[BASENAME]";
        String originalName = "name.pdf";
        String expected = "prefix_name";
        assertEquals(expected, victim.process(prefix, nameRequest().originalName(originalName)));
    }

    @Test
    public void testComplexProcessStripExtensionOneCharName() {
        String prefix = "prefix_[BASENAME]";
        String originalName = "x.pdf";
        String expected = "prefix_x";
        assertEquals(expected, victim.process(prefix, nameRequest().originalName(originalName)));
    }

    @Override
    PrefixProcessor getProcessor() {
        return victim;
    }
}
