 /*************************************************************************
 * tranSMART - translational medicine data mart
 *
 * Copyright 2008-2012 Janssen Research & Development, LLC.
 *
 * This product includes software developed at Janssen Research & Development, LLC.
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License 
 * as published by the Free Software  * Foundation, either version 3 of the License, or (at your option) any later version, along with the following terms:
 * 1.	You may convey a work based on this program in accordance with section 5, provided that you retain the above notices.
 * 2.	You may convey verbatim copies of this program code as you receive it, in any medium, provided that you retain the above notices.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS    * FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 *
 ******************************************************************/

import com.recomdata.genesignature.FileSchemaException
import com.recomdata.search.query.Query
import grails.transaction.Transactional
import groovy.util.logging.Slf4j
import org.hibernate.SQLQuery
import org.hibernate.SessionFactory
import org.hibernate.type.StandardBasicTypes
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.multipart.MultipartFile
import org.transmart.SearchKeywordService
import org.transmart.biomart.BioData
import org.transmart.biomart.BioMarker
import org.transmart.plugin.shared.SecurityService
import org.transmart.searchapp.AuthUser
import org.transmart.searchapp.GeneSignature
import org.transmart.searchapp.GeneSignatureItem
import org.transmart.searchapp.SearchKeyword
import org.transmartproject.db.dataquery.highdim.mrna.DeMrnaAnnotationCoreDb
import org.transmartproject.db.dataquery.highdim.rnaseqcog.DeRnaAnnotation
import org.transmartproject.db.support.DatabasePortabilityService

import static org.transmartproject.db.support.DatabasePortabilityService.DatabaseType.ORACLE

/**
 * @author mmcduffie
*/

@Slf4j('logger')
class GeneSignatureService {

    // fold change metric codes
    static final String METRIC_CODE_TRINARY = 'TRINARY'
    static final String METRIC_CODE_ACTUAL = 'ACTUAL'
    static final String METRIC_CODE_GENE_LIST = 'NOT_USED'

    @Autowired private DatabasePortabilityService databasePortabilityService
    @Autowired private SearchKeywordService searchKeywordService
    @Autowired private SecurityService securityService
    @Autowired private SessionFactory sessionFactory

    /**
     * verify file matches indicated schema
     */
    void verifyFileFormat(MultipartFile file, Long schemaColCt, String metricType) throws FileSchemaException {
        BufferedReader br = null

        // check column count
        int colCount = schemaColCt
	if (metricType == METRIC_CODE_GENE_LIST) {
	    colCount = colCount - 1
	}

	String origFile = file.originalFilename

        try {
	    br = new BufferedReader(new InputStreamReader(file.inputStream))

            // parse file (read first three lines only)
            int i = 0
            while (br.ready() && i < 3) {
                i++
		String record = br.readLine().trim()
		if (!record) {
		    continue
		}

		List<String> items = []
		StringTokenizer st = new StringTokenizer(record, '\t')
                while (st.hasMoreTokens()) {
		    items << st.nextToken()
                }

                // check column count
		if (items.size() != colCount) {
		    throw new FileSchemaException("Invalid number of columns, please check file:'" +
						  origFile + "' settings and/or correct usage of tab delimiter")
		}

                // check metric code
                String foldChgTest
                switch (metricType) {

                    case METRIC_CODE_TRINARY:
			foldChgTest = items[-1]
                        int triFoldChg

                        try {
                            triFoldChg = Integer.parseInt(foldChgTest)
			    if (triFoldChg != -1 && triFoldChg != 0 && triFoldChg != 1) {
				throw new FileSchemaException('ERROR: Fold-change value (' + triFoldChg +
							      ") in file:'" + origFile + "' did not match one of the trinary indicators (i.e. -1,0,1)!")
			    }
                        }
                        catch (NumberFormatException e) {
			    throw new FileSchemaException("Invalid fold-change in file:'" + origFile +
							  "' for Metric indicator: " + METRIC_CODE_TRINARY + ' (' + foldChgTest + ')', e)
                        }

                        break

                    case METRIC_CODE_ACTUAL:
			foldChgTest = items[-1]
                        double actFoldChg
                        try {
                            actFoldChg = Double.parseDouble(foldChgTest)
			    if (actFoldChg == -1 || actFoldChg == 0 || actFoldChg == 1) {
				throw new FileSchemaException('Fold-change value (' + foldChgTest +
							      ")  in file:'" + origFile +
							      "' appears to be trinary instead of an actual fold change!")
			    }
                        }
                        catch (NumberFormatException e) {
			    throw new FileSchemaException("Invalid fold-change in file:'" + origFile +
							  "' for Metric indicator: " + METRIC_CODE_ACTUAL +
							  ' (' + foldChgTest + ')', e)
                        }

                        break

                    case METRIC_CODE_GENE_LIST:
                        break
                }
            }
        }
        finally {
            br.close()
        }
    }

