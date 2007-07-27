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

package uk.ac.ebi.mydas.model;

import java.io.Serializable;

/**
 * Created using IntelliJ IDEA.
 * Date: 26-May-2007
 * Time: 15:32:43
 *
 * @author Phil Jones, EMBL-EBI, pjones@ebi.ac.uk
 * The DasEntryPointOrientation enum is used to represent the
 * orientation of the entry point as reported in the
 * /DASEP/ENTRY_POINTS/SEGMENT/@orientation attribute.
 */
    public enum DasEntryPointOrientation implements Serializable {
        /**
         * Object used to define a positive orientation of a sequenceString.
         */
        POSITIVE_ORIENTATION ("+"),
        /**
         * Object used to define a negative orientation of a sequenceString.
         */
        NEGATIVE_ORIENTATION ("_"),
        /**
         * Object used to define that the sequenceString has no intrinsic orientation.
         * This is the default value of the DasEntryPoint.
         */
        NO_INTRINSIC_ORIENTATION ("+");

        private final String displayString;

        DasEntryPointOrientation(String displayString){
            this.displayString = displayString;
        }

        public String toString (){
            return displayString;
        }
    }

