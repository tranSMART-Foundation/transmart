/*************************************************************************
 * tranSMART - translational medicine data mart
 * 
 * Copyright 2008-2012 Janssen Research & Development, LLC.
 * 
 * This product includes software developed at Janssen Research & Development, LLC.
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version, along with the following terms:
 *
 * 1.	You may convey a work based on this program in accordance with section 5,
 *      provided that you retain the above notices.
 * 2.	You may convey verbatim copies of this program code as you receive it,
 *      in any medium, provided that you retain the above notices.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 ******************************************************************/
  

package org.transmartproject.pipeline.i2b2

import groovy.sql.Sql;
import groovy.util.logging.Slf4j

import org.transmartproject.pipeline.util.Util

@Slf4j('logger')
class TmConceptCounts {

    Sql i2b2metadata
    String basePath, platform
    Map subjects

    void loadConceptCounts(){

	Map sampleCounts = [:]
	int total = 0
	subjects.each{key, val ->
	    total++
	    if(sampleCounts[val].equals(null)) sampleCounts[val] = 1
	    else sampleCounts[val] += 1
	}

	logger.info "Total samples: $total"
	logger.info basePath + "\t" + platform

	String [] str = basePath.split("/")
	String parentPath = basePath.replace(str[-1] + "/", "")
	logger.info parentPath
	insertConceptCount(basePath, parentPath, total)
	insertConceptCount(basePath + platform + "/", basePath, total)

	sampleCounts.each{k, v ->
	    if(!k.equals(null) && k.toString().size() > 0){
		String parentConceptPath = basePath + platform + "/"
		String conceptPath = basePath + platform + "/" + k + "/"
		insertConceptCount(conceptPath, parentConceptPath, v)
	    }
	}
    }

    void insertConceptCount(String conceptPath, String parentConceptPath, int totalCnt ){

	String qry = "insert into tm_concept_counts(CONCEPT_PATH, PARENT_CONCEPT_PATH, PATIENT_COUNT) values(?, ?, ?)"

	String path = conceptPath.replace("/", "\\")
	String parentPath = parentConceptPath.replace("/", "\\")

	if(isConceptCountsExist(path)){
	    logger.info "$conceptPath already exists in TM_CONCEPT_COUNTS ..."
	}else{
	    logger.info "Insert ($path, $totalCnt) into TM_CONCEPT_COUNTS ..."
	    i2b2metadata.execute(qry, [
		path,
		parentPath,
		totalCnt
	    ])
	}
    }

    boolean isConceptCountsExist(String conceptPath){
	String qry = "select count(*) from tm_concept_counts where concept_path=?"
	def res = i2b2metadata.firstRow(qry, [conceptPath])
	if(res[0] > 0) return true
	else return false
    }

    void setBasePath(String basePath){
	this.basePath = basePath
    }

    void setSubjects(Map subjects){
	this.subjects = subjects
    }

    void setPlatform(String platform){
	this.platform = platform
    }

    void setI2b2metadata(Sql i2b2metadata){
	this.i2b2metadata = i2b2metadata
    }
}