    List<GeneSignatureItem> loadGeneSigItemsFromList(List<String> markers) {
	List<GeneSignatureItem> gsItems = []
	SortedSet<String> invalidSymbols = new TreeSet<>()
        Double foldChg = null

	for (String geneSymbol in markers) {
	    List<Object[]> marker = lookupBioAssociations(geneSymbol)
	    if (!marker) {	// SNP RSIDs processed first
		String snpUid = lookupSnpBioAssociations(geneSymbol)
                if (snpUid) {
		    gsItems << new GeneSignatureItem(bioDataUniqueId: snpUid)
                    continue
                }
		logger.warn 'WARN: loadGeneSigItemsFromList invalid gene symbol: {}', geneSymbol
		invalidSymbols << geneSymbol
                continue
            }

	    // anything else is a gene biomarker
            for (int j = 0; j < marker.size(); j++) {
		Long bioMarkerId = marker[j][0]
		String uniqueId = marker[j][1]
		gsItems << new GeneSignatureItem(bioMarker: BioMarker.load(bioMarkerId),
						 bioDataUniqueId: uniqueId,
						 foldChgMetric: foldChg)
            }
        }

        // check for invalid symbols
	if (invalidSymbols) {
	    FileSchemaException.ThrowInvalidGenesFileSchemaException(invalidSymbols)
	}

	gsItems
    }

