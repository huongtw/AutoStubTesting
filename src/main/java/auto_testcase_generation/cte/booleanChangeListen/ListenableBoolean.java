package auto_testcase_generation.cte.booleanChangeListen;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class ListenableBoolean implements BooleanChangeDispatcher{
    public static void main(String[] args) {

        BooleanChangeListener listener = new BooleanChangeListener() {
            @Override
            public void stateChanged(BooleanChangeEvent event) {
                if(event.getDispatcher().getFlag() == true) {
                    System.out.println("Detected change to: "
                            + event.getDispatcher().getFlag()
                            + " -- event: " + event);
                    event.getDispatcher().setFlag(false);
                }
            }
        };

        ListenableBoolean test = new ListenableBoolean(false);
        test.addBooleanChangeListener(listener);

        test.setFlag(false); // no change, no event dispatch
        test.setFlag(true); // changed to true -- event dispatched
        //test.setFlag(false);
        test.setFlag(true);

    }

    private boolean flag;
    private List<BooleanChangeListener> listeners;

    public ListenableBoolean(boolean initialFlagState) {
        flag = initialFlagState;
        listeners = new ArrayList<BooleanChangeListener>();
    }

    @Override
    public void addBooleanChangeListener(BooleanChangeListener listener) {
        listeners.add(listener);
    }

    @Override
    public void setFlag(boolean flag) {
        if (this.flag != flag) {
            this.flag = flag;
            dispatchEvent();
        }
    }

    @Override
    public boolean getFlag() {
        return flag;
    }

    private void dispatchEvent() {
        final BooleanChangeEvent event = new BooleanChangeEvent(this);
        for (BooleanChangeListener l : listeners) {
            dispatchRunnableOnEventQueue(l, event);
        }
    }

    private void dispatchRunnableOnEventQueue(
            final BooleanChangeListener listener,
            final BooleanChangeEvent event) {

        EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                listener.stateChanged(event);
            }
        });
    }
}
