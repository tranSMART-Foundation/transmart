package org.transmart

import groovy.transform.CompileStatic
import org.transmart.searchapp.SearchKeyword

/**
 * @author mmcduffie
 */
@CompileStatic
class KeywordSet extends LinkedHashSet<SearchKeyword> {

    KeywordSet plus(KeywordSet ks) {
	if (ks) {
	    addAll ks
	}
	this
    }

    List<String> getKeywordUniqueIds() {
	List<String> uniqueIds = []
	for (SearchKeyword keyword in this) {
	    uniqueIds << keyword.uniqueId
        }
	uniqueIds
    }

    List<Long> getKeywordDataIds() {
	List<Long> bioDataIds = []
	for (SearchKeyword keyword in this) {
	    bioDataIds << keyword.bioDataId
        }
	bioDataIds
    }

    String getKeywordDataIdString() {
        StringBuilder s = new StringBuilder()

	for (SearchKeyword keyword in this) {
	    if (s) {
		s << ', '
            }
	    s << keyword.bioDataId
        }

	s.toString()
    }

    boolean removeKeyword(SearchKeyword keyword) {
        for (k in this) {
	    if (k.uniqueId == keyword.uniqueId) {
		return remove(k)
            }
        }
    }
}
