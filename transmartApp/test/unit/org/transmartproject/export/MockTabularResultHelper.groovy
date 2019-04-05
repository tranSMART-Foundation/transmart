package org.transmartproject.export

import org.transmartproject.core.dataquery.DataRow
import org.transmartproject.core.dataquery.Patient
import org.transmartproject.core.dataquery.TabularResult
import org.transmartproject.core.dataquery.assay.SampleType
import org.transmartproject.core.dataquery.assay.Timepoint
import org.transmartproject.core.dataquery.assay.TissueType
import org.transmartproject.core.dataquery.highdim.AssayColumn
import org.transmartproject.core.dataquery.highdim.Platform

class MockTabularResultHelper {

    List<AssayColumn> createSampleAssays(int n) {
	(1..n).collect { long id ->
	    new AssayColumn() {
		String getLabel() { 'assay_' + id }
		Long getId() { id }
		Patient getPatient() {}
		String getPatientInTrialId() { 'patient_' + id + '_subject_id' }
		String getTrialName() {}
		Timepoint getTimepoint() { new Timepoint(null, 'timepoint_' + id) }
		String getSampleCode() { 'sample_code_' + id }
		SampleType getSampleType() { new SampleType(null, 'sampletype_' + id) }
		TissueType getTissueType() { new TissueType(null, 'tissuetype_' + id) }
		Platform getPlatform() { [getId: { -> String.valueOf(id * 10) }] as Platform }
		boolean equals(other) { is other }
		String toString() { 'assay for ' + patientInTrialId }
            }
	}
    }

    private DataRow createRowForAssays(List<AssayColumn> assays, List data, String label) {
	createMockRow dot(assays, data), label
    }

    Map<AssayColumn, Object> dot(List<AssayColumn> assays, List list2) {
	assert assays.size() == list2.size()

	Map<AssayColumn, Object> map = [:]
	for (int i = 0; i < assays.size(); i++) {
	    map[assays[i]] = list2[i]
        }
	map
    }

    TabularResult<AssayColumn, DataRow> createMockTabularResult(Map params) {
        List<AssayColumn> sampleAssays = params.assays
        Map<String, List<Object>> labelToData = params.data
	String columnsDimensionLabel = params.columnsLabel ?: null
	String rowsDimensionLabel = params.rowsLabel ?: null

	Iterator<DataRow<AssayColumn, Object>> iterator = labelToData.collect { String label, List data ->
	    createRowForAssays sampleAssays, data, label
        }.iterator()

	new TabularResult() {
	    List getIndicesList() { sampleAssays }
	    Iterator getRows() { iterator }
	    String getColumnsDimensionLabel() { columnsDimensionLabel }
	    String getRowsDimensionLabel() { rowsDimensionLabel }
	    Iterator iterator() { iterator }
	    void close() {}
        }
    }

    private DataRow<AssayColumn, Object> createMockRow(Map<AssayColumn, Object> values, String label) {
	new DataRow<AssayColumn, Object>() {
	    String getLabel() { label }
	    Object getAt(int i) { values.values()[i] }
	    Object getAt(AssayColumn assayColumn) { values[assayColumn] }
	    Iterator<Object> iterator() { values.values().iterator() }
	}
    }
}
