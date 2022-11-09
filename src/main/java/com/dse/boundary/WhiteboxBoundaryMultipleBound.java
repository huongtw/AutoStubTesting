package com.dse.boundary;
import com.dse.parser.object.IVariableNode;
import com.dse.testdata.object.EnumDataNode;
import com.dse.util.Utils;
import com.dse.util.VariableTypeUtils;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTLiteralExpression;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTLiteralExpression;

import java.util.Arrays;
import java.util.List;

public class WhiteboxBoundaryMultipleBound {

    private IVariableNode var;
    private List<String> boundValues;

    public WhiteboxBoundaryMultipleBound(IVariableNode var, List<String> boundValues) {
        this.var = var;
        this.boundValues = boundValues;
    }

    public IVariableNode getVar() {
        return var;
    }

    public List<String> getBoundValues() {
        return boundValues;
    }

    public void setVar(IVariableNode var) {
        this.var = var;
    }

    public void setBoundValues(List<String> boundValues) {
        this.boundValues = boundValues;
    }

    public String getNorm() {
        String norm = "";
        String type = this.var.getCoreType();
        if(VariableTypeUtils.isNumBasicInteger(type) || VariableTypeUtils.isStdInt(type)){
            Long[] array=new Long[this.boundValues.size()];
            int i=0;
            for(String str:this.boundValues){
                try {
                    str = Utils.preprocessorLiteral(str);
                    array[i]=Long.parseLong(str);//Exception in this line
                    i++;
                } catch(NumberFormatException error){
                    try {
                        Double newBound = Double.parseDouble(str);
                        Long   longBound = newBound.longValue();
                        array[i]=longBound;
                        i++;
                    } catch (NumberFormatException err) {
                        IASTNode ast = Utils.convertToIAST(str);
                        if (ast instanceof ICPPASTLiteralExpression) {

                        }
                    }

                }

            }
            Arrays.stream(array).sorted();
            Long normal = (array[0]+array[array.length-1])/2;
            norm = String.valueOf(normal);
        }
        else if(VariableTypeUtils.isNumBasicFloat(type)){
            Double[] array=new Double[this.boundValues.size()];
            int i=0;
            for(String str:this.boundValues){
                str = Utils.preprocessorLiteral(str);
                array[i]=Double.parseDouble(str);//Exception in this line
                i++;
            }
            Arrays.stream(array).sorted();
            Double normal = (array[0]+array[array.length-1])/2;
            norm = String.valueOf(normal);
        }
        else if(VariableTypeUtils.isChBasic(type)){
            Integer[] array=new Integer[this.boundValues.size()];
            int i=0;
            for(String str:this.boundValues){
                str = Utils.preprocessorLiteral(str);
                try {
                    char s = str.charAt(1);//Exception in this line
                    array[i] = (int) s;
                    i++;
                }
                catch (Exception error){
                    int value = Integer.parseInt(str);
                    array[i] = value;
                }

            }
            Arrays.stream(array).sorted();
            Integer normal = (array[0]+array[array.length-1])/2;
            norm = String.valueOf(normal);
        }
        else {
            Long[] array=new Long[this.boundValues.size()];
            int i=0;
            for(String str:this.boundValues){
                str = Utils.preprocessorLiteral(str);
                try {
                    array[i]=Long.parseLong(str);//Exception in this line
                    i++;
                }catch(NumberFormatException error){
                    Double newBound = Double.parseDouble(str);
                    Long   longBound = newBound.longValue();
                    array[i]=longBound;
                    i++;
                }

            }
            Arrays.stream(array).sorted();
            Long normal = (array[0]+array[array.length-1])/2;
            norm = String.valueOf(normal);
        }
//        else if (VariableTypeUtils.isOneDimensionBasic(type)){
//            if (VariableTypeUtils.isNumBasicInteger(type)){
//                Long[] array=new Long[this.boundValues.size()];
//                int i=0;
//                for(String str:this.boundValues){
//                    try {
//                        array[i]=Long.parseLong(str);//Exception in this line
//                        i++;
//                    }catch(NumberFormatException error){
//                        Double newBound = Double.parseDouble(str);
//                        Long   longBound = newBound.longValue();
//                        array[i]=longBound;
//                        i++;
//                    }
//
//                }
//                Arrays.stream(array).sorted();
//                Long normal = (array[0]+array[array.length-1])/2;
//                norm = String.valueOf(normal);
//            }
//            else if (VariableTypeUtils.isNumBasicFloat(type)){
//                Double[] array=new Double[this.boundValues.size()];
//                int i=0;
//                for(String str:this.boundValues){
//                    array[i]=Double.parseDouble(str);//Exception in this line
//                    i++;
//                }
//                Arrays.stream(array).sorted();
//                Double normal = (array[0]+array[array.length-1])/2;
//                norm = String.valueOf(normal);
//            }
//        }
        return norm;
    }

    public String getLast(){
        int size = this.boundValues.size();
        String lastBound = this.boundValues.get(size-1);
        return lastBound;
    }
}
