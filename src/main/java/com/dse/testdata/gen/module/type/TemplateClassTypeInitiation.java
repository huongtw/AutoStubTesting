package com.dse.testdata.gen.module.type;

import com.dse.parser.object.ExternalVariableNode;
import com.dse.parser.object.INode;
import com.dse.parser.object.STLTypeNode;
import com.dse.parser.object.VariableNode;
import com.dse.testdata.object.DataNode;
import com.dse.testdata.object.TemplateClassDataNode;
import com.dse.testdata.object.ValueDataNode;
import com.dse.util.TemplateUtils;
import com.dse.util.VariableTypeUtilsForStd;

/**
 * Khoi tao bien truyen vao la kieu structure
 */
public class TemplateClassTypeInitiation extends AbstractTypeInitiation {
    public TemplateClassTypeInitiation(VariableNode vParent, DataNode nParent) throws Exception {
        super(vParent, nParent);
    }

    @Override
    public ValueDataNode execute() throws Exception {
        TemplateClassDataNode child = new TemplateClassDataNode();

//        String templateName = VariableTypeUtils.getTemplateName(vParent);
        String type = "";

//        if (vParent.getAST() == null) {
            type = vParent.getRealType();
//        } else if (vParent.getAST() instanceof IASTParameterDeclaration) {
//            IASTParameterDeclaration parameterDeclaration = (IASTParameterDeclaration) vParent.getAST();
//            IASTDeclSpecifier declSpecifier = parameterDeclaration.getDeclSpecifier();
//            if (declSpecifier instanceof IASTNamedTypeSpecifier)
//                type = ((IASTNamedTypeSpecifier) declSpecifier).getName().getRawSignature();
//        } else {
//            type = vParent.getRawType();
//        }

//        IASTParameterDeclaration parameterDeclaration = (IASTParameterDeclaration) vParent.getAST();
//
//        if (parameterDeclaration == null) {
//            type = vParent.getRawType();
//            String[] elements = type.split("::");
//            String simpleType = elements[elements.length - 1];
//            templateName = simpleType.substring(0, simpleType.indexOf('<'));
//        } else {
//            IASTDeclSpecifier declSpecifier = parameterDeclaration.getDeclSpecifier();
//
//            if (declSpecifier instanceof IASTNamedTypeSpecifier) {
//                IASTName name = ((IASTNamedTypeSpecifier) declSpecifier).getName();
//                type = name.getRawSignature();
//                if (name instanceof ICPPASTQualifiedName)
//                    name = name.getLastName();
//                if (name instanceof ICPPASTTemplateId)
//                    templateName = ((ICPPASTTemplateId) name).getTemplateName().toString();
//            }
//        }

        INode coreTypeNode = vParent.resolveCoreType();

        if (coreTypeNode == null || coreTypeNode instanceof STLTypeNode) {
            if (VariableTypeUtilsForStd.isSTL(type) || coreTypeNode instanceof STLTypeNode)
                return new STLTypeInitiation(vParent, nParent, type).execute();
            else
                return new ProblemTypeInitiation(vParent, nParent).execute();
        }

        child.setParent(nParent);
//        child.setType(vParent.getFullType());
        child.setRawType(TemplateUtils.getTemplateFullRawType(vParent));
        child.setRealType(vParent.getRealType());
        child.setName(vParent.getNewType());
        child.setCorrespondingVar(vParent);

        if (vParent instanceof ExternalVariableNode)
            child.setExternel(true);

        nParent.addChild(child);
        return child;
    }


//    private boolean isSTLType(String typeName) {
//        List<Level> spaces = new VariableSearchingSpace(vParent).getSpaces();
//
//        for (Level l : spaces) {
//            for (INode n : l) {
//                List<INode> includeHeaders = Search.searchNodes(n, new IncludeHeaderNodeCondition());
//                for (INode includeHeader : includeHeaders) {
//                    String includeStatement = normalize(includeHeader.toString());
//                    String buildIncludeStm = "#include <" + typeName + ">";
//                    if (includeStatement.equals(buildIncludeStm))
//                        return true;
//                }
//            }
//        }
//
//        return false;
//    }

    private String normalize(String statement) {
        String normalized = statement + "";
        while (normalized.contains("  "))
            normalized = normalized.replace("  ", " ");
        while (normalized.contains("< "))
            normalized = normalized.replace("< ", "<");
        while (normalized.contains(" >"))
            normalized = normalized.replace(" >", ">");
        return normalized;
    }
}
