package jobs.table

import groovy.transform.CompileStatic

interface MissingValueAction {

    // null means delete row. Return an empty string to leave the cell empty
    Object getReplacement(String primaryKey)

    @CompileStatic
    static class ThrowExceptionMissingValueAction implements MissingValueAction {

        Class exceptionClass
        String message

        def getReplacement(String primaryKey) {
            throw exceptionClass.newInstance(message)
        }
    }

    @CompileStatic
    static class ConstantReplacementMissingValueAction implements MissingValueAction {

        def replacement

        def getReplacement(String primaryKey) {
            replacement
        }
    }

    @CompileStatic
    static class DropRowMissingValueAction implements MissingValueAction {

        def getReplacement(String primaryKey) {}
    }

}
