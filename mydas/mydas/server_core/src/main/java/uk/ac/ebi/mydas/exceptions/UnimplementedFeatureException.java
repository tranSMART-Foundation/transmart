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

package uk.ac.ebi.mydas.exceptions;

/**
 * Created Using IntelliJ IDEA.
 * Date: 16-May-2007
 * Time: 14:50:49
 *
 * @author Phil Jones, EMBL-EBI, pjones@ebi.ac.uk
 *
 * This class can be thrown by a DataSource to indicate that it does not implement a particular piece of
 * functionality, so that the MyDas servlet can return a suitable HTTP header error code to the
 * DAS client. (i.e. X-DAS-Status: 501 UnimplementedFeature
 */
public class UnimplementedFeatureException extends Exception{

    public UnimplementedFeatureException(String message){
        super (message);
    }
}
