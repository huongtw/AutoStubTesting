package com.dse.environment.object;

public class BeginningOfStubNode extends StubUserCodeNode implements ICommandList {

    @Override
    public String exportToFile() {
    /*
      BEGINNING_OF_STUB.b.b
      printf( " Configure Stubs | Beginning of Stub for Unit b, Subprogram b {\n" );
      END_BEGINNING_OF_STUB.b.b
     */
        StringBuilder output;
        output = new StringBuilder(BEGINNING_OF_STUB + getUnitName() + "." + getSubFunctionName() + "\n");
        for (String line : getBlock())
            output.append(line).append("\n");
        output.append(END_BEGINNING_OF_STUB).append(getUnitName()).append(".").append(getSubFunctionName());
        return output.toString();
    }
}
