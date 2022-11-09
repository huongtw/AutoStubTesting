package auto_testcase_generation.testdatagen.se.normalization;

import com.dse.parser.normalizer.AbstractNormalizer;
import auto_testcase_generation.testdatagen.se.memory.IVariableNodeTable;

/**
 * Normalize path constraint
 *
 * @author DucAnh
 */
public abstract class AbstractPathConstraintNormalizer extends AbstractNormalizer implements IPathConstraintNormalizer {

    /**
     * Table of variables
     */
    protected IVariableNodeTable tableMapping;

    public IVariableNodeTable getTableMapping() {
        return tableMapping;
    }

    public void setTableMapping(IVariableNodeTable tableMapping) {
        this.tableMapping = tableMapping;
    }

}
