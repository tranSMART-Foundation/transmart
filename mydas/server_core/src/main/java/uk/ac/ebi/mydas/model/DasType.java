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
 * Created Using IntelliJ IDEA.
 * Date: 17-May-2007
 * Time: 14:41:39
 *
 * @author Phil Jones, EMBL-EBI, pjones@ebi.ac.uk
 *
 * This class has two jobs:
 * Loading the types from the server configuration and holding details of the types
 * for a particular request.  Note that the equals method only compares the id.
 */
public class DasType {

    private String id;

    private String category;

    private String method;


    public DasType(String id, String category, String method){
        if (id == null || id.length() == 0){
            throw new IllegalArgumentException("id must not be null or an empty String");
        }
        this.id = id;
        this.category = category;
        this.method = method;
    }

    public String getId() {
        return id;
    }

    public String getCategory() {
        return category;
    }

    public String getMethod() {
        return method;
    }


    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DasType dasType = (DasType) o;

        if (category != null ? !category.equals(dasType.category) : dasType.category != null) return false;
        if (!id.equals(dasType.id)) return false;
        if (method != null ? !method.equals(dasType.method) : dasType.method != null) return false;

        return true;
    }

    public int hashCode() {
        int result;
        result = id.hashCode();
        result = 31 * result + (category != null ? category.hashCode() : 0);
        result = 31 * result + (method != null ? method.hashCode() : 0);
        return result;
    }

    public String toString(){
        StringBuffer buf = new StringBuffer("DasType.  id: '");
        buf .append (id)
            .append ("' category: '")
            .append (category)
            .append ("' method: '")
            .append (method)
            .append ("'");
        return buf.toString();
    }
}
