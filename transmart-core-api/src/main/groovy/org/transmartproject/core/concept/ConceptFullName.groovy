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

package org.transmartproject.core.concept

import com.google.common.base.Splitter
import com.google.common.collect.Lists
import groovy.transform.CompileStatic
import groovy.transform.EqualsAndHashCode

/**
 * Represents an i2b2 concept full name.
 */
@CompileStatic
@EqualsAndHashCode
final class ConceptFullName {

    private final String fullPath

    final List<String> parts

    ConceptFullName(String path) {
        if (!path || path[0] != '\\') {
            throw new IllegalArgumentException('Path must start with \\')
	}
        if (path.size() < 2) {
            throw new IllegalArgumentException('Path is too short')
	}
        if (path[-1] != '\\') {
            path += '\\'
	}

        fullPath = path

        // trim the first leading and trailing backslash for split
        path = path.replaceFirst('^\\\\', '').replaceFirst('\\\\$', '')
        parts = Lists.newArrayList(Splitter.on('\\').split(path))

        if (!parts || '' in parts) {
            throw new IllegalArgumentException("Path cannot have empty parts (got '$path')")
        }
    }

    String getAt(int index) {
        if (index < 0) {
            index = parts.size() + index
	}

        index < parts.size() ? parts[index] : null
    }

    List<String> getAt(Range range) {
        parts[range]
    }

    int getLength() {
        parts.size()
    }

    ConceptFullName getParent() {
        if (length == 1) {
            return null
        }
        new ConceptFullName('\\' + parts[0..-2].join('\\') + '\\')
    }

    boolean isSiblingOf(ConceptFullName otherFullName) {
        otherFullName.length == length && length == 1 ||
                otherFullName.parts[1..-1] == parts[1..-1]
    }

    String toString() {
        fullPath
    }
}
