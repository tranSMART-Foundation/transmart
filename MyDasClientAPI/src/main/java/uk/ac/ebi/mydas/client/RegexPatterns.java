package uk.ac.ebi.mydas.client;

import java.util.regex.Pattern;

/**
 * Standard regular expressions.
 * @author Phil Jones
 * Date: 25-Jun-2008
 * Time: 12:45:34
 */
public abstract class RegexPatterns {
    /**
	 * Regex for integer that includes optional exponent, sign.
     */
    public static final Pattern INTEGER_PATTERN = Pattern.compile("^[+-]?(\\d+)([eE][+-]?[0-9]+)?$");
    /**
	 * Regex for float that includes optional exponent, sign.
     */
    public static final Pattern FLOAT_PATTERN = Pattern.compile("^[+-]?((\\d+(\\.\\d*)?)|\\.\\d+)([eE][+-]?[0-9]+)?$");

    static final Pattern FIND_DSN_NAME_PATTERN = Pattern.compile("/das/([^/]+)/");
}
