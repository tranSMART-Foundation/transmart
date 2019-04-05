package org.transmartproject.export

import groovy.util.logging.Slf4j
import org.transmartproject.core.dataquery.TabularResult
import org.transmartproject.core.dataquery.highdim.AssayColumn
import org.transmartproject.core.dataquery.highdim.chromoregion.RegionRow
import org.transmartproject.core.dataquery.highdim.projections.Projection

@Slf4j('logger')
abstract class AbstractChromosomalRegionBedExporter implements HighDimExporter {

    static final String COLUMN_SEPARATOR = '\t'
    static final int CHROMOSOME_COLUMN_POSITION = 1
    static final int ITEM_RGB_COLUMN_POSITION = 9

    String getFormat() {
        'BED'
    }

    String getDescription() {
        '''BED format provides a flexible way to define the data lines that are displayed in an annotation track.
        See http://genome.ucsc.edu/FAQ/FAQformat.html#format1'''
	}

    void export(TabularResult tabularResult, Projection projection,
	        Closure<OutputStream> newOutputStream, Closure<Boolean> isCancelled = null) {

	logger.info 'started exporting to {}', format

        if (isCancelled && isCancelled()) {
            return
        }

	long startTime = System.currentTimeMillis()

        List<AssayColumn> assayList = tabularResult.indicesList
	Map<Long, Writer> streamsPerSample = [:]

        try {
	    for (RegionRow datarow in tabularResult) {
                if (isCancelled && isCancelled()) {
                    return
                }

                int rowNumber = 0
		for (AssayColumn assay in assayList) {
                    if (isCancelled && isCancelled()) {
                        return
                    }

                    if (!datarow[assay]) {
			logger.debug '(datrow.id={}, assay.id={}) No cell data.', datarow.id, assay.id
                        continue
                    }

                    List row = calculateRow(datarow, assay)
                    if (row[0..2].any { !it }) {
			logger.debug '(datrow.id={}, assay.id={}) Row has not required values: {}. Skip it.',
			    datarow.id, assay.id, row
                        continue
                    }

                    Writer writer = streamsPerSample[assay.id]
                    if (writer == null) {
			writer = new BufferedWriter(new OutputStreamWriter(
			    newOutputStream(assay.sampleCode + '_' + assay.id, format), 'UTF-8'))

                        //Write header line
			writer << 'track name="' + assay.sampleCode + '" '
                        if (row.size() >= ITEM_RGB_COLUMN_POSITION) {
			    writer << 'itemRgb="On" '
                        }
                        else {
			    writer << 'useScore="1" '
                        }
                        if (datarow.platform) {
			    writer << 'genome_build="' + datarow.platform.genomeReleaseId + '"'
                        }
                        writer << '\n'

                        streamsPerSample[assay.id] = writer
                    }

                    String chromosome = row[CHROMOSOME_COLUMN_POSITION - 1]
                    if (!chromosome.toLowerCase().startsWith('chr')) {
                        row[CHROMOSOME_COLUMN_POSITION - 1] = 'chr' + chromosome
                    }
                    writer << row.join(COLUMN_SEPARATOR) << '\n'
		    rowNumber++
                }

                if (rowNumber < assayList.size()) {
		    logger.warn '{} rows from {} were skipped.',
			assayList.size() - rowNumber, assayList.size()
                }
            }
        }
        finally {
	    for (it in streamsPerSample.values()) {
                it.close()
            }
        }

	logger.info 'Exporting data took {} ms', System.currentTimeMillis() - startTime
    }

    protected abstract List calculateRow(RegionRow datarow, AssayColumn assay)
}
