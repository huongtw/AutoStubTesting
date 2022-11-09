package com.dse.project_init;

import com.dse.config.WorkspaceConfig;
import com.dse.environment.Environment;
import com.dse.environment.EnvironmentSearch;
import com.dse.environment.object.EnviroLibraryStubNode;
import com.dse.environment.object.IEnvironmentNode;
import com.dse.parser.dependency.Dependency;
import com.dse.parser.dependency.FunctionCallDependency;
import com.dse.parser.object.*;
import com.dse.search.Search;
import com.dse.search.condition.FunctionNodeCondition;
import com.dse.search.condition.SourcecodeFileNodeCondition;
import com.dse.stub_manager.StubManager;
import com.dse.stub_manager.SystemLibrary;
import com.dse.util.SourceConstant;
import com.dse.util.SpecialCharacter;
import com.dse.util.Utils;
import com.dse.util.VariableTypeUtils;
import org.eclipse.cdt.core.dom.ast.IASTNode;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

public class LcovProjectClone extends ProjectClone {

    public static final String LCOV_CLONED_FILE_EXTENSION = ".lcov.akaignore";

    public static void cloneLcovEnvironment() {
        List<IEnvironmentNode> stubLibraries = EnvironmentSearch
                .searchNode(Environment.getInstance().getEnvironmentRootNode(), new EnviroLibraryStubNode());

        List<INode> stubUnits = Environment.getInstance().getStubs();
        List<INode> sbfUnits = Environment.getInstance().getSBFs();
        List<INode> uutUnits = Environment.getInstance().getUUTs();

        List<String> libraries = getLibraries();

        boolean onWhiteBoxMode = Environment.getInstance().isOnWhiteBoxMode();


        ProjectNode projectRoot = Environment.getInstance().getProjectNode();
        List<INode> sources = Search.searchNodes(projectRoot, new SourcecodeFileNodeCondition());
        sources.removeIf(source -> libraries.contains(source.getAbsolutePath()));

        for (INode sourceCode : sources) {
            LcovProjectClone lcovClone = new LcovProjectClone();

            lcovClone.libraries = libraries;
            lcovClone.whiteBoxEnable = (uutUnits.contains(sourceCode) || sbfUnits.contains(sourceCode)) && onWhiteBoxMode;
            lcovClone.canStub = stubUnits.contains(sourceCode) || sbfUnits.contains(sourceCode);
            lcovClone.stubLibraries = stubLibraries;

            try {
                String newContent = lcovClone.generateFileContent(sourceCode);
                Utils.writeContentToFile(newContent, getLcovClonedFilePath(sourceCode.getAbsolutePath()));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    protected void refactorFunction(ICommonFunctionNode function) {
        if (function instanceof AbstractFunctionNode || function instanceof MacroFunctionNode) {
            IASTNode functionAST;
            if (function instanceof AbstractFunctionNode) {
                functionAST = ((AbstractFunctionNode) function).getAST();
            } else {
                functionAST = ((MacroFunctionNode) function).getAST();
            }
            String oldFunctionCode = functionAST.getRawSignature();

            // generate instrumented function content
            String newFunctionCode = oldFunctionCode;

            // include stub code in function scope
            int bodyIdx = newFunctionCode.indexOf(SpecialCharacter.OPEN_BRACE) + 1;
            if (bodyIdx > 1) {
                newFunctionCode = newFunctionCode.substring(0, bodyIdx)
                        + SourceConstant.INCREASE_FCALLS + newFunctionCode.substring(bodyIdx);
                ; //generateCallingMark(function.getAbsolutePath()) String.format(IGTestConstant.INCREASE_FCALLS)
            }
            if (function instanceof FunctionNode && canStub) {
                newFunctionCode = includeStubFile(newFunctionCode, (FunctionNode) function);
                StubManager.initializeStubCode((FunctionNode) function);
            }

            // modify all function call expression to stub libraries
            for (IEnvironmentNode stubLibrary : stubLibraries) {
                if (stubLibrary instanceof EnviroLibraryStubNode) {
                    Map<String, String> libraries = ((EnviroLibraryStubNode) stubLibrary).getLibraries();

                    for (String libraryName : libraries.keySet()) {
                        if (!libraryName.matches("[a-zA-Z0-9_]+") && libraryName.indexOf('(') != -1) {
                            libraryName = libraryName.substring(0, libraryName.indexOf('(')).trim();
                            libraryName = libraryName.substring(libraryName.lastIndexOf(' ') + 1);
                        }
                        if (SystemLibrary.isUseLibrary(libraryName, function)) {
                            String newLibraryCall = SourceConstant.STUB_PREFIX + libraryName;

                            newFunctionCode = newFunctionCode
                                    .replaceAll("\\b" + Pattern.quote(VariableTypeUtils.STD_SCOPE + libraryName) + "\\b", newLibraryCall);

                            newFunctionCode = newFunctionCode
                                    .replaceAll("\\b" + Pattern.quote(libraryName) + "\\b", newLibraryCall);

                            // update function call dependency
                            if (newFunctionCode.contains(newLibraryCall)) {
                                List<INode> possibles = Search.searchNodes(
                                        Environment.getInstance().getSystemLibraryRoot(),
                                        new FunctionNodeCondition()
                                );

                                possibles.removeIf(f -> !((IFunctionNode) f).getSimpleName().equals(newLibraryCall));

                                if (!possibles.isEmpty()) {
                                    INode libraryFunction = possibles.get(0);
                                    Dependency d = new FunctionCallDependency(function, libraryFunction);

                                    if (!function.getDependencies().contains(d)
                                            && !libraryFunction.getDependencies().contains(d)) {
                                        function.getDependencies().add(d);
                                        libraryFunction.getDependencies().add(d);
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // change main function to AKA_MAIN
            if (function.getSingleSimpleName().equals("main")) {
                newFunctionCode = newFunctionCode.replaceAll(MAIN_REGEX, MAIN_REFACTOR_NAME);
            }

            refactors.put(oldFunctionCode, newFunctionCode);
        }
    }

    /**
     * Ex: Utils.cpp -> Utils.lcov.akaignore.cpp
     *
     * @param origin file path
     * @return cloned file path
     */
    public static String getLcovClonedFilePath(String origin) {
        Path rootPath = Paths.get(Environment.getInstance().getProjectNode().getAbsolutePath());
        Path originPath = Paths.get(origin);
        String relativePath = rootPath.relativize(originPath).toString();
        int lastDotPos = relativePath.lastIndexOf(SpecialCharacter.DOT);
        String clonedName = relativePath.substring(0, lastDotPos)
                + LCOV_CLONED_FILE_EXTENSION
                + relativePath.substring(lastDotPos);
        return new WorkspaceConfig().fromJson().getInstrumentDirectory() + File.separator + clonedName;
    }

    @Override
    protected String getCorrespondingClonePath(String path) {
        return getLcovClonedFilePath(path);
    }
}


