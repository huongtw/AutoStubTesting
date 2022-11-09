package com.dse.parser.normalizer;

import auto_testcase_generation.cfg.testpath.INormalizedTestpath;
import auto_testcase_generation.cfg.testpath.ITestpathInCFG;

public interface ITestpathNormalizer extends INormalizer {

    ITestpathInCFG getOriginalTestpath();

    void setOriginalTestpath(ITestpathInCFG tp);

    INormalizedTestpath getNormalizedTestpath();
}
