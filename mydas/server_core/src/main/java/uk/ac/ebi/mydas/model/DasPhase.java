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

/**
 * Created using IntelliJ IDEA.
 * Date: 26-May-2007
 * Time: 15:40:09
 *
 * @author Phil Jones, EMBL-EBI, pjones@ebi.ac.uk
 */
public enum DasPhase {
    PHASE_READING_FRAME_0("0"),
    PHASE_READING_FRAME_1 ("1"),
    PHASE_READING_FRAME_2 ("2"),
    PHASE_NOT_APPLICABLE ("-");

    private String representation;

    private DasPhase (String representation){
        this.representation = representation;
    }

    public String toString (){
        return representation;
    }
}
