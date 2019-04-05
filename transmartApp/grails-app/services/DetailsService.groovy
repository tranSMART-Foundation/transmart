import org.transmart.biomart.BioDataExternalCode

/**
 * @author mmcduffie
 */
class DetailsService {

    static transactional = false

    String getHydraGeneID(id) {
	BioDataExternalCode.executeQuery '''
		SELECT DISTINCT bec.code
		FROM org.transmart.biomart.BioDataExternalCode bec
		WHERE bec.bioDataId=?
		AND bec.codeType='HYDRA_GENE_ID' ''',
		[Long.valueOf(String.valueOf(id))][0] ?: ''
    }
}
