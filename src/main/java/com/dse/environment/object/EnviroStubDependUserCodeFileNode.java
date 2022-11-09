package com.dse.environment.object;

import java.util.ArrayList;
import java.util.List;

public class EnviroStubDependUserCodeFileNode extends AbstractEnvironmentNode implements ICommandList{

    public void analyzeBlock(List<String> block){
        int currentLineIndex = 0;
        while (currentLineIndex < block.size()) {
            String commandLine = block.get(currentLineIndex).trim();
            switch (commandLine) {
                case BEGIN_Uc: {
                    /*
                      Case:
                      "BEGIN_Uc:
                      b
                      // Configure Stubs | Stub Dependency, Unit b
                      END_Uc:"
                     */
                    // create new node
                    UcNode newUcNode = new UcNode();
                    newUcNode.setParent(this);
                    this.addChild(newUcNode);

                    // the first line is the name of the stub
                    currentLineIndex += 1;
                    String nameOfStub = block.get(currentLineIndex).trim();
                    newUcNode.setName(nameOfStub);

                    // from the second line to the END_Uc line
                    List<String> contentOfUc = new ArrayList<>();
                    boolean foundEndOfUc = false;
                    while (currentLineIndex < block.size() && !foundEndOfUc) {
                        if (block.get(currentLineIndex).trim().equals(END_Uc))
                            foundEndOfUc = true;
                        else {
                            contentOfUc.add(block.get(currentLineIndex));
                        }
                        currentLineIndex++;
                    }
                    newUcNode.setBlock(contentOfUc);
                }
                case ENVIRO_STUB_DEPEND_USER_CODE_FILE: {
                    currentLineIndex++;
                    continue;
                }

                default: {
                    currentLineIndex++;
                    // nothing to do
                }
            }
        }
    }

    @Override
    public String exportToFile() {
        StringBuilder output = new StringBuilder(ENVIRO_STUB_DEPEND_USER_CODE_FILE + "\n");

        for (IEnvironmentNode child: getChildren())
            output.append(child.exportToFile()).append("\n");

        output.append(ENVIRO_END_STUB_DEPEND_USER_CODE_FILE);
        return output.toString();
    }
}