    /**
     * parse file and create associated gene sig item records
     */
    private List<GeneSignatureItem> loadGeneSigItemsFromFile(MultipartFile file, String organism,
	                                                     String metricType, String fileSchemaName, String domainKey,
	                                                     GeneSignature gs) throws FileSchemaException {
        BufferedReader br = null
	List<GeneSignatureItem> gsItems = []
	SortedSet<String> invalidSymbols = new TreeSet<>()
	String origFile = file.originalFilename

        try {
	    br = new BufferedReader(new InputStreamReader(file.inputStream))

            // parse file (read first three lines only)
            int i = 0

            while (br.ready()) {
                i++
		String record = br.readLine().trim()
		if (!record) {
		    continue
		}

		List<String> items = []
		StringTokenizer stt = new StringTokenizer(record, '\t')
		while (stt.hasMoreTokens()) {
		    items << stt.nextToken()
                }

                String geneSymbol = (String) items.get(0)
                String foldChgTest = (String) items.get(items.size() - 1)
                Double foldChg = null

                // parse fold change metric for non gene lists
                if (metricType != METRIC_CODE_GENE_LIST) {
                    // check valid fold change
		    if (foldChgTest) {
                        try {
                            foldChg = Double.parseDouble(foldChgTest)
                        }
                        catch (NumberFormatException e) {
			    logger.error 'invalid number format detected in file ({})', foldChgTest, e
			    throw new FileSchemaException('Invalid fold-change number detected in file:"' +
							  origFile + '", please correct (' + foldChgTest + ')', e)
                        }
                    }
                }

                // lookup gene symbol or probeset id
                if (fileSchemaName.toUpperCase() =~ /GENE /) {
		    if(domainKey == GeneSignature.DOMAIN_KEY_GL) { // gene list: iterate over all genes from any organism
			List<Object[]> marker = lookupBioAssociations(geneSymbol)
			if (!marker) {	// SNP RSIDs processed first
			    String snpUid = lookupSnpBioAssociations(geneSymbol)
			    if (snpUid) {
				gsItems << new GeneSignatureItem(bioDataUniqueId: snpUid)
				continue
			    }
			    logger.warn 'WARN: loadGeneSigItemsFromFile invalid gene symbol: {}', geneSymbol
			    invalidSymbols << geneSymbol
			    continue
			}

			// anything else is a gene biomarker
			for (int j = 0; j < marker.size(); j++) {
			    Long bioMarkerId = marker[j][0]
			    String uniqueId = marker[j][1]
			    gsItems << new GeneSignatureItem(bioMarker: BioMarker.load(bioMarkerId),
							     bioDataUniqueId: uniqueId,
							     foldChgMetric: foldChg)
			}
		    }
		    else {	// gene signature with known organism, 
			Object[] marker = lookupBioAssociations(geneSymbol, organism)
			if (!marker) {
			    logger.warn 'WARN: loadGeneSigItemsFromFile invalid gene symbol: {}', geneSymbol
			    invalidSymbols << geneSymbol
                            continue
			}

			Long bioMarkerId = marker[0]
			String uniqueId = marker[1]

			gsItems << new GeneSignatureItem(bioMarker: BioMarker.load(bioMarkerId),
							 bioDataUniqueId: uniqueId, foldChgMetric: foldChg)
		    }
                }
                else if (fileSchemaName.toUpperCase() =~ /PROBESET /) {
		    Map marker = lookupProbesetBioAssociations_probeIds(geneSymbol, gs.techPlatform.accession)
		    if (!marker) {
			logger.warn 'WARN: invalid probe set id: {} for platform {}',
			    geneSymbol, gs.techPlatform.accession
			invalidSymbols << geneSymbol
                        continue
                    }

		    Long probesetId = marker.probesetId
		    String bioDataUniqueId = 'GENE:' + marker.geneId
		    BioMarker bioMarkerId = BioMarker.load(marker.bioMarkerId)
		    gsItems << new GeneSignatureItem(
                        probesetId: probesetId,
                        foldChgMetric: foldChg,
                        bioDataUniqueId: bioDataUniqueId,
			bioMarker: bioMarkerId)
                }
            }

            // check for invalid symbols
	    if (invalidSymbols) {
		FileSchemaException.ThrowInvalidGenesFileSchemaException(invalidSymbols)
            }

	    gsItems
        }
        finally {
            br.close()
        }
    }

    /**
     * mark specified instance public to user community
     */
    @Transactional
    GeneSignature doClone(GeneSignature gs) {
	GeneSignature clone = gs.clone()
	clone
    }

    /**
     * mark specified instance public to user community
     */
    @Transactional
    void makePublic(GeneSignature gs, boolean publicFlag) {
        gs.publicFlag = publicFlag
	GeneSignature savedInst = gs.save()

        // update search link for both GS and GL version
	searchKeywordService.updateGeneSignatureLink savedInst, GeneSignature.DOMAIN_KEY, true
	searchKeywordService.updateGeneSignatureLink savedInst, GeneSignature.DOMAIN_KEY_GL, true
    }

    /**
     * mark specified instance as deleted
     */
    @Transactional
    void delete(GeneSignature gs) {
        gs.deletedFlag = true
	GeneSignature savedInst = gs.save()

        // update search link
	searchKeywordService.updateGeneSignatureLink savedInst, GeneSignature.DOMAIN_KEY, true
	searchKeywordService.updateGeneSignatureLink savedInst, GeneSignature.DOMAIN_KEY_GL, true
    }

