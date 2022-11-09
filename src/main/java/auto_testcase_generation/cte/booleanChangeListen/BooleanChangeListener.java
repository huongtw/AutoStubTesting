package auto_testcase_generation.cte.booleanChangeListen;

import java.util.EventListener;

public interface BooleanChangeListener extends EventListener {
    public void stateChanged(BooleanChangeEvent event);
}
