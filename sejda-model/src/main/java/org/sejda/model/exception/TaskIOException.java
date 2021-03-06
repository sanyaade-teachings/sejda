/*
 * Created on 31/mag/2010
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
package org.sejda.model.exception;

/**
 * Exception thrown if an IOException is raised during the task execution
 * 
 * @author Andrea Vacondio
 * 
 */
public class TaskIOException extends TaskException {

    private static final long serialVersionUID = 5205172452237197631L;

    public TaskIOException(String message, Throwable cause) {
        super(message, cause);
    }

    public TaskIOException(String message) {
        super(message);
    }

    public TaskIOException(Throwable cause) {
        super(cause);
    }

}
