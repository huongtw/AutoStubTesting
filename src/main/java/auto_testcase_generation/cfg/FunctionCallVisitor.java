package auto_testcase_generation.cfg;

import com.dse.parser.ProjectParser;
import com.dse.parser.dependency.finder.MethodFinder;
import com.dse.parser.object.IFunctionNode;
import com.dse.parser.object.INode;
import com.dse.search.Search;
import com.dse.search.condition.FunctionNodeCondition;
import com.dse.util.VariableTypeUtils;
import org.eclipse.cdt.core.dom.ast.*;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

public class FunctionCallVisitor extends ASTVisitor {

    public static void main(String[] args) {
        ProjectParser parser = new ProjectParser(new File("datatest/lamnt/test"));
        parser.setExpandTreeuptoMethodLevel_enabled(true);
        parser.setCpptoHeaderDependencyGeneration_enabled(true);
        parser.setParentReconstructor_enabled(true);
        parser.setFuncCallDependencyGeneration_enabled(true);
        parser.setSizeOfDependencyGeneration_enabled(true);
        parser.setGlobalVarDependencyGeneration_enabled(true);
        parser.setTypeDependency_enable(true);

        List<IFunctionNode> functions = Search
                .searchNodes(parser.getRootTree(), new FunctionNodeCondition(), "main()");

        IFunctionNode function = functions.get(0);

        FunctionCallVisitor visitor = new FunctionCallVisitor(function);
        function.getAST().accept(visitor);

        CallMap map = visitor.getCallMap();

        System.out.println();
    }

    private final IFunctionNode context;

    private final CallMap map = new CallMap();

    public FunctionCallVisitor(IFunctionNode context) {
        shouldVisitExpressions = true;
        this.context = context;
    }

    @Override
    public int visit(IASTExpression expr) {
        if (expr instanceof IASTFunctionCallExpression && !map.containsKey(expr)) {
            IASTFunctionCallExpression functionCallExpr = (IASTFunctionCallExpression) expr;
            if (!isStdFunction(functionCallExpr.getFunctionNameExpression()))
                handle(functionCallExpr);
        }

        return PROCESS_CONTINUE;
    }

    private boolean isStdFunction(IASTExpression nameExpr) {
        while (nameExpr instanceof IASTUnaryExpression
                && ((IASTUnaryExpression) nameExpr).getOperator() == IASTUnaryExpression.op_bracketedPrimary) {
            nameExpr = ((IASTUnaryExpression) nameExpr).getOperand();
        }

        return nameExpr.getRawSignature().startsWith(VariableTypeUtils.STD_SCOPE);
    }

    private void handle(IASTFunctionCallExpression functionCallExpr) {
        List<IASTInitializerClause> arguments = Arrays.asList(functionCallExpr.getArguments());
        List<IASTNode> preprocess = new ArrayList<>();
        IASTExpression nameExpr = functionCallExpr.getFunctionNameExpression();
        if (nameExpr instanceof IASTFieldReference) {
            IASTExpression owner = ((IASTFieldReference) nameExpr).getFieldOwner();
            preprocess.add(owner);
        }
        preprocess.addAll(arguments);

        for (IASTNode item : preprocess) {
            FunctionCallVisitor visitor = new FunctionCallVisitor(context);
            item.accept(visitor);

            if (!visitor.map.isEmpty()) {
                this.map.putAll(visitor.map);
            }
        }

        MethodFinder finder = new MethodFinder(context);
        INode node = finder.find(functionCallExpr);

        if (node instanceof IFunctionNode && node != context) {
            IFunctionNode functionNode = (IFunctionNode) node;
            int params = functionNode.getArguments().size();
            int args = functionCallExpr.getArguments().length;
            if (params == args) {
                this.map.put(functionCallExpr, functionNode);
            }
        }
    }

    public CallMap getCallMap() {
        return map;
    }

    private interface ICallMap {
        boolean containsKey(Object key);
        void putAll(CallMap m);
        IFunctionNode get(Object key);
        IFunctionNode put(IASTFunctionCallExpression key, IFunctionNode value);
        Set<IASTFunctionCallExpression> keySet();

        Collection<IFunctionNode> values();

        Set<CallEntry> entrySet();
    }

    private static class CallEntry implements Map.Entry<IASTFunctionCallExpression, IFunctionNode> {

        private final IASTFunctionCallExpression key;
        private IFunctionNode value;

        public CallEntry(IASTFunctionCallExpression key, IFunctionNode value) {
            this.key = key;
            this.value = value;
        }

        @Override
        public IASTFunctionCallExpression getKey() {
            return key;
        }

        @Override
        public IFunctionNode getValue() {
            return value;
        }

        @Override
        public IFunctionNode setValue(IFunctionNode value) {
            this.value = value;
            return this.value;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            CallEntry callEntry = (CallEntry) o;
            return key.equals(callEntry.key) &&
                    Objects.equals(value, callEntry.value);
        }

        @Override
        public int hashCode() {
            return Objects.hash(key, value);
        }
    }

    public static class CallMap extends ArrayList<CallEntry> implements ICallMap {

        @Override
        public boolean containsKey(Object key) {
            return stream().anyMatch(e -> e.getKey().equals(key));
        }

        @Override
        public IFunctionNode get(Object key) {
            return stream()
                    .filter(e -> e.getKey().equals(key))
                    .findFirst()
                    .map(CallEntry::getValue)
                    .orElse(null);
        }

        @Override
        public IFunctionNode put(IASTFunctionCallExpression key, IFunctionNode value) {
            CallEntry entry = new CallEntry(key, value);
            add(entry);
            return value;
        }

        @Override
        public void putAll(CallMap m) {
            m.forEach(e -> {
                CallEntry entry = new CallEntry(e.key, e.value);
                add(entry);
            });
        }

        @Override
        public Set<IASTFunctionCallExpression> keySet() {
            return stream().map(CallEntry::getKey).collect(Collectors.toSet());
        }

        @Override
        public Collection<IFunctionNode> values() {
            return stream().map(CallEntry::getValue).collect(Collectors.toList());
        }

        @Override
        public Set<CallEntry> entrySet() {
            return stream().collect(Collectors.toSet());
        }
    }
}