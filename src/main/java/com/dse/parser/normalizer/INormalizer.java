package com.dse.parser.normalizer;

import auto_testcase_generation.testdatagen.structuregen.ChangedTokens;

/**
 * Represent normalization behavior for source code, e.g., file level, function
 * level
 *
 * @author ducanhnguyen
 */
public interface INormalizer {
    /**
     * The value display in the result when the normalization catches an
     * unexpected error
     */
    String ERROR = "//error";

    /**
     * Perform normalization
     */
    void normalize();

    /**
     * Get normalized source code
     *
     * @return
     */
    String getNormalizedSourcecode();

    /**
     * Set normalized source code
     *
     * @param normalizedSourcecode
     */
    void setNormalizedSourcecode(String normalizedSourcecode);

    /**
     * Get original source code
     *
     * @return
     */
    String getOriginalSourcecode();

    /**
     * Set original source code
     *
     * @param originalSourcecode
     */
    void setOriginalSourcecode(String originalSourcecode);

    /**
     * Get changed tokens
     *
     * @return
     */
    ChangedTokens getTokens();

    /**
     * Set changed tokens
     *
     * @param tokens
     */
    void setTokens(ChangedTokens tokens);

    /**
     * Should write the content of file to the hark disk or not
     *
     * @return
     */
    default boolean shouldWriteToFile() {
        return true;
    }
}
