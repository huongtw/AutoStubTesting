package com.dse.parser.dependency.finder;

import com.dse.parser.dependency.finder.Level;

import java.util.List;

/**
 * Get searching space of a node in the structure tree.
 * <p>
 * If the node is function/attribute, the searching space consists of the
 * containing file, and all included file (represented in #include)
 *
 * @author DucAnh
 */
public interface IVariableSearchingSpace {

    /**
     * Get the variable searching space
     *
     * @return
     */
    List<Level> getSpaces();

    /**
     * Generate Extend spaces with source code implement header node
     *
     * @return
     */
    List<Level> generateExtendSpaces();
}