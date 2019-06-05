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
  

package org.transmartproject.pipeline.converter

import groovy.util.logging.Slf4j

@Slf4j('logger')
class PlinkConverter {

    // full path of PLINK program 
    String plink
    String plinkSourceDirectory, plinkDestinationDirectory, studyName
	
    void createBinaryFromLongPlink(){
		
	File lgen = getLgenFile()
	String plinkSourceFile = plinkSourceDirectory + "/" + studyName
	String outputFile = plinkDestinationDirectory + "/" + studyName

	String fam = outputFile + ".fam"
	if(!isFamExist(fam)) {
	    logger.info "Cannot find PLINK FAM file " + fam
	    throw new RuntimeException("Cannot find PLINK FAM file " + fam)
	}

	String map = outputFile + ".map"
	if(!isFamExist(map)) {
	    logger.info "Cannot find PLINK MAP file " + map
	    throw new RuntimeException("Cannot find PLINK MAP file " + map)
	}

	logger.info "Read Long-formt PLINK files from " + plinkSourceFile
	logger.info "Write Binary PLINK files to " + plinkDestinationDirectory 
	StringBuffer sb = new StringBuffer()
	sb.append(plink + " --lfile " + plinkSourceFile)
	sb.append(" --noweb --make-bed ")
	sb.append(" --out " + outputFile)

	String command = sb.toString()
	logger.info "Run: " + command
	Process proc = command.execute()
	logger.info(proc.getText())
    }

    void recodePlinkFileByChrs() {
	for(int chr in 1..24){
	    recodePlinkFileByChr(chr.toString())
	}
    }

    void recodePlinkFileByChr(String chr) {
	logger.info "Read Binary PLINK files from: " + plinkDestinationDirectory
	logger.info "Generate PLINK files for chromosome: " + chr

	StringBuffer sb = new StringBuffer()
	sb.append(plink + " --bfile " + plinkDestinationDirectory + "/" + studyName)
	sb.append(" --noweb --recode --chr " + chr)
	sb.append(" --out " + plinkDestinationDirectory + "/chr" + chr)

	String command = sb.toString()
	logger.info "Run: " + command
	Process proc = command.execute()
	logger.info(proc.getText())
    }		

    void recodePlinkFile() {
	logger.info "Read Binary PLINK files from: " + plinkDestinationDirectory
	logger.info "Recode PLINK files for all chromosomes... "

	StringBuffer sb = new StringBuffer()
	sb.append(plink + " --bfile " + plinkDestinationDirectory + "/" + studyName)
	sb.append(" --noweb --recode ")
	sb.append(" --out " + plinkDestinationDirectory + "/all")

	String command = sb.toString()
	logger.info "Run: " + command
	Process proc = command.execute()
	logger.info(proc.getText())
    }

    boolean isFamExist(String fam){
	File famFile = new File(fam)
	return famFile.exists()
    }

    boolean isMapExist(String map){
	File mapFile = new File(map)
	return mapFile.exists()
    }

    File getLgenFile(){
	File lgen = new File(plinkDestinationDirectory + "/" + studyName + ".lgen")

	if(lgen.exists()){
	    logger.info "Start transforming PLINK Long format to Binart format ..."
	} else{
	    logger.error "PLINK Long format file: " + lgen.toString() + " doesn't exist."
	    throw new RuntimeException("PLINK Long format file: " + lgen.toString() + " doesn't exist.")
	}

	return lgen
    }

    /**
     * 
     * @param plinkSourceDirectory		location to find .lgen, .map and .fam files
     */
    void setPlinkSourceDirectory(String plinkSourceDirectory){
	this.plinkSourceDirectory = plinkSourceDirectory
    }

    /**
     * 
     * @param plinkDestinationDirectory		location to store generated Binary PLINK files
     */
    void setPlinkDestinationDirectory(String plinkDestinationDirectory){
	this.plinkDestinationDirectory = plinkDestinationDirectory
    }

    void setStudyName(String studyName){
	this.studyName = studyName
    }

    /**
     * 
     * @param plink		full path for PLINK program
     */
	void setPlink(String plink){
	this.plink = plink
    }
}
