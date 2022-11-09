package com.dse.regression;

import com.dse.parser.object.INode;
import com.dse.regression.objects.Reason;
import com.dse.testcasescript.object.TestNameNode;
import com.dse.testcasescript.object.TestUnitNode;

import java.util.HashMap;
import java.util.Map;

public class ReasonManager {
    private static Map<Object, Reason> reasonMap = new HashMap<>();


    public static String showStatus(Object item) {
        Reason reason = getReason(item);
        if (reason != null) {
            if (item instanceof INode) {
                return ((INode) item).getName() + " is " + reason.getStatusOfItem();
            } else if (item instanceof TestNameNode) {
                return "Test case " + ((TestNameNode) item).getName() + " is " + reason.getStatusOfItem();
            } else if (item instanceof TestUnitNode) {
                return "Unit " + ((TestUnitNode) item).getName() + " is " + reason.getStatusOfItem();
            }
        }
        return "";
    }

    public static String showStatusWithReason(Object item) {
        Reason reason = getReason(item);
        if (reason != null) {
            if (item instanceof INode) {
                if (reason.getSource() != null) {
                    return ((INode) item).getName() + " is " + reason.getStatusOfItem()
                            + " due to " + ReasonManager.showStatus(reason.getSource());
                } else {
                    return ((INode) item).getName() + " is " + reason.getStatusOfItem();
                }
            } else if (item instanceof TestNameNode) {
                if (reason.getSource() != null) {
                    return "Test case " + ((TestNameNode) item).getName() + " is " + reason.getStatusOfItem()
                            + " due to " + ReasonManager.showStatus(reason.getSource());
                } else {
                    return "Test case " + ((TestNameNode) item).getName() + " is " + reason.getStatusOfItem();
                }
            } else if (item instanceof TestUnitNode) {
                return showStatus(item);
            }
        }
        return "";
    }

    // show reason for a item recursively
    public static String getFullReason(Object item) {
        Reason reason = getReason(item);
        if (reason != null) {
            StringBuilder builder = new StringBuilder();
            if (reason.getStatusOfItem().equals(Reason.STATUS_NA) && item instanceof TestNameNode) {
                builder.append(showStatus(item)).append("\n");
                builder.append("Test case ").append(((TestNameNode) item).getName()).append(" has no test path\n");
            } else {
                builder.append(showStatusWithReason(item)).append("\n");
                if (reason.getSource() != null) {
                    builder.append(getFullReason(reason.getSource())).append("\n");
                }
            }

            return builder.toString();
        }
        return "";
    }

    public static void putToReasonMap(Object item, Reason reason) {
        if (! reasonMap.containsKey(item)) {
            reasonMap.put(item, reason);
        }
    }

    public static Reason getReason(Object item) {
        return reasonMap.get(item);
    }

    public static Map<Object, Reason> getReasonMap() {
        return reasonMap;
    }

    public static void clear() {
        reasonMap.clear();
    }
}
