/*
 * Created on 18/ago/2011
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.sejda.model.input.PdfSource;
import org.sejda.model.output.MultipleTaskOutput;
import org.sejda.model.parameter.base.AbstractParameters;
import org.sejda.model.parameter.base.MultiplePdfSourceTaskParameters;
import org.sejda.model.validation.constraint.NotEmpty;

/**
 * Parameter class for the unpack manipulation. Accepts a list of {@link PdfSource} that will be unpacked.
 * 
 * @author Andrea Vacondio
 * 
 */
public class UnpackParameters extends AbstractParameters implements MultiplePdfSourceTaskParameters {

    @Valid
    @NotNull
    private final MultipleTaskOutput<?> output;
    @NotEmpty
    @Valid
    private final List<PdfSource<?>> sourceList = new ArrayList<PdfSource<?>>();

    public UnpackParameters(MultipleTaskOutput<?> output) {
        this.output = output;
    }

    public MultipleTaskOutput<?> getOutput() {
        return output;
    }

    /**
     * adds the input source to the source list.
     * 
     * @param input
     */
    public void addSource(PdfSource<?> input) {
        sourceList.add(input);
    }

    /**
     * @return an unmodifiable view of the source list
     */
    public List<PdfSource<?>> getSourceList() {
        return Collections.unmodifiableList(sourceList);
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().appendSuper(super.hashCode()).append(output).append(sourceList).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof UnpackParameters)) {
            return false;
        }
        UnpackParameters parameter = (UnpackParameters) other;
        return new EqualsBuilder().appendSuper(super.equals(other)).append(output, parameter.getOutput())
                .append(sourceList, parameter.getSourceList()).isEquals();
    }
}
