package dependency_generation;

import com.dse.parser.dependency.FunctionCallDependencyGeneration;
import com.dse.parser.object.IFunctionNode;
import org.apache.log4j.Logger;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertEquals;


public class FunctionCallDependencyGenerationDiffNameCaseTest extends AbstractFunctionCallTest {
    final static Logger logger = Logger.getLogger(FunctionCallDependencyGenerationDiffNameCaseTest.class);

    @Test
    //@Ignore
    public void test01() throws IOException {
        IFunctionNode functionNode = warm("datatest\\lamnt\\coreutils\\src",
                "\\cat.c\\usage(int)");

        new FunctionCallDependencyGeneration().dependencyGeneration(functionNode);

        String calledPath = "\\system.h\\emit_ancillary_info(char const*)";

        assertEquals(findCalledFunctions(functionNode, calledPath).size(), 1);
    }

    @Test
    //@Ignore
    public void test02() throws IOException {
        IFunctionNode functionNode = warm("datatest\\lamnt\\coreutils\\src",
                "\\chown.c\\main(int,char**)");

        new FunctionCallDependencyGeneration().dependencyGeneration(functionNode);

        String calledPath = "\\chown-core.h\\chopt_init(struct Chown_option*)";

        assertEquals(1, findCalledFunctions(functionNode, calledPath).size());
    }

    @Test
    public void test03() throws IOException {
        IFunctionNode functionNode = warm("datatest\\lamnt\\coreutils\\src",
                "\\uname.c\\decode_switches(int,char**)");

        new FunctionCallDependencyGeneration().dependencyGeneration(functionNode);

        String calledPath = "\\uname.c\\usage(int)";

        assertEquals(1, findCalledFunctions(functionNode, calledPath).size());
    }

    @Test
    public void test04() throws IOException {
        IFunctionNode functionNode = warm("datatest\\lamnt\\coreutils\\src",
                "\\timeout.c\\cleanup(int)");

        new FunctionCallDependencyGeneration().dependencyGeneration(functionNode);

        String calledPath = "\\timeout.c\\settimeout(double,bool)";

        assertEquals(1, findCalledFunctions(functionNode, calledPath).size());
    }

    @Test
    public void test05() throws IOException {
        IFunctionNode functionNode = warm("datatest\\lamnt\\coreutils\\src",
                "\\cut.c\\cut_bytes(FILE*)");

        new FunctionCallDependencyGeneration().dependencyGeneration(functionNode);

        String calledPath = "\\cut.c\\next_item(uintmax_t*)";

        assertEquals(1, findCalledFunctions(functionNode, calledPath).size());
    }

    @Test
    public void test06() throws IOException {
        IFunctionNode functionNode = warm("datatest\\lamnt\\stdint_type",
                "\\stdint_lib.c\\callee()");

        new FunctionCallDependencyGeneration().dependencyGeneration(functionNode);

        String calledPath = "\\stdint_lib.c\\called(uint16_t)";

        assertEquals(1, findCalledFunctions(functionNode, calledPath).size());
    }

    @Test
    public void test07() throws IOException {
        IFunctionNode functionNode = warm("datatest\\lamnt\\coreutils\\src",
                "\\expr.c\\mpz_add(mpz_t,mpz_t,mpz_t)");

        new FunctionCallDependencyGeneration().dependencyGeneration(functionNode);

        String calledPath = "\\expr.c\\integer_overflow(char)";

        assertEquals(1, findCalledFunctions(functionNode, calledPath).size());
    }

    @Test
    public void test08() throws IOException {
        IFunctionNode functionNode = warm("datatest\\lamnt\\coreutils\\src",
                "\\group-list.c\\print_group_list(const char*,uid_t,gid_t,gid_t,bool,char)");

        new FunctionCallDependencyGeneration().dependencyGeneration(functionNode);

        String calledPath = "\\group-list.c\\print_group(gid_t,bool)";

        assertEquals(1, findCalledFunctions(functionNode, calledPath).size());
    }

    @Test
    public void test09() throws IOException {
        IFunctionNode functionNode = warm("datatest\\lamnt\\coreutils\\src",
                "\\install.c\\need_copy(const char*,const char*,const struct cp_options*)");

        new FunctionCallDependencyGeneration().dependencyGeneration(functionNode);

        String calledPath = "\\install.c\\extra_mode(mode_t)";

        assertEquals(1, findCalledFunctions(functionNode, calledPath).size());
    }

    @Test
    public void test10() throws IOException {
        IFunctionNode functionNode = warm("datatest\\lamnt\\coreutils\\src",
                "\\join.c\\xfields(struct line*)");

        new FunctionCallDependencyGeneration().dependencyGeneration(functionNode);

        String calledPath = "\\join.c\\extract_field(struct line*,char*,size_t)";

        assertEquals(1, findCalledFunctions(functionNode, calledPath).size());
    }

    @Test
    public void test11() {
        IFunctionNode functionNode = warm("datatest\\lamnt\\cunit\\CUnit",
                "\\Sources\\Framework\\TestDB.c\\CU_initialize_registry(void)");

        new FunctionCallDependencyGeneration().dependencyGeneration(functionNode);

        String calledPath = "\\Sources\\Framework\\TestDB.c\\CU_cleanup_registry(void)";

        assertEquals(1, findCalledFunctions(functionNode, calledPath).size());
    }
}