/*
 * Copyright © 2013-2014 The Hyve B.V.
 *
 * This file is part of transmart-core-db.
 *
 * Transmart-core-db is free software: you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any
 * later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * transmart-core-db.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.transmartproject.db.dataquery

import com.google.common.collect.AbstractIterator
import groovy.util.logging.Slf4j
import org.hibernate.ScrollableResults
import org.transmartproject.core.dataquery.DataRow
import org.transmartproject.core.dataquery.TabularResult
import org.transmartproject.core.exceptions.UnexpectedResultException

/**
 * Implementation of {@link TabularResult} that converts an Hibernate scrollable
 * result set where each row has the data for a cell in the
 * {@link TabularResult}.
 *
 * The Hibernate result set should return sequentially (though not necessarily
 * in the same column order unless some functionality described below is used)
 * all the original rows that map to cells on the same {@link TabularResult}
 * row.
 *
 * The properties {@link CollectingTabularResult#rowsDimensionLabel},
 * {@link CollectingTabularResult#columnsDimensionLabel} and
 * {@link CollectingTabularResult#indicesList} must be provided. The order in
 * the indices list is not important unless
 * {@link CollectingTabularResult#allowMissingColumns} is set to
 * <code>true</code>, in which case it must also be the order under which the
 * original rows are returned.
 * For instance, if the indices list is <code>C1, C2, C3</code>, the original
 * result set can return (sequentially) rows mapping to <code>C1, C2, C3</code>
 * or <code>C1, C3</code>, but not <code>C2, C1, C3</code>.
 *
 * The settable closure
 * {@link CollectingTabularResult#inSameGroup} determines if any two original
 * rows belong in the same final (={@link TabularResult} row); in theory, this
 * could permit returning the original rows in any order, but the order
 * mentioned before must be respected.
 *
 * The settable closure {@link CollectingTabularResult#finalizeGroup} is given
 * a set of original rows determined to belong to the same final row (in the
 * same order as in the original result) and returns the final row object, of
 * type <code>R</code>.
 *
 * Unless {@link CollectingTabularResult#allowMissingColumns} is set, this class
 * checks that the number of sequential rows found to correspond to the same
 * final row is equal to the number of indices provided in
 * {@link CollectingTabularResult#allowMissingColumns}. If
 * {@link CollectingTabularResult#columnIdFromRow} is provided, this will be
 * used to generate a better error message.
 *
 * If {@link CollectingTabularResult#allowMissingColumns} is set,
 * {@link CollectingTabularResult#columnIdFromRow} must be provided as well, and
 * the indices list must be provided in order (see above). Note that due to the
 * design of this class, it is impossible to have final rows where all the
 * columns are missing (are <code>null</code>).
 *
 * This class takes ownership of the {@link CollectingTabularResult#results}'
 * session if {@link CollectingTabularResult#closeSession} is <code>true</code>
 * (the default).
 *
 * @param < C > the type for the columns
 * @param < R > the type for the rows
 */
@Slf4j('logger')
abstract class CollectingTabularResult<C, R extends DataRow> implements TabularResult<C, R>, Iterable<R> {

    String            rowsDimensionLabel
    String            columnsDimensionLabel
    List<C>           indicesList

    ScrollableResults results
    Closure<Boolean>  inSameGroup
    Closure<R>        finalizeGroup

    boolean allowMissingColumns = false
    Closure columnIdFromRow
    
    boolean closeSession = true

    private boolean getRowsCalled = false
    private boolean closeCalled = false

    protected abstract String getColumnEntityName()

    // exception created for printing the stack trace in case we detect the object has not been properly closed
    private RuntimeException initialException = new RuntimeException('Instantiated at this point')

    // We expect results.next() to already be called before the first time this method is called.

    R getNextRow() {
        Object[] firstEntry = results.get()
        Object[] latestEntry = firstEntry

        if (firstEntry == null) {
            return null
        }

        List collectedEntries = new ArrayList(indicesList.size())
//	logger.info 'getNextRow start with first entry row {} indicesList.size {} firstEntry {}',
//	    results.getRowNumber(), indicesList.size(), firstEntry
        addToCollectedEntries collectedEntries, firstEntry

        while (results.next() && inSameGroup(firstEntry, results.get())) {
	    if(latestEntry.assayId == results.get().assayId) {
//		logger.info 'DUPLICATE RECORD for assayId {} row {} latestEntry {} next result {}',
//		    latestEntry.assayId, results.getRowNumber(), latestEntry, results.get()
		continue
	    }
	    latestEntry = results.get()
//	    logger.info 'getNextRow next result same group (annotationId+geneSymbol) row {} latestEntry {} next result {}',
//		results.getRowNumber(), latestEntry, results.get()
            addToCollectedEntries collectedEntries, results.get()
        }

//	logger.info 'getNextRow retrieval done at row {} {}', results.getRowNumber(), results.get()

        finalizeCollectedEntries collectedEntries

//	logger.info 'getNextRow finalizeGroup closure {}', finalizeGroup
        finalizeGroup collectedEntries
    }

