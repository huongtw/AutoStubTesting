package dependency_generation;

import com.dse.parser.dependency.IncludeHeaderDependencyGeneration;
import com.dse.parser.object.INode;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class IncludeHeaderTest extends AbstractIncludeHeaderTest {
    @Test
    public void test01() {
        INode sourceFile = warm("datatest\\lamnt\\coreutils\\src", "\\basename.c");

        new IncludeHeaderDependencyGeneration().dependencyGeneration(sourceFile);

        String headerPath = "\\system.h";

        assertTrue(findHeaderNode(sourceFile, headerPath) != null);
    }

    @Test
    public void test02() {
        INode sourceFile = warm("datatest\\lamnt\\coreutils\\src", "\\basenc.c");

        new IncludeHeaderDependencyGeneration().dependencyGeneration(sourceFile);

        String headerPath = "\\die.h";

        assertEquals(findHeaderNode(sourceFile, headerPath) != null, true);
    }

    @Test
    public void test03() {
        INode sourceFile = warm("datatest\\lamnt\\coreutils\\src", "\\md5sum.c");

        new IncludeHeaderDependencyGeneration().dependencyGeneration(sourceFile);

        String headerPath = "\\blake2\\b2sum.h";

        assertEquals(findHeaderNode(sourceFile, headerPath) != null, true);
    }

    @Test
    public void test04() {
        INode sourceFile = warm("datatest\\lamnt\\coreutils\\src", "\\blake2\\b2sum.c");

        new IncludeHeaderDependencyGeneration().dependencyGeneration(sourceFile);

        String headerPath = "\\blake2\\blake2.h";

        assertEquals(findHeaderNode(sourceFile, headerPath) != null, true);
    }

    @Test
    public void test05() {
        INode sourceFile = warm("datatest\\lamnt\\coreutils\\src", "\\blake2\\blake2b-ref.c");

        new IncludeHeaderDependencyGeneration().dependencyGeneration(sourceFile);

        String headerPath = "\\blake2\\blake2-impl.h";

        assertEquals(findHeaderNode(sourceFile, headerPath) != null, true);
    }

    @Test
    public void test06() {
        INode sourceFile = warm("datatest\\lamnt\\coreutils\\src", "\\cat.c");

        new IncludeHeaderDependencyGeneration().dependencyGeneration(sourceFile);

        String headerPath = "\\ioblksize.h";

        assertEquals(findHeaderNode(sourceFile, headerPath) != null, true);
    }

    @Test
    public void test08() {
        INode sourceFile = warm("datatest\\lamnt\\coreutils\\src", "\\chcon.c");

        new IncludeHeaderDependencyGeneration().dependencyGeneration(sourceFile);

        String headerPath = "\\die.h";

        assertEquals(findHeaderNode(sourceFile, headerPath) != null, true);
    }

    @Test
    public void test09() {
        INode sourceFile = warm("datatest\\lamnt\\coreutils\\src", "\\chgrp.c");

        new IncludeHeaderDependencyGeneration().dependencyGeneration(sourceFile);

        String headerPath = "\\chown-core.h";

        assertEquals(findHeaderNode(sourceFile, headerPath) != null, true);
    }

    @Test
    public void test10() {
        INode sourceFile = warm("datatest\\lamnt\\coreutils\\src", "\\copy.c");

        new IncludeHeaderDependencyGeneration().dependencyGeneration(sourceFile);

        String headerPath = "\\extent-scan.h";

        assertEquals(findHeaderNode(sourceFile, headerPath) != null, true);
    }

    @Test
    public void test12() {
        INode sourceFile = warm("datatest\\lamnt\\cunit\\CUnit", "\\Headers\\CUnit.h");

        new IncludeHeaderDependencyGeneration().dependencyGeneration(sourceFile);

        String headerPath = "\\Headers\\TestDB.h";

        assertEquals(findHeaderNode(sourceFile, headerPath) != null, true);
    }

    @Test
    public void test13() {
        INode sourceFile = warm("datatest\\duc-anh\\IncludeHeaderDependencyGeneration", "main.cpp");

        new IncludeHeaderDependencyGeneration().dependencyGeneration(sourceFile);

        String headerPath = "autumn.h";

        assertEquals(findHeaderNode(sourceFile, headerPath) != null, true);
    }

    @Test
    public void test14() {
        INode sourceFile = warm("datatest\\duc-anh\\IncludeHeaderDependencyGeneration", "autumn.h");

        new IncludeHeaderDependencyGeneration().dependencyGeneration(sourceFile);

        String headerPath = "winter.h";

        assertEquals(findHeaderNode(sourceFile, headerPath) != null, true);
    }

    @Test
    public void test15() {
        INode sourceFile = warm("datatest\\duc-anh\\IncludeHeaderDependencyGeneration", "autumn.cpp");

        new IncludeHeaderDependencyGeneration().dependencyGeneration(sourceFile);

        String headerPath = "winter.h";

        assertEquals(findHeaderNode(sourceFile, headerPath) != null, true);
    }

    @Test
    public void test16() {
        INode sourceFile = warm("datatest\\duc-anh\\IncludeHeaderDependencyGeneration", "autumn.cpp");

        new IncludeHeaderDependencyGeneration().dependencyGeneration(sourceFile);

        String headerPath = "autumn.h";

        assertEquals(findHeaderNode(sourceFile, headerPath) != null, true);
    }

    @Test
    public void test17() {
        INode sourceFile = warm("datatest\\lamnt\\mysample", "class\\classMethodInDiffFile.cpp");

        new IncludeHeaderDependencyGeneration().dependencyGeneration(sourceFile);

        String headerPath = "Person.h";

        assertEquals(findHeaderNode(sourceFile, headerPath) != null, true);
    }
}