    /**
     * delete the indicated items for the gene signature
     */
    @Transactional
    GeneSignature deleteGeneSigItems(GeneSignature gs, List<String> delItems) {

        String inClause = delItems.toString()
        inClause = inClause.replace('[', '')
        inClause = inClause.replace(']', '')

	GeneSignatureItem.executeUpdate 'delete GeneSignatureItem i where i.id in (' + inClause +') and i.geneSignature.id = '+ gs.id

        // load fresh gs and modify
        gs = GeneSignature.get(gs.id)
	gs.modifiedByAuthUser = AuthUser.load(securityService.currentUserId())
        gs.lastUpdated = new Date()
        gs.validate()
	GeneSignature saved = gs
	if (!gs.hasErrors()) {
	    saved = gs.save()
	}
	saved
    }

    /**
     * add gene signature items
     */
    @Transactional
    int addGenSigItems(GeneSignature gs, List<String> geneSymbols,
	               List<String> probes, List<Double> valueMetrics) {

	String organism = gs.techPlatform?.organism
	long fileSchemaId = gs.fileSchemaId
	List<String> invalidSymbols = []
	int i = 0
	List<GeneSignatureItem> gsItems = []

        // iterate symbols
	Iterator<String> iter
	if (geneSymbols) {
	    iter = geneSymbols.iterator()
	}
	if (probes) {
	    iter = probes.iterator()
	}

        while (iter.hasNext()) {
	    String symbol = iter.next()
	    Double foldChgMetric = valueMetrics ? valueMetrics[i] : null
            i++

            // check for invalid symbols
	    if (fileSchemaId == 3) {
		List<Long> probesetMarker = lookupProbesetBioAssociations(symbol, gs.techPlatform.accession)

		if (!probesetMarker) {
		    logger.warn 'WARN: AddGeneSigItems invalid probeset: {}', symbol
		    invalidSymbols << symbol
		    continue
		}

                // create item instance
		Long annot = DeMrnaAnnotationCoreDb.findByProbesetId(probeMarker[0]).probesetId
                if (annot != null) {
		    gsItems << new GeneSignatureItem(probesetId: annot, foldChgMetric: foldChgMetric)
                }
	    }
	    else {
		if(organism == '') {
		    // all genes matching the name, need to loop over them
		    List<Object[]> anygeneMarkers = lookupBioAssociations(symbol)

		    if (!anygeneMarkers) {
			logger.warn 'WARN: AddGeneSigItems invalid gene symbol: {}', symbol
			invalidSymbols << symbol
			continue
		    }

		    for(Object[] anygeneMarker in anygeneMarkers) {
			Long bioMarkerId = anygeneMarker[0]
			String uniqueId = anygeneMarker[1]
			gsItems << new GeneSignatureItem(bioMarker: BioMarker.load(bioMarkerId),
							 bioDataUniqueId: uniqueId, foldChgMetric: foldChgMetric)
		    }
		}
		else {
		    // named gene for named organism
		    Object[] orggeneMarker = lookupBioAssociations(symbol, organism)

		    if (!orggeneMarker) {
			logger.warn 'WARN: AddGeneSigItems invalid {} gene symbol: {}', organism, symbol
			invalidSymbols << symbol
			continue
		    }

		    Long bioMarkerId = orggeneMarker[0]
		    String uniqueId = orggeneMarker[1]
		    gsItems << new GeneSignatureItem(bioMarker: BioMarker.load(bioMarkerId),
						     bioDataUniqueId: uniqueId, foldChgMetric: foldChgMetric)
		}
            }
	}

        // check for invalid gene symbols
	if (invalidSymbols) {
	    FileSchemaException.ThrowInvalidGenesFileSchemaException(invalidSymbols)
	}

        // modify gs and add new items
	gs.modifiedByAuthUser = AuthUser.load(securityService.currentUserId())
        gs.lastUpdated = new Date()

        // add new items
	for (it in gsItems) {
	    gs.addToGeneSigItems it
	}

        gs.validate()
	if (!gs.hasErrors()) {
	    gs.save()
	}

	gsItems.size()
    }

