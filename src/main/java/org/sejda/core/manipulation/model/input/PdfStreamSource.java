/*
 * Created on 29/mag/2010
 * Copyright (C) 2010 by Andrea Vacondio (andrea.vacondio@gmail.com).
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */
package org.sejda.core.manipulation.model.input;

import java.io.InputStream;

/**
 * {@link PdfSource} from a {@link InputStream}
 * 
 * @author Andrea Vacondio
 * 
 */
public class PdfStreamSource extends PdfSource {

    private static final long serialVersionUID = 2581826909753391287L;

    private InputStream stream;

    public PdfStreamSource(InputStream stream, String name) {
        this(stream, name, null);
    }

    public PdfStreamSource(InputStream stream, String name, String password) {
        super(name, password);
        this.stream = stream;
    }

    public InputStream getStream() {
        return stream;
    }

    public void setStream(InputStream stream) {
        this.stream = stream;
    }

    @Override
    public PdfSourceType getSourceType() {
        return PdfSourceType.STREAM_SOURCE;
    }
}
