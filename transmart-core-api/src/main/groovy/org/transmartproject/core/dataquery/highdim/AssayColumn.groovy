package org.transmartproject.core.dataquery.highdim

import org.transmartproject.core.dataquery.DataColumn
import org.transmartproject.core.dataquery.assay.Assay

/**
 * Represents an assay used as a data result column.
 */
interface AssayColumn extends Assay, DataColumn { }
