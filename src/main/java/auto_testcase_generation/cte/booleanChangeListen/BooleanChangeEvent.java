package auto_testcase_generation.cte.booleanChangeListen;

import java.util.EventObject;

public class BooleanChangeEvent extends EventObject {
    private final BooleanChangeDispatcher dispatcher;

    public BooleanChangeEvent(BooleanChangeDispatcher dispatcher) {
        super(dispatcher);
        this.dispatcher = dispatcher;
    }

    // type safe way to get source (as opposed to getSource of EventObject
    public BooleanChangeDispatcher getDispatcher() {
        return dispatcher;
    }

}
