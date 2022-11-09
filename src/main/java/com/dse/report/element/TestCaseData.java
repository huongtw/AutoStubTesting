package com.dse.report.element;

import com.dse.report.converter.DataTreeConverter;
import com.dse.search.Search2;
import com.dse.testcase_manager.TestCase;
import com.dse.testdata.object.*;

import java.io.File;
import java.util.List;

import static com.dse.testdata.object.IUserCodeNode.DEFAULT_USER_CODE;

public class TestCaseData extends Section {
    private final String NO_SOURCE_FILE = "No Source File Exist";

    private TestCase testCase;

    public TestCaseData(TestCase testCase) {
        super(testCase.getName() + "-data");
        this.testCase = testCase;
        generate();
    }

    public void generate() {
        Line sectionTitle = new Line("Test Case Data", COLOR.MEDIUM);
        title.add(sectionTitle);

        generateBody();
    }

    private void generateBody() {
        String testCaseName = testCase.getName();
        String fileName = testCase.getSourceCodeFile();
        if (fileName == null)
            fileName = NO_SOURCE_FILE;
        else
            fileName = new File(fileName).getName();

        Table common = new Table();
        common.getRows().add(new Table.Row("Test Case:", testCaseName));
        common.getRows().add(new Table.Row("File Name:", fileName));
        body.add(common);

        Table dataTable = DataTreeConverter.execute(testCase.getRootDataNode());
        body.add(dataTable);

        List<ValueDataNode> unresolved = Search2.searchNodes(testCase.getRootDataNode(),
                VoidPointerDataNode.class, OtherUnresolvedDataNode.class);
        unresolved.removeIf(node -> {
            UnresolvedDataNode cast = (UnresolvedDataNode) node;
            return cast.getUserCode().equals(cast.generateInitialUserCode() + DEFAULT_USER_CODE);
        });

        if (!unresolved.isEmpty()) {
            Line dataTitle = new CenteredLine("Parameter User Code", COLOR.MEDIUM);
            body.add(dataTitle);

            Table userCode = new Table(false);
            for (IDataNode parameter : unresolved) {
                UnresolvedDataNode cast = (UnresolvedDataNode) parameter;
                userCode.getRows().add(new Table.Row(cast.getDisplayNameInParameterTree(), cast.getUserCode().getContent()));
            }
            body.add(userCode);
        }

        body.add(new Section.BlankLine());
    }
}
