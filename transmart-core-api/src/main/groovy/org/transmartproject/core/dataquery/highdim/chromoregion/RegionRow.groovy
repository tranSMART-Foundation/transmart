package org.transmartproject.core.dataquery.highdim.chromoregion

import org.transmartproject.core.dataquery.DataRow
import org.transmartproject.core.dataquery.highdim.AssayColumn

interface RegionRow<V> extends Region, DataRow<AssayColumn, V> { }