    /**
     * create new GeneSignature and all dependendant objects from wizard
     */
    @Transactional
    GeneSignature saveWizard(GeneSignature gs, MultipartFile file, String domainKey) {

	String metricType = gs.foldChgMetricConceptCode?.bioConceptCode
	String organism = gs.techPlatform?.organism
	String fileSchemaName = gs.fileSchema?.name

        // load gs items (could be from a cloned object)
	if (file) {
	    for (it in loadGeneSigItemsFromFile(file, organism, metricType, fileSchemaName, domainKey, gs)) {
		gs.addToGeneSigItems it
	    }
        }
        // set AuthUser
        if (!gs.createdByAuthUser) {
	    gs.createdByAuthUser = AuthUser.load(securityService.currentUserId())
        }
        else {
	    gs.modifiedByAuthUser = AuthUser.load(securityService.currentUserId())
        }

        // save gs, items, and search link
	GeneSignature savedInst
	savedInst = gs.save(flush: true)

        if (!savedInst) {
	    logger.error 'ERROR saving GeneSignature'
	    gs.errors.allErrors.each {
		logger.error 'ERROR: {}', it
	    }
	    return gs // error saving
        }

	GeneSignature nsave = savedInst
	if (!savedInst.uniqueId) {
	    // need to refresh this object
            if(domainKey == GeneSignature.DOMAIN_KEY_GL) {
		savedInst.updateUniqueIdList()
	    } else {
		savedInst.updateUniqueId()
	    }
        }

        // link objects to search
	searchKeywordService.updateGeneSignatureLink nsave, GeneSignature.DOMAIN_KEY_GL, true
	searchKeywordService.updateGeneSignatureLink nsave, GeneSignature.DOMAIN_KEY, true

	nsave
    }

    /**
     * update GeneSignature and all dependant objects from  wizard
     */
    @Transactional
    void updateWizard(GeneSignature gs, MultipartFile file) {

        // load new items if file present
	if (file?.originalFilename) {
	    String metricType = gs.foldChgMetricConceptCode?.bioConceptCode
	    String organism = gs.techPlatform?.organism
	    String fileSchemaName = gs.fileSchema?.name

            // parse items
	    List<GeneSignatureItem> gsItems = loadGeneSigItemsFromFile(file, organism, metricType, fileSchemaName,
								       GeneSignature.DOMAIN_KEY, gs)

            // delete current items
	    GeneSignatureItem.executeUpdate '''
			delete GeneSignatureItem i
			where i.geneSignature.id = :currentId''',
			[currentId: gs.id]
            gs.geneSigItems = []

            // add new items
	    for (it in gsItems) {
		gs.addToGeneSigItems it
	    }
        }

        // update gs, refresh items, and search link

	GeneSignature savedInst = gs.save()
	searchKeywordService.updateGeneSignatureLink savedInst, GeneSignature.DOMAIN_KEY, true
	searchKeywordService.updateGeneSignatureLink savedInst, GeneSignature.DOMAIN_KEY_GL, true
    }

    /**
     * clone items from a parent onto a clone
     */
    void cloneGeneSigItems(GeneSignature parent, GeneSignature clone) {

	GeneSignatureItem item
	for (it in parent.geneSigItems) {
            if (it.bioMarker) {
		item = new GeneSignatureItem(bioMarker: BioMarker.load(it.bioMarkerId),
					     bioDataUniqueId: it.bioDataUniqueId, foldChgMetric: it.foldChgMetric)
            }
            else {
                item = new GeneSignatureItem(foldChgMetric: it.foldChgMetric, probesetId: it.probesetId)
            }
	    clone.addToGeneSigItems item
        }
    }

