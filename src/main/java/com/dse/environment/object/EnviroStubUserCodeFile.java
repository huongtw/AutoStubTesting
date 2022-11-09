package com.dse.environment.object;

import com.dse.util.Utils;

import java.util.List;

/**
 * Example
 * "ENVIRO.STUB_USER_CODE_FILE:
 *
 * BEGINNING_OF_STUB.b.b
 * printf( " Configure Stubs | Beginning of Stub for Unit b, Subprogram b {\n" );
 * END_BEGINNING_OF_STUB.b.b
 *
 * END_OF_STUB.b.b
 * printf( " } Configure Stubs | End of Stub for Unit b, Subprogram b\n\n" );
 * END_END_OF_STUB.b.b
 *
 * BEGINNING_OF_STUB.c.c
 * printf( " Configure Stubs | Beginning of Stub for Unit c, Subprogram c {\n" );
 * END_BEGINNING_OF_STUB.c.c
 *
 * END_OF_STUB.c.c
 * printf( " } Configure Stubs | End of Stub for Unit c, Subprogram c\n\n" );
 * END_END_OF_STUB.c.c
 *
 * ENVIRO.END_STUB_USER_CODE_FILE:"
 */
public class EnviroStubUserCodeFile extends AbstractEnvironmentNode implements ICommandList {
    public void analyzeBlock(List<String> block) {
        int currentLineIndex = 0;
        while (currentLineIndex < block.size()) {
            String commandLine = block.get(currentLineIndex).trim();

            if (commandLine.startsWith(BEGINNING_OF_STUB)) {
                List<String> beginningStub = this.getBlockOfTag(END_BEGINNING_OF_STUB, currentLineIndex, Utils.convertToArray(block));
                BeginningOfStubNode newNode = analyzeBeginningOfSubfunction(beginningStub);
                newNode.setParent(this);
                this.addChild(newNode);

                currentLineIndex += beginningStub.size();

            } else if (commandLine.startsWith(END_OF_STUB)) {
                List<String> endBlock = this.getBlockOfTag(END_END_OF_STUB, currentLineIndex, Utils.convertToArray(block));
                EndStubUserCodeNode newNode = analyzeEndOfSubfunction(endBlock);
                newNode.setParent(this);
                this.addChild(newNode);

                currentLineIndex += endBlock.size();
            } else {
                // nothing to do
                currentLineIndex += 1;
            }
        }
    }


    /**
     * Case:
     * END_OF_STUB.b.b
     * printf( " } Configure Stubs | End of Stub for Unit b, Subprogram b\n\n" );
     * END_END_OF_STUB.b.b
     */
    private EndStubUserCodeNode analyzeEndOfSubfunction(List<String> endStub) {
        // create new node
        EndStubUserCodeNode stub = new EndStubUserCodeNode();

        // get name of unit
        String firstLine = endStub.get(0).trim(); // Example: "END_OF_STUB.b.b"
        firstLine = firstLine.replace(END_OF_STUB, "");
        String nameOfUnit = firstLine.substring(0, firstLine.indexOf(".")); // get the first "b"
        stub.setUnitName(nameOfUnit);

        // get name of subfunction
        String nameOfSubFunction = firstLine.substring(firstLine.indexOf(".") + 1); // get the second "b"
        stub.setSubFunctionName(nameOfSubFunction);

        // get block
        for (int i = 1; i < endStub.size() - 1; i++)
            stub.getBlock().add(endStub.get(i));

        return stub;
    }

    /**
     * Case:
     * BEGINNING_OF_STUB.b.b
     * printf( " Configure Stubs | Beginning of Stub for Unit b, Subprogram b {\n" );
     * END_BEGINNING_OF_STUB.b.b
     */
    private BeginningOfStubNode analyzeBeginningOfSubfunction(List<String> beginningStub) {
        // create new node and add relationship
        BeginningOfStubNode stub = new BeginningOfStubNode();

        // get name of unit
        String firstLine = beginningStub.get(0).trim(); // Example: "BEGINNING_OF_STUB.b.b"
        firstLine = firstLine.replace(BEGINNING_OF_STUB, "");
        String nameOfUnit = firstLine.substring(0, firstLine.indexOf(".")); // get the first "b"
        stub.setUnitName(nameOfUnit);

        // get name of subfunction
        String nameOfSubFunction = firstLine.substring(firstLine.indexOf(".") + 1); // get the second "b"
        stub.setSubFunctionName(nameOfSubFunction);

        // get block
        for (int i = 1; i < beginningStub.size() - 1; i++)
            stub.getBlock().add(beginningStub.get(i));

        return stub;
    }

    @Override
    public String exportToFile() {
        StringBuilder output = new StringBuilder(ENVIRO_STUB_USER_CODE_FILE + "\n");

        for (IEnvironmentNode child: getChildren())
            output.append(child.exportToFile()).append("\n");

        output.append(ENVIRO_END_STUB_USER_CODE_FILE);
        return output.toString();
    }
}
