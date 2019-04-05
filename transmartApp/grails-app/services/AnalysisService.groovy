import groovy.sql.Sql
import org.springframework.beans.factory.annotation.Autowired

import javax.sql.DataSource

/**
 * Helps run analysis components like the modules in the advanced workflow menu.
 * @author MMcDuffie
 */
class AnalysisService {

    static transactional = false

    @Autowired private DataSource dataSource

    /**
     * Genes from the database for a given list of Sample Ids.
     * We access the haploview_data table to get this information.
     */
    List<String> getGenesForHaploviewFromSampleId(result) {
	List<String> genes = []

	Sql sql = new Sql(dataSource)

        //Query the Haploview table directly to get the genes. The Sample ID should be the 'i2b2_id' or patient_num
	String sqlt = '''
				SELECT DISTINCT gene
				FROM DEAPP.haploview_data hd
				WHERE	hd.I2B2_ID IN (?)
				order by gene asc'''

	for (currentSampleList in result) {
            String[] currentStringArray = (String[]) currentSampleList.value
	    sql.eachRow(sqlt, [quoteCSV(currentStringArray.join(','))], { row ->
		if (!genes.contains(row.gene)) {
		    genes << row.gene
		}
            })
        }

	genes
    }

    String quoteCSV(String val) {
        StringBuilder s = new StringBuilder()
	if (val) {
	    String[] split = val.split(',')
	    s << "'" << split[0] << "'"
	    for (int i = 1; i < split.length; i++) {
		s << ",'" << split[i] << "'"
            }
        }
	s
    }
}