    /**
     * match up the uploaded gene symbol with our internal bio_marker & bio_data_uid tables
     */
    Object[] lookupBioAssociations(String geneSymbol, String organism) {
	Query query = new Query(mainTableAlias: 'bd')
	query.addTable 'org.transmart.biomart.BioMarker bm'
	query.addTable 'org.transmart.biomart.BioData bd'
	query.addCondition 'bm.id=bd.id'
	query.addCondition "bm.bioMarkerType='GENE'"
	query.addCondition "bm.organism='" + organism.toUpperCase() + "'"
	query.addCondition "UPPER(bm.name) ='" + geneSymbol.toUpperCase() + "'"
	query.addCondition "bd.type='BIO_MARKER.GENE'"
	query.addSelect 'bm.id'
	query.addSelect 'bd.uniqueId'

	List<Object[]> markers = BioData.executeQuery(query.generateSQL())

        // try ext code lookup if necessary

	if (!markers || markers.size() > 1) {
            query = new Query(mainTableAlias: 'bm')
	    query.addTable 'org.transmart.biomart.BioDataExternalCode ext'
	    query.addTable 'org.transmart.biomart.BioMarker bm'
	    query.addTable 'org.transmart.biomart.BioData bd'
	    query.addCondition 'ext.bioDataId=bm.id'
	    query.addCondition 'bm.id=bd.id'
	    query.addCondition "UPPER(ext.code) = '" + geneSymbol.toUpperCase() + "'"
	    query.addCondition "bm.bioMarkerType='GENE'"
	    query.addCondition "bm.organism='" + organism.toUpperCase() + "'"
	    query.addCondition "bd.type='BIO_MARKER.GENE'"
	    query.addSelect 'bm.id'
	    query.addSelect 'bd.uniqueId'

	    markers = BioMarker.executeQuery(query.generateSQL())

            // check for none or ambiguity
	    if (!markers || markers.size() > 1) {
		return null
            }
	}

	markers[0]
    }

    /**
     * match up the uploaded gene symbol with our internal bio_marker & bio_data_uid tables
     */
    List<Object[]> lookupBioAssociations(String geneSymbol) {
	Query query = new Query(mainTableAlias: 'bd')
	query.addTable 'org.transmart.biomart.BioMarker bm'
	query.addTable 'org.transmart.biomart.BioData bd'
	query.addCondition 'bm.id=bd.id'
	query.addCondition "bm.bioMarkerType='GENE'"
	query.addCondition "UPPER(bm.name) ='" + geneSymbol.toUpperCase() + "'"
	query.addCondition "bd.type='BIO_MARKER.GENE'"
	query.addSelect 'bm.id'
	query.addSelect 'bd.uniqueId'

	List<Object[]> markers = BioData.executeQuery(query.generateSQL())

        // try ext code lookup if necessary

	if (!markers) {
            query = new Query(mainTableAlias:'bm')
	    query.addTable 'org.transmart.biomart.BioDataExternalCode ext'
	    query.addTable 'org.transmart.biomart.BioMarker bm'
	    query.addTable 'org.transmart.biomart.BioData bd'
	    query.addCondition 'ext.bioDataId=bm.id'
	    query.addCondition 'bm.id=bd.id'
	    query.addCondition "UPPER(ext.code) = '" + geneSymbol.toUpperCase() + "'"
	    query.addCondition "bm.bioMarkerType='GENE'"
	    query.addCondition "bd.type='BIO_MARKER.GENE'"
	    query.addSelect 'bm.id'
	    query.addSelect 'bd.uniqueId'

	    markers = BioMarker.executeQuery(query.generateSQL())

            // check for none or ambiguity
	    if (!markers) {
		return null
	    }
        }

	markers
    }

