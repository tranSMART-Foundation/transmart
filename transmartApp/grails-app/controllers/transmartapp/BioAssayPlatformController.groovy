package transmartapp

import grails.converters.JSON
import org.transmart.biomart.BioAssayPlatform

class BioAssayPlatformController {

    def platformsForVendor() {

	List<Object[]> platforms
        if (params.type) {
	    platforms = BioAssayPlatform.executeQuery('''
					SELECT bd.uniqueId, p.name
					FROM BioAssayPlatform p, BioData bd
					WHERE p.id=bd.id
					and bd.type='BIO_ASSAY_PLATFORM'
					and p.vendor = :term
					AND p.platformType = :type''',
						      [term: params.vendor, type: params.type])
        }
        else {
	    platforms = BioAssayPlatform.executeQuery('''
					SELECT id, name, accession
					FROM BioAssayPlatform p
					WHERE p.vendor = :term''',
						      [term: params.vendor])
        }

	List<Map> itemlist = []
	for (Object[] platform in platforms) {
	    itemlist << [id: platform[0], title: platform[1], accession: platform[2]]
        }

	render([rows: itemlist] as JSON)
    }

    def getSelections(String name, String vendor, String platformTechnology,
	              String platformType, String platformName) {

	Map<String, Object> paramMap = [:]

	StringBuilder sb = new StringBuilder()
	sb << 'SELECT bd.unique_id, p.' << name
	sb << ''' FROM BioAssayPlatform p, BioData bd  WHERE p.id=bd.id and bd.bioDataType='BIO_ASSAY_PLATFORM' '''

	if (vendor) {
	    sb << ' and p.vendor = :vendor'
	    paramMap.vendor = vendor
        }

	if (platformTechnology) {
	    sb << ' and p.platformTechnology = :platformTechnology'
	    paramMap.platformTechnology = platformTechnology
        }

	if (platformType) {
	    sb << ' and p.platformType = :platformType'
	    paramMap.platformType = platformType
        }

	if (platformName) {
	    sb << ' and p.name = :platformName'
	    paramMap.platformName = platformName
        }

        // sort
	sb << ' order by ' << param.sort

	List<Object[]> platforms = BioAssayPlatform.executeQuery(sb.toString(), paramMap)
    }

    def getPlatforms(String vendor, String platformTechnology, String platformType,
	             String platformName) {

	Map<String, Object> paramMap = [:]

        // construct query
	StringBuilder sb = new StringBuilder()
	sb << '''SELECT bd.unique_id, p.name FROM BioAssayPlatform p, BioData bd  WHERE p.id=bd.id and bd.bioDataType='BIO_ASSAY_PLATFORM' '''

	if (vendor) {
	    sb << ' and p.vendor = :vendor'
	    paramMap.vendor = vendor
        }

	if (platformTechnology) {
	    sb << ' and p.platformTechnology = :platformTechnology'
	    paramMap.platformTechnology = platformTechnology
        }

	if (platformType) {
	    sb << ' and p.platformType = :platformType'
	    paramMap.platformType = platformType
        }

	if (platformName) {
	    sb << ' and p.name = :platformName'
	    paramMap.platformName = platformName
        }

        // sort
	sb << ' order by name'

	List<Object[]> platforms = BioAssayPlatform.executeQuery(sb.toString(), paramMap)
    }
}
