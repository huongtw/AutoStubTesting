package auto_testcase_generation.cte.booleanChangeListen;

public interface BooleanChangeDispatcher {
    public void addBooleanChangeListener(BooleanChangeListener listener);
    public boolean getFlag();
    public void setFlag(boolean flag);
}