    /**
     * match up the uploaded probeset id with our internal bio_assay_feature_group & bio_data_uid tables
     */
    List<Long> lookupProbesetBioAssociations(String probeset, String platform) {
	Query query = new Query(mainTableAlias: 'bf')

	query.addTable 'DeMrnaAnnotationCoreDb a'
	query.addCondition "a.gplId='" + platform + "'"
	query.addCondition "a.probeId ='" + probeset.replace(' ', '') + "'"
	query.addSelect 'a.probesetId'

	List<Long> markers = DeMrnaAnnotationCoreDb.executeQuery query.generateSQL()

	if (!markers || markers.size() > 1) {
	    query = new Query(mainTableAlias: 'bf')

	    query.addTable 'DeRnaAnnotation a'
	    query.addCondition "a.gplId='" + platform + "'"
	    query.addCondition "a.transcriptId ='" + probeset.replace(' ', '') + "'"
	    query.addSelect 'a.probesetId'

	    markers = DeRnaAnnotation.executeQuery query.generateSQL()
	}

        // check for none or ambiguity
	if (!markers) {
	    return null
	}

	markers
    }


    /**
     * fixed for working with probe_Ids in gene signatures -- 2014.10.31
     * match up the uploaded probeset id with our internal bio_assay_feature_group & bio_data_uid tables
     */
    private Map lookupProbesetBioAssociations_probeIds(String probeset, String platform) {
	Query query = new Query(mainTableAlias: 'bf')

	query.addTable 'DeMrnaAnnotationCoreDb a'
	query.addTable 'BioMarker b'
	query.addSelect 'a.gplId'
	query.addSelect 'a.probeId'
	query.addSelect 'a.geneId'
	query.addSelect 'a.id'
	query.addSelect 'b.id'
	query.addCondition "a.gplId='" + platform + "'"
	query.addCondition "a.probeId ='" + probeset.replace(' ', '') + "'"
	query.addCondition 'CAST(a.geneId as string) = b.primaryExternalId'

	List<Object[]> markers = DeMrnaAnnotationCoreDb.executeQuery(query.generateSQL())

	if(!markers || markers.size() < 1) {
	    query = new Query(mainTableAlias: 'bf')

	    query.addTable 'DeRnaAnnotation a'
	    query.addTable 'BioMarker b'
	    query.addSelect 'a.gplId'
	    query.addSelect 'a.transcriptId'
	    query.addSelect 'a.geneId'
	    query.addSelect 'a.transcriptId'
	    query.addSelect 'b.id'
	    query.addCondition "a.gplId='" + platform + "'"
	    query.addCondition "a.transcriptId ='" + probeset.replace(' ', '') + "'"
	    query.addCondition 'CAST(a.geneId as string) = b.primaryExternalId'

	    markers = DeRnaAnnotation.executeQuery(query.generateSQL())
	}

	if(!markers || markers.size() < 1) {
	    return null
	}

	Object[] mm = markers[0]

	[gplId: mm[0], probeId: mm[1], geneId: mm[2], probesetId: mm[3], bioMarkerId: mm[4]]
    }

    /**
     *  Match the uploaded item with our SNP list
     */
    private String lookupSnpBioAssociations(String keyword) {
	SearchKeyword.findByKeywordAndDataCategory(keyword, 'SNP')?.uniqueId
    }

    /**
     * gets a lit of permissioned gene signature records the user is allowed to view. The returned
     * items are list of domain objects
     */
    List<GeneSignature> listPermissionedGeneSignatures(Long userId, boolean admin) {
	String permCriteria = admin ? '(1=1)' : '(gs.createdByAuthUser.id=' + userId + ' or gs.publicFlag=true)'
	GeneSignature.executeQuery 'from GeneSignature gs where ' + permCriteria +
	    ' and gs.deletedFlag=false order by gs.name'
    }

