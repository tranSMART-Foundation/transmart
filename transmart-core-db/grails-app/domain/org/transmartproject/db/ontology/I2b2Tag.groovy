package org.transmartproject.db.ontology

import org.transmartproject.core.ontology.OntologyTermTag

class I2b2Tag implements OntologyTermTag {

    String description
    String name
    String ontologyTermFullName
    Long position

    static  mapping = {
        table 'i2b2metadata.i2b2_tags'
        id                      column: 'tag_id', generator: 'sequence', params: [sequence: 'i2b2metadata.seq_i2b2_data_id']
        version                 false

        description             column: 'tag'
        name                    column: 'tag_type'
        ontologyTermFullName    column: 'path'
        position                column: 'tags_idx'
        sort                    'position'
    }
}
