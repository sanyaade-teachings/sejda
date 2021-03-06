/*
 * Created on 29/mag/2010
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
package org.sejda.model.input;

import java.net.URL;

import javax.validation.constraints.NotNull;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.sejda.model.exception.TaskIOException;

/**
 * {@link AbstractPdfSource} from a {@link URL}
 * 
 * @author Andrea Vacondio
 * 
 */
public final class PdfURLSource extends AbstractPdfSource<URL> {

    @NotNull
    private final URL url;

    private PdfURLSource(URL url, String name, String password) {
        super(name, password);
        this.url = url;
    }

    public URL getSource() {
        return url;
    }

    public <T> T open(PdfSourceOpener<T> opener) throws TaskIOException {
        return opener.open(this);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).appendSuper(super.toString()).append(url).toString();
    }

    /**
     * Creates a new instance of the pdf source where a password is NOT required to open the source.
     * 
     * @param url
     *            input URL to a pdf document
     * @param name
     * @return a newly created instance
     */
    public static PdfURLSource newInstanceNoPassword(URL url, String name) {
        return PdfURLSource.newInstanceWithPassword(url, name, null);
    }

    /**
     * Creates a new instance of the pdf source where a password is required to open the source.
     * 
     * @param url
     *            input URL to a pdf document
     * @param name
     * @param password
     * @return a newly created instance
     * @throws IllegalArgumentException
     *             if the input stream or the input name are blank.
     */
    public static PdfURLSource newInstanceWithPassword(URL url, String name, String password) {
        if (url == null) {
            throw new IllegalArgumentException("A not null url instance and a not blank name are expected.");
        }
        return new PdfURLSource(url, name, password);
    }
}
