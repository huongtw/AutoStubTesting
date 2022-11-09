package com.dse.environment.object;

public class EndStubUserCodeNode extends StubUserCodeNode {

    @Override
    public String exportToFile() {
        /*
          END_OF_STUB.b.b

          printf( " } Configure Stubs | End of Stub for Unit b, Subprogram b\n\n" );

          END_END_OF_STUB.b.b
         */
        StringBuilder output;
        output = new StringBuilder(END_OF_STUB + getUnitName() + "." + getSubFunctionName() + "\n");
        for (String line : getBlock())
            output.append(line).append("\n");
        output.append(END_END_OF_STUB).append(getUnitName()).append(".").append(getSubFunctionName());
        return output.toString();
    }
}
