package annotation

import groovy.util.logging.Slf4j

@Slf4j('logger')
class AmTagItem implements Comparable<AmTagItem> {
    Boolean activeInd
    String codeTypeName
    String displayName
    Integer displayOrder
    Boolean editable
    String guiHandler
    Integer maxValues
    Boolean required
    String tagItemAttr
    String tagItemSubtype
    String tagItemType
    Boolean viewInChildGrid
    Boolean viewInGrid

    static belongsTo = [amTagTemplate: AmTagTemplate]

    static mapping = {
        table 'AMAPP.am_tag_item'
	id generator: 'sequence', params: [sequence: 'AMAPP.SEQ_AMAPP_DATA_ID'], column: 'tag_item_id'
        version false
        cache true
        sort 'displayOrder'

	amTagTemplate joinTable: [name: 'AMAPP.am_tag_template', key: 'tag_template_id', column: 'tag_item_id'],
	    lazy: false, column: 'tag_template_id'
    }

    static constraints = {
	codeTypeName maxSize: 200
	displayName maxSize: 200
	guiHandler maxSize: 200
	tagItemAttr maxSize: 200
	tagItemSubtype nullable: true
	tagItemType maxSize: 200
    }

    int compareTo(AmTagItem itemIn) {
	itemIn.displayOrder != null && displayOrder != null ?
	    displayOrder.compareTo(itemIn.displayOrder) :
	    displayName?.compareTo(itemIn.displayName);
    }

    String toString() {
	'ID: ' + id + ', Display Name: ' + displayName + ', Display Order: ' + displayOrder
    }
}
