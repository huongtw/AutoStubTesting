package com.dse.util;

/**
 * Contain regexes that make your regex clearly
 *
 * @author ducanh
 */
public interface IRegex {
    String DENOMINATOR_SEPARATOR = "\\/";

    /**
     * Ex: 1.23 <br/>
     * Ex: -1.234 <br/>
     * Ex: +1.234
     */
    String NUMBER_REGEX = "[\\+\\-\\d][.\\d]+";

    String INTEGER_NUMBER_REGEX = "[\\+\\-\\d][\\d]*";

    /**
     * Ex: 123 <br/>
     * Ex: +1234
     */
    String POSITIVE_INTEGER_REGEX = "[\\+]*[\\d]+";

    String OPENING_PARETHENESS = "\\(";
    String CLOSING_PARETHENESS = "\\)";

    String ARGUMENT_LIST = "(?:[^)(]+|\\((?:[^)(]+|\\([^)(]*\\))*\\))*";

    String ANY = ".+";

    /**
     * Ex1: (1+1) <br/>
     * Ex2: (1+1-1) <br/>
     * Ex3: (1/1) <br/>
     * Ex4:(1*1)
     */
    String EXPRESSION_IN_PARETHENESS = IRegex.OPENING_PARETHENESS + "([0-9\\.\\+\\-\\*/]+)"
            + IRegex.CLOSING_PARETHENESS;

    String OPENING_BRACKET = "\\[";
    String CLOSING_BRACKET = "\\]";
    String EXPRESSION_IN_BRACKET = IRegex.OPENING_BRACKET + "([\\(\\)\\s0-9\\.\\+\\-\\*/]+)"
            + IRegex.CLOSING_BRACKET;

    /**
     * Ex1: abc <br/>
     * Ex2: a1b2c3 <br/>
     * Ex3: a1_b2_c3
     */
    String NAME_REGEX = "[a-zA-Z0-9\\_]+";

    String SPACES = "\\s*";

    String POINTER = "\\*";

    String REFERENCE_OPERATOR = "\\&";

    String ADDRESS = "\\&";

    /**
     * Ex1: [1+1] <br/>
     * Ex2: [1+1-1] <br/>
     * Ex3: [1/1] <br/>
     * Ex4:[1*1]
     */
    String ARRAY_INDEX = IRegex.OPENING_BRACKET + IRegex.SPACES + "([^\\]\\[]*)" + IRegex.SPACES
            + IRegex.CLOSING_BRACKET;

    String ARRAY_ITEM = IRegex.NAME_REGEX + IRegex.ARRAY_INDEX;

    String DOT_DELIMITER = "\\.";

    /**
     * Ex: 1.2
     */
    String FLOAT_REGEX = IRegex.NUMBER_REGEX + IRegex.DOT_DELIMITER + IRegex.NUMBER_REGEX;

    String FUNCTION_POINTER = ANY + SPACES
            + OPENING_PARETHENESS + POINTER + NAME_REGEX + CLOSING_PARETHENESS + SPACES
            + OPENING_PARETHENESS + ".*" + CLOSING_PARETHENESS;

}
