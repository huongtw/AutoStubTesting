package auto_testcase_generation.testdatagen.se.memory;

import org.eclipse.cdt.core.dom.ast.IASTFunctionCallExpression;

import java.util.*;

public class FunctionCallTable extends HashMap<IASTFunctionCallExpression, String> {

    private static final String FUNCTION_CALL_PREFIX = "_aka_func_call_";

    private int count = 0;

    public String put(IASTFunctionCallExpression key) {
        String name = FUNCTION_CALL_PREFIX + count;
        count++;
        super.put(key, name);
        return name;
    }

    @Override
    public String remove(Object key) {
        if (containsKey(key))
            return super.remove(key);
        else {
            Entry<IASTFunctionCallExpression, String> entry = getCorrespondingEntry(key);
            if (entry != null) {
                key = entry.getKey();
                return super.remove(key);
            }
        }

        return null;
    }

    private Entry<IASTFunctionCallExpression, String> getCorrespondingEntry(Object key) {
        final String functionName;

        if (key instanceof IASTFunctionCallExpression)
            functionName = ((IASTFunctionCallExpression) key)
                    .getFunctionNameExpression()
                    .getRawSignature();
        else
            functionName = key.toString();

        return entrySet()
                .stream()
                .sorted(Collections.reverseOrder())
                .filter(e -> e.getKey().getFunctionNameExpression()
                        .getRawSignature().equals(functionName)
                ).findFirst()
                .orElse(null);
    }

    @Override
    public String get(Object key) {
        String value = super.get(key);

        if (value != null)
            return value;

        Entry<IASTFunctionCallExpression, String> entry = getCorrespondingEntry(key);
        if (entry != null)
            value = entry.getValue();

        return value;
    }
}
