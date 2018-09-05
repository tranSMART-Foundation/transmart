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
 * Date: 08-May-2007
 * Time: 12:44:46
 *
 * @author Phil Jones, EMBL-EBI, pjones@ebi.ac.uk
 *
 * If this Exception is thrown, indicates that the problem is with the initial reading
 * of the configuration of the server and so it fatal.  Should be reported back ASAP to
 * the deployer of the servlet.
 */
public class ConfigurationException extends Exception{

    public ConfigurationException(String message){
        super(message);
    }

    public ConfigurationException(String message, Throwable cause){
        super (message, cause);
    }
}