    /**
     * creates a Map of the gene counts per gene signature including up and down regulation counts for those
     * signatures the user has permission to view
     */
    Map getPermissionedCountMap(Long userId, boolean admin) {
	String permCriteria = admin ? '(1=1)' :
	    '(gs.CREATED_BY_AUTH_USER_ID=' + userId +
	    ' or gs.PUBLIC_FLAG=' + (databasePortabilityService.databaseType == ORACLE ? '1' : 'true') + ')'
	String sql = '''
		select gsi.SEARCH_GENE_SIGNATURE_ID as id, count(*) Gene_Ct,
		       sum(CASE WHEN gsi.FOLD_CHG_METRIC>0 THEN 1 ELSE 0 END) Up_Ct,
		       sum(CASE WHEN gsi.FOLD_CHG_METRIC<0 THEN 1 ELSE 0 END) Down_Ct
		from SEARCHAPP.SEARCH_GENE_SIGNATURE_ITEM gsi
		join SEARCHAPP.SEARCH_GENE_SIGNATURE gs on gsi.search_gene_signature_id=gs.search_gene_signature_id
		where ''' + permCriteria + '''
		and gs.DELETED_FLAG=''' + (databasePortabilityService.databaseType == ORACLE ? '0' : 'false') + '''
		group by gsi.SEARCH_GENE_SIGNATURE_ID'''

	Map countMap = [:]
	SQLQuery hqlQuery = sessionFactory.currentSession.createSQLQuery(sql.toString())
	hqlQuery.addScalar 'id', StandardBasicTypes.LONG
	hqlQuery.addScalar 'Gene_Ct', StandardBasicTypes.LONG
	hqlQuery.addScalar 'Up_Ct', StandardBasicTypes.LONG
	hqlQuery.addScalar 'Down_Ct', StandardBasicTypes.LONG
	for (it in hqlQuery.list()) {
	    countMap[it.getAt(0)] = it
	}

	countMap
    }

    String getGeneSigName(String geneSigId) {
	GeneSignature gs = GeneSignature.get(geneSigId)
	gs?.name ?: geneSigId
    }

    String getGeneSigGMTContent(String geneSigId) {
	GeneSignature gs = GeneSignature.get(geneSigId)

        //write gene-sig items into the GMT file
	StringBuilder sb = new StringBuilder()
	sb << gs?.name ?: '' << '\t'
	sb << gs?.description ?: '' << '\t'

        for (geneSigItem in gs.geneSigItems) {
	    sb << geneSigItem?.geneSymbol ? geneSigItem.geneSymbol.join('/') : '' << '\t'
        }

	sb << '\n'

	sb
    }

    List<String> expandGeneList(String geneListUid) {

	List<String> genesList = []

	GeneSignature geneSig = GeneSignature.findByUniqueId(geneListUid)
	List<GeneSignatureItem> geneKeywords = GeneSignatureItem.findAllByGeneSignature(geneSig)

	for (GeneSignatureItem gsi in geneKeywords) {
	    List<String> symbol = gsi.geneSymbol
	    for (String g in symbol) {
		String bioId = lookupBioAssociations(g, geneSig.techPlatform?.organism)[1]
		if (bioId && !genesList.contains(bioId)) {
		    genesList << bioId
                }
            }
        }

	genesList
    }

    List<String> expandPathway(String pathwayUid) {

	List<String> genesList = []

	List<String> geneKeywords = SearchKeyword.executeQuery('''
		select k_gene.uniqueId
			from org.transmart.searchapp.SearchKeyword k_pathway,
			     org.transmart.biomart.BioMarkerCorrelationMV b,
				org.transmart.searchapp.SearchKeyword k_gene
				where b.correlType = 'PATHWAY GENE'
				and b.bioMarkerId = k_pathway.bioDataId
				and k_pathway.dataCategory = 'PATHWAY'
				and b.assoBioMarkerId = k_gene.bioDataId
				and k_gene.dataCategory = 'GENE'
		  and k_pathway.uniqueId = :pathwayUid''',
		[pathwayUid: pathwayUid])

	for (String keyword in geneKeywords) {
	    if (!genesList.contains(keyword)) {
		genesList << keyword
            }
        }

	genesList
    }
}
