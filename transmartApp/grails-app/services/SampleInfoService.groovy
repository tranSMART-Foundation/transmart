import groovy.transform.CompileStatic
import i2b2.SampleInfo
import org.springframework.beans.factory.annotation.Autowired

import javax.sql.DataSource

@CompileStatic
class SampleInfoService {

    static transactional = false

    @Autowired private DataSource dataSource

    /*
     * Get a list of SampleInfo objects based on a string of sample IDs.
     */
    List<SampleInfo> getSampleInfoListInOrder(String sampleIdListStr) {
	if (!sampleIdListStr) {
	    return null
	}

        List<SampleInfo> sampleInfoList = SampleInfo.findAll('from SampleInfo where id in (' + quoteCSV(sampleIdListStr) + ')')

	Map<String, SampleInfo> sampleInfoById = [:]
        for (SampleInfo sampleInfo : sampleInfoList) {
	    sampleInfoById[sampleInfo.id] = sampleInfo
        }

	List<SampleInfo> infos = []

	for (String sampleIdStr in sampleIdListStr.split(',')) {
	    infos << sampleInfoById[sampleIdStr]
        }

	infos
    }

    /**
     * Take a comma seperated list and return the same list with single quotes around each item.
     */
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
