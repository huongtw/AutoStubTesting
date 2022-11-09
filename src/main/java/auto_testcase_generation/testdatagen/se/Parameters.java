package auto_testcase_generation.testdatagen.se;

import java.util.ArrayList;
import java.util.List;

import com.dse.parser.object.IVariableNode;

/**
 * Represent the paramaters of a function including the arguments + external
 * variables
 *
 * @author ducanhnguyen
 */
public class Parameters extends ArrayList<IVariableNode> {

    /**
     *
     */
    private static final long serialVersionUID = -2583457982870539611L;

    public Parameters() {
    }

    public <T extends List<IVariableNode>> Parameters(T parameters) {
        this.addAll(parameters);
    }

}
