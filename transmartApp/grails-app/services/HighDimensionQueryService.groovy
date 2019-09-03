import com.recomdata.export.ExportColumn
import com.recomdata.export.ExportRowNew
import com.recomdata.export.ExportTableNew
import groovy.sql.Sql
import groovy.util.logging.Slf4j
import groovy.util.slurpersupport.GPathResult
import org.apache.commons.io.IOUtils
import org.codehaus.groovy.runtime.NullObject
import org.springframework.beans.factory.annotation.Autowired
import org.transmartproject.core.dataquery.highdim.HighDimensionDataTypeResource
import org.transmartproject.core.exceptions.InvalidRequestException
import org.transmartproject.core.querytool.ConstraintByOmicsValue
import org.transmartproject.db.dataquery.highdim.HighDimensionResourceService

import javax.sql.DataSource

import static org.transmart.authorization.QueriesResourceAuthorizationDecorator.checkQueryResultAccess

/**
 * @author Denny Verbeeck (dverbeec@its.jnj.com)
 */
@Slf4j('logger')
class HighDimensionQueryService {

	static transactional = false

	@Autowired private DataSource dataSource
	@Autowired private I2b2HelperService i2b2HelperService
	@Autowired private HighDimensionResourceService highDimensionResourceService

	List<Map> getHighDimensionalConceptSet(String result_instance_id1, String result_instance_id2) {
	List<Map> result = []

	if (result_instance_id1) {
	    result.addAll getHighDimensionalConceptKeysInSubset(result_instance_id1)
	}
	if (result_instance_id2) {
	    result.addAll getHighDimensionalConceptKeysInSubset(result_instance_id2)
	}

        result
    }

    List<Map> getHighDimensionalConceptKeysInSubset(String resultInstanceId) {
	String sqlt = '''
		SELECT REQUEST_XML
		FROM I2B2DEMODATA.QT_QUERY_MASTER c
		INNER JOIN I2B2DEMODATA.QT_QUERY_INSTANCE a ON a.QUERY_MASTER_ID=c.QUERY_MASTER_ID
		INNER JOIN I2B2DEMODATA.QT_QUERY_RESULT_INSTANCE b ON a.QUERY_INSTANCE_ID=b.QUERY_INSTANCE_ID
		WHERE RESULT_INSTANCE_ID = ?'''

	List<Map> concepts = []
	new Sql(dataSource).eachRow sqlt, [resultInstanceId], { row ->
	    GPathResult xml
            try {
                xml = new XmlSlurper().parse(new StringReader(clobToString(row.request_xml)))
	    }
	    catch (e) {
		throw new InvalidRequestException('Malformed XML document: ' + e.message, e)
	    }

	    for (p in xml.panel) {
		for (i in p.item) {
		    if (i.constrain_by_omics_value.size()) {
			concepts << [
			    concept_key: i.item_key.toString(),
			    omics_selector: i.constrain_by_omics_value.omics_selector.toString(),
			    omics_value_type: i.constrain_by_omics_value.omics_value_type.toString(),
			    omics_value_operator: i.constrain_by_omics_value.omics_value_operator.toString(),
			    omics_value_constraint: i.constrain_by_omics_value.omics_value_constraint.toString(),
			    omics_projection_type: i.constrain_by_omics_value.omics_projection_type.toString(),
			    omics_property: i.constrain_by_omics_value.omics_property.toString()]
		    }
                }
            }
        }

	logger.debug 'High dimensional concepts found: {}', concepts
        concepts
    }

    /**
     * Adds a column of high dimensional data to the grid export table
     */
    ExportTableNew addHighDimConceptDataToTable(ExportTableNew tablein, omics_constraint, String result_instance_id) {
        checkQueryResultAccess result_instance_id

        ExportColumn hascol

        if (!i2b2HelperService.isValidOmicsParams(omics_constraint)) {
            return i2b2HelperService.addConceptDataToTable(tablein, omics_constraint.concept_key, result_instance_id)
        }

        String concept_key = omics_constraint.concept_key
        String selector = omics_constraint.omics_selector
        String projection_type = omics_constraint.omics_projection_type
        String newlabel = selector + ' ' + projection_type

        String columnname =  newlabel
        String columnid = "${concept_key}${newlabel}\\".encodeAsSHA1()

        // Clean up tooltip - remove all except alphanumeric, _-/\()[]
        String columntooltip = "${i2b2HelperService.keyToPath(concept_key)}${newlabel}\\".replaceAll('[^a-zA-Z0-9_/\\-\\\\()\\[\\]]+','_')

        /* add the subject and columnid column to the table if it's not there*/
        if (tablein.getColumn('subject') == null) {
	    tablein.putColumn 'subject',
		new ExportColumn('subject', 'Subject', '', 'string')
        }

        hascol = tablein.getColumnByBasename(columnname); // check existing column with same basename

        if (tablein.getColumn(columnid) == null) {
	    tablein.putColumn columnid,
		new ExportColumn(columnid, columnname, '', 'number', columntooltip)
            if(hascol)
                tablein.setColumnUnique(columnid); // make labels unique by expanding
        }

	HighDimensionDataTypeResource resource =
	    highDimensionResourceService.getHighDimDataTypeResourceFromConcept(concept_key)
	if (resource) {
	    Map<Long, List> data = resource.getDistribution(
                new ConstraintByOmicsValue(projectionType: omics_constraint.omics_projection_type,
					   property      : omics_constraint.omics_property,
					   selector      : omics_constraint.omics_selector),
                concept_key,
                (result_instance_id == '' ? null : result_instance_id as Long))

            data.each { s, v ->
                String subject = s.toString() // this is a Long
                String value = v.toString() // this is a Double
		// If I already have this subject mark it in the subset column as belonging to both subsets
                if (tablein.containsRow(subject)) /*should contain all subjects already if I ran the demographics first*/ {
                    tablein.getRow(subject).put(columnid, value)
                }
		else {
		    ExportRowNew newrow = new ExportRowNew()
		    newrow.put 'subject', subject
		    newrow.put columnid, value
		    tablein.putRow subject, newrow
		}
            }

	    //pad all the empty values for this column
	    for (ExportRowNew row in tablein.rows) {
		if (!row.containsColumn(columnid)) {
		    row.put columnid, 'NULL'
		}
            }
	}

	tablein
    }

    /**
     * Converts a clob to a string for returned Oracle columns
     */
    def String clobToString(clob) {
        if (clob == null) {
            '.'
        }
        else if (clob instanceof NullObject) {
            ''
        }
        else if (clob instanceof String) {
            // postgres schema uses strings in some places oracle uses clobs
            clob
        }
	else {
            def buffer = new byte[1000];
            def num = 0;
            def inStream = clob.asciiStream;
            def out = new ByteArrayOutputStream();
            while ((num = inStream.read(buffer)) > 0) {
		out.write(buffer, 0, num);
            }
            new String(out.toByteArray());
	}
    }
}
