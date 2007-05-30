/*
 * Copyright 2007 Philip Jones, EMBL-European Bioinformatics Institute
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
 *
 *
 * For further details of the mydas project, including source code,
 * downloads and documentation, please see:
 *
 * http://code.google.com/p/mydas/
 *
 */

package uk.ac.ebi.mydas.controller;

/**
 * Created using IntelliJ IDEA.
 * Date: 29-May-2007
 * Time: 20:20:59
 *
 * @author Phil Jones, EMBL-EBI, pjones@ebi.ac.uk
 *
 * Valid X-DAS-STATUS codes.
 */
public enum XDasStatus {
    
    STATUS_200_OK ("200"),
    STATUS_400_BAD_COMMAND ("400"),
    STATUS_401_BAD_DATA_SOURCE ("401"),
    STATUS_402_BAD_COMMAND_ARGUMENTS ("402"),
    STATUS_403_BAD_REFERENCE_OBJECT ("403"),
    STATUS_404_BAD_STYLESHEET ("404"),
    STATUS_405_COORDINATE_ERROR ("405"),
    STATUS_500_SERVER_ERROR ("500"),
    STATUS_501_UNIMPLEMENTED_FEATURE ("501");

    private final String errorCode;

    private XDasStatus(String errorCode){
        this.errorCode = errorCode;
    }

    public String toString(){
        return errorCode;
    }
}