    protected void finalizeCollectedEntries(List collectedEntries) {
//	logger.info 'finalizeCollectedEntries stack trace {}', Arrays.toString(Thread.currentThread().getStackTrace()).replace( ',', '\n' )
//	logger.info 'finalizeCollectedEntries collectedEntries {} indicesList {}', collectedEntries.size(), indicesList.size()
        if (collectedEntries.size() == indicesList.size()) {
//	    logger.info 'finalizeCollectedEntries DONE checked size {}', collectedEntries.size()
            return
        }


        if (collectedEntries.size() > indicesList.size()) {
//	    logger.info 'collected too many entries {} entries from {} indicesList {}, throw exception', collectedEntries.size(), indicesList.size()
            throw new UnexpectedResultException(
		"Got more ${columnEntityName}s than expected in a row group. " +
		    "This can generally only happen if primary keys on " +
		    "the data table are not being enforced and the same " +
		    "${columnEntityName} appears twice for same row " +
		    "entity (current label for rows is " +
                    "'$rowsDimensionLabel'). Collected " +
		    "${columnEntityName}s for this row: $indicesList")
        }

        if (allowMissingColumns) {
//	    logger.info 'finalizeCollectedEntries fill with nulls till we have the expected size {} to {}', collectedEntries.size(), indicesList.size()
            /* fill with nulls till we have the expected size */
            collectedEntries.addAll Collections.nCopies(indicesList.size() - collectedEntries.size(), null)
            return
        }

        // !allowMissingColumns
        Set columnsNotFound
        if (columnIdFromRow) {
            Set expectedColumnIds = indicesList*.getAt('id') as Set
	    Set gottenColumnIds = collectedEntries.collect { row -> columnIdFromRow(row) } as Set
            columnsNotFound = expectedColumnIds - gottenColumnIds
	}
//	logger.info 'finalizeCollectedEntries {} columns not found and missing is not allowed: {}', columnEntityName.capitalize(), columnsNotFound

	String message = "Expected row group to be of size ${indicesList.size()}; got ${collectedEntries.size()} objects"
        if (columnsNotFound) {
	    message += ". ${columnEntityName.capitalize()} ids not found: ${columnsNotFound}"
	}

        throw new UnexpectedResultException(message)
    }

    protected getIndexObjectId(C object) {
	object['id']
    }

    private void addToCollectedEntries(List collectedEntries, row) {

        String rowAsString

//	logger.info 'addToCollectedEntries row {}', row

        if (allowMissingColumns) {
	    def currentColumnId = columnIdFromRow(row)
            int startSize = collectedEntries.size()
            int i

//	    logger.info 'addToCollectedEntries allowMissingColumns currentColumnId {} startSize {} maxsize {}', currentColumnId, startSize, indicesList.size()

	    for (i = startSize; indicesList[i] != null && getIndexObjectId(indicesList[i]) != currentColumnId; i++) {
//		logger.info 'skipping indicesList[{}] {}', i, getIndexObjectId(indicesList[i])
		collectedEntries << null
            }
            if (indicesList[i] == null) {
                try {
//		    logger.info 'indicesList[{}] null searching for currentColumnId from startSize {}', i, startSize
                    rowAsString = row.toString()
                }
		catch (e) {
		    rowAsString = "<Could not convert row to string, error was ${e.message}>"
		}
		throw new IllegalStateException("Starting at position " +
						"$startSize in the $columnEntityName list, could not " +
						"find $columnEntityName with id $currentColumnId. " +
						"Possible causes: repeated $columnEntityName for " +
						"the same row, bad order by clause in module " +
						"query or bad columnIdFromRow closure. " +
						"Row was: $rowAsString. " +
						"${columnEntityName.capitalize()} id list was " +
						indicesList.collect { getIndexObjectId it })
//	    } else {
//		logger.info 'Found at indicesList[{}] {} with id {}', i, columnEntityName, currentColumnId
	    }
//	} else {
//            rowAsString = row.toString()
//	    logger.info 'Single row {}', rowAsString
	}


	collectedEntries << row
    }

    Iterator<R> getRows() {
        if (getRowsCalled) {
            throw new IllegalStateException('getRows() cannot be called more than once')
        }

        getRowsCalled = true
        if (allowMissingColumns && !columnIdFromRow) {
	    throw new IllegalArgumentException('columnIdFromRow must be set when allowMissingColumns is true')
        }

        // Load first result
//	logger.info 'getRows ... load first result'
        results.next()
//	logger.info 'first result ... rownumber {} {}', results.getRowNumber(), results.get()

	// A correctly typed AbstractIterator<R> crashes the compiler on groovy 2.2.0
        return new AbstractIterator() {
	    def computeNext() {
                getNextRow() ?: endOfData()
            }
        }
    }

    void close() throws IOException {
        closeCalled = true
        results?.close()
        if (closeSession) {
            results.session.close()
        }
    }

    Iterator<R> iterator() {
        getRows()
    }

    protected void finalize() throws Throwable {
        super.finalize()
        if (!closeCalled) {
	    logger.error 'Failed to call close before the object was scheduled to be garbage collected', initialException
            close()
        }
    }
}
