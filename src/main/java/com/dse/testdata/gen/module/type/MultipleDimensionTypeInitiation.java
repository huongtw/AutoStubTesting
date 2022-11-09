package com.dse.testdata.gen.module.type;

import com.dse.parser.dependency.finder.Level;
import com.dse.parser.dependency.finder.VariableSearchingSpace;
import com.dse.parser.object.ExternalVariableNode;
import com.dse.parser.object.INode;
import com.dse.parser.object.MacroDefinitionNode;
import com.dse.parser.object.VariableNode;
import com.dse.search.Search;
import com.dse.search.condition.MacroDefinitionNodeCondition;
import com.dse.testdata.gen.module.TreeExpander;
import com.dse.testdata.object.*;
import com.dse.util.IRegex;
import com.dse.util.TemplateUtils;
import com.dse.util.Utils;
import com.dse.util.VariableTypeUtils;
import org.eclipse.cdt.core.dom.ast.IASTIdExpression;
import org.eclipse.cdt.core.dom.ast.IASTLiteralExpression;
import org.eclipse.cdt.core.dom.ast.IASTNode;

import java.io.File;
import java.util.List;


/**
 * Khoi tao bien dau vao la kieu mang 2 chieu
 */
public class MultipleDimensionTypeInitiation extends AbstractTypeInitiation {
    public MultipleDimensionTypeInitiation(VariableNode vParent, DataNode nParent) throws Exception {
        super(vParent, nParent);
    }

    @Override
    public ValueDataNode execute() throws Exception {
        String coreType = VariableTypeUtils.getSimpleRealType(vParent)
//                .replaceAll(IRegex.POINTER, "")
                .replaceAll(IRegex.ARRAY_INDEX, "");

        MultipleDimensionDataNode child;
        if (VariableTypeUtils.isPointer(coreType))
            child = new MultipleDimensionPointerDataNode();
        else if (VariableTypeUtils.isCh(coreType))
            child = new MultipleDimensionCharacterDataNode();
        else if (VariableTypeUtils.isNum(coreType))
            child = new MultipleDimensionNumberDataNode();
        else if (VariableTypeUtils.isStr(coreType))
            child = new MultipleDimensionStringDataNode();
        else
            child = new MultipleDimensionStructureDataNode();

        child.setParent(nParent);
        child.setRawType(vParent.getRawType());
        child.setRealType(vParent.getRealType());
        child.setName(vParent.getNewType());
        child.setCorrespondingVar(vParent);
        setSizeOf(child);

        if (vParent instanceof ExternalVariableNode)
            child.setExternel(true);

        nParent.addChild(child);
        return  child;
    }

    /**
     * Set size of the Two Dimension Data Node
     *
     * @param node
     * @throws Exception
     */
    private void setSizeOf(MultipleDimensionDataNode node) throws Exception {
        String type = node.getRawType();

        // Remove template arguments
        type = TemplateUtils.deleteTemplateParameters(type);
        type = VariableTypeUtils.removeRedundantKeyword(type);

        List<String> sizesInString = Utils.getIndexOfArray(type);

        int dimensions = sizesInString.size();

        int[] sizes = new int[dimensions];

        for (int i = 0; i < dimensions; i++) {
            String sizeInString = sizesInString.get(i);
            IASTNode astArraySize = Utils.convertToIAST(sizeInString);

            if (astArraySize instanceof IASTLiteralExpression)
                sizes[i] = Integer.parseInt(sizeInString);

            else if (astArraySize instanceof IASTIdExpression) {
                VariableSearchingSpace searchingSpace = new VariableSearchingSpace(vParent);
                List<Level> space = searchingSpace.getSpaces();
                String path = File.separator + sizeInString;
                List<INode> macroNodes = Search.searchInSpace(space, new MacroDefinitionNodeCondition(), path);
                if (!macroNodes.isEmpty()) {
                    MacroDefinitionNode macroNode = (MacroDefinitionNode) macroNodes.get(0);
                    sizes[i] = Integer.parseInt(macroNode.getOldType());
                }

            } else
                sizes[i] = -1;
        }

        node.setDimensions(dimensions);
        node.setSizes(sizes);

        if (sizes[0] > 0) {
            node.setFixedSize(true);
            node.setSizeIsSet(true);
            (new TreeExpander()).expandTree(node);
        }
    }

//    private boolean isNumeric(String strNum) {
//        Pattern pattern = Pattern.compile("-?\\d+(\\.\\d+)?");
//
//        if (strNum == null || strNum.length() == 0)
//            return false;
//
//        return pattern.matcher(strNum).matches();
//    }
}
