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
package org.sejda.model.parameter;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

import javax.validation.constraints.NotNull;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.sejda.model.parameter.base.MultiplePdfSourceMultipleOutputParameters;
import org.sejda.model.pdf.PdfVersion;
import org.sejda.model.pdf.encryption.PdfAccessPermission;
import org.sejda.model.pdf.encryption.PdfEncryption;

/**
 * Parameters for the encrypt manipulation. Accepts a list of {@link org.sejda.model.input.PdfSource} that will be encrypted using the same parameters.
 * 
 * @author Andrea Vacondio
 * 
 */
public class EncryptParameters extends MultiplePdfSourceMultipleOutputParameters {

    private String ownerPassword = "";
    private String userPassword = "";
    @NotNull
    private PdfEncryption encryptionAlgorithm = PdfEncryption.STANDARD_ENC_40;
    private final Set<PdfAccessPermission> permissions = EnumSet.noneOf(PdfAccessPermission.class);

    public EncryptParameters(PdfEncryption encryptionAlgorithm) {
        this.encryptionAlgorithm = encryptionAlgorithm;
    }

    public String getOwnerPassword() {
        return ownerPassword;
    }

    public void setOwnerPassword(String ownerPassword) {
        this.ownerPassword = ownerPassword;
    }

    public String getUserPassword() {
        return userPassword;
    }

    public void setUserPassword(String userPassword) {
        this.userPassword = userPassword;
    }

    public PdfEncryption getEncryptionAlgorithm() {
        return encryptionAlgorithm;
    }

    /**
     * @return an unmodifiable view of the permissions
     */
    public Set<PdfAccessPermission> getPermissions() {
        return Collections.unmodifiableSet(permissions);
    }

    /**
     * clear permissions
     */
    public void clearPermissions() {
        permissions.clear();
    }

    /**
     * adds a permission to the permissions set
     * 
     * @param permission
     */
    public void addPermission(PdfAccessPermission permission) {
        permissions.add(permission);
    }

    @Override
    public PdfVersion getMinRequiredPdfVersion() {
        return PdfVersion.getMax(super.getMinRequiredPdfVersion(), encryptionAlgorithm.getMinVersion());
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().appendSuper(super.hashCode()).append(userPassword).append(ownerPassword)
                .append(encryptionAlgorithm).append(permissions).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof EncryptParameters)) {
            return false;
        }
        EncryptParameters parameter = (EncryptParameters) other;
        return new EqualsBuilder().appendSuper(super.equals(other)).append(userPassword, parameter.getUserPassword())
                .append(ownerPassword, parameter.getOwnerPassword())
                .append(encryptionAlgorithm, parameter.getEncryptionAlgorithm())
                .append(permissions, parameter.getPermissions()).isEquals();
    }
}
