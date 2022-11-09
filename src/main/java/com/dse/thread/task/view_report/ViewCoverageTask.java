package com.dse.thread.task.view_report;

import com.dse.coverage.CoverageManager;
import com.dse.coverage.CoverageDataObject;
import com.dse.environment.Environment;
import com.dse.environment.object.EnviroCoverageTypeNode;
import com.dse.testcase_manager.TestCase;
import com.dse.thread.AbstractAkaTask;

import java.util.ArrayList;
import java.util.List;

public class ViewCoverageTask extends AbstractAkaTask<List<CoverageDataObject>> {

    private final List<TestCase> testCases;

    public ViewCoverageTask(List<TestCase> testCases) {
        this.testCases = testCases;
    }

    @Override
    protected List<CoverageDataObject> call() throws Exception {
        List<CoverageDataObject> out = new ArrayList<>();

        switch (Environment.getInstance().getTypeofCoverage()) {
            case EnviroCoverageTypeNode.BASIS_PATH:
            case EnviroCoverageTypeNode.BRANCH:
            case EnviroCoverageTypeNode.STATEMENT:
            case EnviroCoverageTypeNode.MCDC: {
                // compute coverage at file level
                String coverageType = Environment.getInstance().getTypeofCoverage();
                CoverageDataObject coverageDataObject = CoverageManager
                        .getCoverageOfMultiTestCaseAtSourcecodeFileLevel(testCases, coverageType);
                out.add(coverageDataObject);
                CoverageDataObject funcCovDataObject = CoverageManager
                        .getCoverageOfMultiTestCaseAtFunctionLevel(testCases, coverageType);
                out.add(funcCovDataObject);
                break;
            }

            case EnviroCoverageTypeNode.STATEMENT_AND_BRANCH: {
                // tab coverage 1
                CoverageDataObject coverageDataObject = CoverageManager
                        .getCoverageOfMultiTestCaseAtSourcecodeFileLevel(testCases,
                        EnviroCoverageTypeNode.STATEMENT);
                out.add(coverageDataObject);
                // tab coverage 2
                CoverageDataObject coverageDataObjectInTab2 = CoverageManager
                        .getCoverageOfMultiTestCaseAtSourcecodeFileLevel(testCases,
                        EnviroCoverageTypeNode.BRANCH);
                out.add(coverageDataObjectInTab2);

                // tab coverage 1
                CoverageDataObject funcCovDataObject = CoverageManager
                        .getCoverageOfMultiTestCaseAtFunctionLevel(testCases,
                                EnviroCoverageTypeNode.STATEMENT);
                out.add(funcCovDataObject);
                // tab coverage 2
                CoverageDataObject funcCovDataObjectInTab2 = CoverageManager
                        .getCoverageOfMultiTestCaseAtFunctionLevel(testCases,
                                EnviroCoverageTypeNode.BRANCH);
                out.add(funcCovDataObjectInTab2);
                break;
            }

            case EnviroCoverageTypeNode.STATEMENT_AND_MCDC: {
                // tab coverage 1
                CoverageDataObject coverageDataObject = CoverageManager
                        .getCoverageOfMultiTestCaseAtSourcecodeFileLevel(testCases,
                        EnviroCoverageTypeNode.STATEMENT);
                if (coverageDataObject != null) {
                    out.add(coverageDataObject);
                }

                // tab coverage 2
                CoverageDataObject coverageDataObjectInTab2 = CoverageManager
                        .getCoverageOfMultiTestCaseAtSourcecodeFileLevel(testCases,
                        EnviroCoverageTypeNode.MCDC);
                if (coverageDataObjectInTab2 != null) {
                    out.add(coverageDataObjectInTab2);
                }

                // tab coverage 1
                CoverageDataObject funcCovDataObject = CoverageManager
                        .getCoverageOfMultiTestCaseAtFunctionLevel(testCases,
                                EnviroCoverageTypeNode.STATEMENT);
                out.add(funcCovDataObject);
                // tab coverage 2
                CoverageDataObject funcCovDataObjectInTab2 = CoverageManager
                        .getCoverageOfMultiTestCaseAtFunctionLevel(testCases,
                                EnviroCoverageTypeNode.MCDC);
                out.add(funcCovDataObjectInTab2);
                break;
            }
        }

        return out;
    }
}
