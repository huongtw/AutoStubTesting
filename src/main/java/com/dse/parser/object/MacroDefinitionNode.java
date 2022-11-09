package com.dse.parser.object;

import org.eclipse.cdt.core.dom.ast.IASTPreprocessorObjectStyleMacroDefinition;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IMacroBinding;

/**
 * Ex1: <b>#define MIN 100</b>
 *
 * @author ducanhnguyen
 */
public class MacroDefinitionNode extends PreprocessorObjectStyleMacroDefinitionNode implements ITypedefDeclaration {

    @Override
    public void setAST(IASTPreprocessorObjectStyleMacroDefinition aST) {
        super.setAST(aST);
        setName(getNewType());
    }

    @Override
    public String getOldType() {
        IBinding binding = getAST().getName().getBinding();
        String name = binding.getName();
        if (binding instanceof IMacroBinding) {
            name = new String(((IMacroBinding) binding).getExpansion());
        }
        return name;
    }
}
