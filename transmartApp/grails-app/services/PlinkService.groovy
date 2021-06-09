import groovy.sql.Sql
import org.springframework.beans.factory.annotation.Autowired

import javax.sql.DataSource
import java.sql.Clob

import static org.transmart.authorization.QueriesResourceAuthorizationDecorator.checkQueryResultAccess

class PlinkService {

    static transactional = false

    @Autowired private DataSource dataSource

    /**
     *  extract the selected corhort through patient_num
     */
    String[] getStudyInfoBySubject(String subjectIds) {

	String query = '''
			select platform_name, trial_name
			from DEAPP.DE_SUBJECT_SNP_DATASET
			where rownum=1
			and platform_name is not null
			and patient_num in (''' + subjectIds + ')'

	def row = new Sql(dataSource).firstRow(query)

	[row.platform_name, row.trial_name]
    }

    /**
     * Retrieve the Platform_Name and Trial_Name based on the Result_Instance_Id
     */
    String[] getStudyInfoByResultInstanceId(String resultInstanceId) {
        checkQueryResultAccess resultInstanceId

	String query = '''
			select a.platform_name, a.trial_name 
			from DEAPP.DE_SUBJECT_SNP_DATASET a
			INNER JOIN DEAPP.de_subject_sample_mapping c on c.omic_patient_id=a.patient_num
			INNER JOIN (SELECT DISTINCT patient_num
		            FROM I2B2DEMODATA.qtm_patient_set_collection
		            WHERE result_instance_id = ?
		              AND patient_num IN
		                 (SELECT patient_num
		                  FROM I2B2DEMODATA.patient_dimension
		                  WHERE sourcesystem_cd NOT LIKE '%:S:%')
			) b on c.patient_id=b.patient_num
				where a.platform_name is not null
		'''

	def row = new Sql(dataSource).firstRow(query, [resultInstanceId])

	[row.platform_name, row.trial_name]
    }

    /**
     *  Create a *.map file for PLINK
     */
    void getMapDataByChromosome(String subjectIds, String chr, File plinkMapFile) {

        // 0 -- Platform Name   1 -- Trial Name
	String platform = getStudyInfoBySubject(subjectIds)[0]

        String chroms
        if (chr.contains(',')) {
	    chroms = chr.replace(',', "','")
        }
        else {
            chroms = chr
        }

	String query = '''
			SELECT probe_def
			FROM DEAPP.de_snp_probe_sorted_def
			WHERE chrom in (?)
			  and platform_name=?'''

	new Sql(dataSource).eachRow(query, [chroms, platform]) { row ->
	    if (row.probe_def != null) {
		Clob clob = (Clob) row.probe_def
                // change probe_def format from 'SNP  chr  position' to 'chr  SNP position'
		clob.asciiStream.text.eachLine { String line ->
		    String[] items = line.split()
                    plinkMapFile.append(items[1] + '\t' + items[0] + '\t' + items[2] + '\n')
                }
            }
        }
    }

    /**
     *   Create a *.ped file for PLINK
     */
    void getSnpDataBySujectChromosome(String subjectIds, String chr, File plinkPedFile) {

        // 0 -- Platform Name   1 -- Trial Name
	String trialName = getStudyInfoBySubject(subjectIds)[1]

        String chroms
        if (chr.contains(',')) {
	    chroms = chr.replace(',', "','")
        }
        else {
            chroms = chr
        }

	String query = '''SELECT t1.PATIENT_NUM,
	                  case t2.PATIENT_GENDER 
	                      when 'M' then 1
	                      when 'F' then 2
	                      else 0
	                  end as PATIENT_GENDER,
	                  t1.PED_BY_PATIENT_CHR 
	           FROM DEAPP.DE_SNP_DATA_BY_PATIENT t1,
	                (select distinct PATIENT_NUM, TRIAL_NAME, PATIENT_GENDER, SUBJECT_SNP_DATASET_ID
		                 from DEAPP.DE_SUBJECT_SNP_DATASET) t2
	           WHERE t1.PATIENT_NUM=t2.PATIENT_NUM and t1.TRIAL_NAME=t2.TRIAL_NAME and 
	           		 t1.PED_BY_PATIENT_CHR is not null and 
	           		 t2.SUBJECT_SNP_DATASET_ID=t1.SNP_DATASET_ID and 
	                 t1.chrom in (?) and t1.trial_name=?'''

	new Sql(dataSource).eachRow(query, [chroms, trialName]) { row ->
	    if (row.PED_BY_PATIENT_CHR != null) {
		Clob clob = (Clob) row.PED_BY_PATIENT_CHR
		plinkPedFile.append "$row.PATIENT_NUM $row.PATIENT_NUM 0 0 $row.PATIENT_GENDER 0  $clob.asciiStream.text\n"
            }
        }
    }

    /**
     *   Create a *.ped file for PLINK
     */
    void getSnpDataBySujectChromosome(String subjectIds, String chr, File plinkPedFile,
                                      List<String> conceptCodeList, String isAffected) {

        // 0 -- Platform Name   1 -- Trial Name
	String trialName = getStudyInfoBySubject(subjectIds)[1]

        String chroms
        if (chr.contains(',')) {
	    chroms = chr.replace(',', "','")
        }
        else {
            chroms = chr
        }

        String conceptCd = ''
	if (conceptCodeList) {
            for (item in 0..conceptCodeList.size() - 2) {
                conceptCd += "'" + conceptCodeList[item] + "',"
            }
            conceptCd += "'" + conceptCodeList[conceptCodeList.size() - 1] + "'"
        }

	String query = '''SELECT t1.PATIENT_NUM,
				 case t2.PATIENT_GENDER
					 when 'M' then 1
					 when 'F' then 2
					 else 0
				 end as PATIENT_GENDER,
				 t1.PED_BY_PATIENT_CHR
		  FROM DEAPP.DE_SNP_DATA_BY_PATIENT t1,
			   (select distinct PATIENT_NUM, TRIAL_NAME, PATIENT_GENDER, SUBJECT_SNP_DATASET_ID
				from DEAPP.DE_SUBJECT_SNP_DATASET
				where concept_cd in (''' + conceptCd + ''') and patient_num in (''' + subjectIds + ''')) t2
		  WHERE t1.PATIENT_NUM=t2.PATIENT_NUM and t1.TRIAL_NAME=t2.TRIAL_NAME and
				   t1.PED_BY_PATIENT_CHR is not null and
				   t2.SUBJECT_SNP_DATASET_ID=t1.SNP_DATASET_ID and
				t1.chrom in (?) and t1.trial_name=?'''

	new Sql(dataSource).eachRow(query, [chroms, trialName]) { row ->
	    if (row.PED_BY_PATIENT_CHR != null) {
		Clob clob = (Clob) row.PED_BY_PATIENT_CHR
		plinkPedFile.append "$row.PATIENT_NUM $row.PATIENT_NUM 0 0 $row.PATIENT_GENDER $isAffected $clob.asciiStream.text\n"
            }
        }
    }

    def getPhenotypicDataByPatient(String subjectIds) {}

    def getPhenotypicDataByChromosome(String chromsomes) {}

    def getPhenotypicDataByGene(String genes) {}
}
