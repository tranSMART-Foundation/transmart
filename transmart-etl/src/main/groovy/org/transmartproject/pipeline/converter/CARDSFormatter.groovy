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

import groovy.sql.Sql
import groovy.util.logging.Slf4j
import java.util.Properties;

import org.transmartproject.pipeline.util.Util

@Slf4j('logger')
class CARDSFormatter {

	private static Properties props
	private int batchSize

	static main(args) {

//		PropertyConfigurator.configure("conf/log4j.properties");

		CARDSFormatter cards = new CARDSFormatter()

		if(args.size() > 0){
			logger.info("Start loading property files conf/Common.properties and ${args[0]} ...")
			cards.setProperties(Util.loadConfiguration(args[0]));
		} else {
			logger.info("Start loading property files conf/Common.properties and conf/CARDS.properties ...")
			cards.setProperties(Util.loadConfiguration("conf/CARDS.properties"));
		}

		Sql deapp = Util.createSqlFromPropertyFile(props, "deapp")
		Sql i2b2demodata = Util.createSqlFromPropertyFile(props, "i2b2demodata")

		cards.setBatchSize(Integer.parseInt(props.get("batch_size")))

		Map snpMap = cards.loadSnpList()
		Util.printMap(snpMap)

		cards.formatGenotype(snpMap)

		Map subjectPatientMap = cards.getSubjectPatientMap()
		Util.printMap(subjectPatientMap)

		cards.formatLgen(subjectPatientMap)

		//cards.createFam(i2b2demodata)
		//cards.createMap(i2b2demodata)
	}


	private void createMap(Sql sql){

		if(props.get("skip_create_map").toString().toLowerCase().equals("yes")){
			logger.info "Skip creating CARDS MAP file ..."
		}else{

			File map = new File(props.get("output_cards_map"))
			if(map.size() > 0){
				map.delete()
				map.createNewFile()
			}

			logger.info("Start creating CARDS MAP file: " + map.toString())

			StringBuffer sb = new StringBuffer()

			String qry = """ select t1.chrom, t1.name, t1.chrom_pos
						 from deapp.de_snp_info t1, tm_lz.pfizer_snp t2
						 where t1.name='rs'||t2.dbsnp_rsid """

			sql.eachRow(qry) {
				sb.append(it.chrom + "\t" + it.name + "\t0\t" + it.chrom_pos + "\n" )
			}

			map.append(sb.toString())
			sb.setLength(0)

			logger.info("End creating CARDS MAP file: " + map.toString())
		}
	}


	private void createFam(Sql sql){

		if(props.get("skip_create_fam").toString().toLowerCase().equals("yes")){
			logger.info "Skip creating CARDS FAM file ..."
		}else{
			File fam = new File(props.get("output_cards_fam"))
			if(fam.size() > 0){
				fam.delete()
				fam.createNewFile()
			}

			logger.info("Start creating CARDS FAM file: " + fam.toString())

			StringBuffer sb = new StringBuffer()

			String qry = """ select patient_num, decode(lower(sex_cd), 'male', '1', 'female', '2', '9') as gender
						 from i2b2demodata.patient_dimension p, deapp.de_subject_sample_mapping s
						 where s.platform='SNP' and s.trial_name='CARDS' and p.patient_num=s.patient_id """

			sql.eachRow(qry) {
				println it.patient_num + "\t" + it.gender
				if(!it.patient_num.equals(null)) sb.append(it.patient_num + "\t" + it.patient_num + "\t0\t0\t" + it.gender + "\t9\n" )
			}

			sb.setLength(0)

			logger.info("End creating CARDS FAM file: " + fam.toString())
		}
	}


	/**
	 *   create subject-patient_num mapping using the mapping file
	 *   
	 * @return
	 */
	private Map getSubjectPatientMap() {

		Map subjectPatientMap = [:]
		String [] str

		File map = new File(props.get("cards_subject_mapping"))

		if(map.size() > 0){
			map.eachLine {
				str = it.split(",")
				subjectPatientMap[str[1].trim()] = str[0].trim()
			}
		} else{
			logger.error("CARDS subject-patient mapping file: " + map.toString() + " doesn't exist or is empty ...")
		}
		return subjectPatientMap
	}



	private void formatLgen(Map subjectPatientMap){

		if(props.get("skip_format_lgen").toString().toLowerCase().equals("yes")){
			logger.info "Skip formtting CARDS LGEN file ..."
		}else{

			String [] str
			StringBuffer sb = new StringBuffer()

			File lgenOutput = new File(props.get("output_lgen_data"))
			if(lgenOutput.size() > 0){
				lgenOutput.delete()
				lgenOutput.createNewFile()
			}

			logger.info("Start formatting CARDS LGEN file: " + lgenOutput.toString())

			File genotypeInput = new File(props.get("output_genotype_data"))
			if(genotypeInput.size()){
				genotypeInput.eachLine {
					str = it.split("\t")
					sb.append(subjectPatientMap[str[0]] + "\t" + subjectPatientMap[str[0]] + "\t" + str[1] + "\t" + str[2] + "\n")
				}

				lgenOutput.append(sb.toString())
				sb.setLength(0)

				logger.info("End formatting CARDS LGEN file: " + lgenOutput.toString())
			} else{
				logger.error("CARDS genotype data file: " + genotypeInput.toString() + " doesn't exist or is empty ...")
			}
		}
	}


	private void formatGenotype(Map snpMap){

		if(props.get("skip_format_genotype").toString().toLowerCase().equals("yes")){
			logger.info "Skip formtting CARDS genotype data ..."
		}else{
			String [] str, subjects
			String genotype
			File genotypeInput = new File(props.get("cards_genotype_data"))

			StringBuffer sb = new StringBuffer()
			File genotypeOutput = new File(props.get("output_genotype_data"))
			if(genotypeOutput.size() > 0){
				genotypeOutput.delete()
				genotypeOutput.createNewFile()
			}

			logger.info("Start creating CARDS genotype data file: " + genotypeOutput.toString())

			if(genotypeInput.size() > 0){
				genotypeInput.eachLine{
					str = it.split("\t")
					if(it.indexOf("SNP_ID") >= 0){
						subjects = str
					} else{
						for(int i in 1..str.size()-1) {
						//for(int i in 1..10) {
							genotype = str[i].substring(0, 1) + " " + str[i].substring(1)
							sb.append(subjects[i] + "\t" + snpMap[str[0]] + "\t" + genotype +  "\n"  )
						}
					}
				}

				println sb.toString()
				genotypeOutput.append(sb.toString())
				sb.setLength(0)

				logger.info("End creating CARDS genotype data file: " + genotypeOutput.toString())

			} else {
				logger.error("CARDS genotype file: " + genotypeInput.toString() + " doesn't exist or empty ...")
			}
		}
	}

	private Map loadSnpList(){

		Map snpIdMap = [:]
		String [] str

		File snp = new File(props.get("cards_snp_list"))

		if(snp.size()>0){
			snp.each {
				if(it.indexOf("SNP_ID") == -1){
					str = it.split(" +")
					snpIdMap[str[0]] = "rs" + str[1]
				}
			}
		} else {
			logger.error("CARDS SNP list file: " + snp.toString() + " doesn't exist or empty ...")
		}

		return snpIdMap
	}


	void setBatchSize(int batchSize){
		this.batchSize = batchSize
	}


	void setProperties(Properties props){
		this.props = props
	}
}

