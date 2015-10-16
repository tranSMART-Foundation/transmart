package heim.parameter

/**
 * Created by glopes on 09-10-2015.
 */
interface Parameter<T> {
    /**
     * The type of parameter (e.g. input concept, processed input step, number of
     * iterations etc.)
     */
    String getParameterCategory()

    /**
     * Something to disambiguate between parameters of the same category.
     */
    String getKey()

    T getValue()
}
