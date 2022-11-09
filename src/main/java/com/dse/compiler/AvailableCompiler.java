package com.dse.compiler;

public class AvailableCompiler {
    public static class TEMPLATE {
        public static final String NAME = "Custom Compiler";

        public static final String COMPILE_CMD = "compile";
        public static final String PRE_PRECESS_CMD = "pre-process";
        public static final String LINK_CMD = "link";
        public static final String DEBUG_CMD = "debug";

        public static final String INCLUDE_FLAG = "-I";
        public static final String DEFINE_FLAG = "-D";
        public static final String OUTPUT_FLAG = "-o";
        public static final String DEBUG_FLAG = "-ggdb";

        public static final String OUTPUT_EXTENSION = ".out";
    }

    public static class C_GNU_NATIVE extends TEMPLATE {
        public static final String NAME = "[GNU Native] C";
        
        public static final String COMPILE_CMD = "gcc -c -Werror-implicit-function-declaration";
        public static final String PRE_PRECESS_CMD = "gcc -c -E";
        public static final String LINK_CMD = "gcc -Werror-implicit-function-declaration";
        public static final String DEBUG_CMD = "gdb";

        public static final String INCLUDE_FLAG = "-I";
        public static final String DEFINE_FLAG = "-D";
        public static final String OUTPUT_FLAG = "-o";
        public static final String DEBUG_FLAG = "-ggdb";

        public static final String OUTPUT_EXTENSION = ".out";
    }

    public static class CPP_GNU_NATIVE extends TEMPLATE {
        public static final String NAME = "[GNU Native] C++";
        
        public static final String COMPILE_CMD = "g++ -c -Werror-implicit-function-declaration";
        public static final String PRE_PRECESS_CMD = "g++ -c -E";
        public static final String LINK_CMD = "g++ -Werror-implicit-function-declaration";
        public static final String DEBUG_CMD = "gdb";

        public static final String INCLUDE_FLAG = "-I";
        public static final String DEFINE_FLAG = "-D";
        public static final String OUTPUT_FLAG = "-o";
        public static final String DEBUG_FLAG = "-ggdb";

        public static final String OUTPUT_EXTENSION = ".out";
    }

    public static class CPP_11_GNU_NATIVE extends TEMPLATE {
        public static final String NAME = "[GNU Native] C++ 11";
        
        public static final String COMPILE_CMD = "g++ -c -std=c++11 -Werror-implicit-function-declaration";
        public static final String PRE_PRECESS_CMD = "g++ -c -E -std=c++11";
        public static final String LINK_CMD = "g++ -std=c++11 -Werror-implicit-function-declaration";
        public static final String DEBUG_CMD = "gdb";

        public static final String INCLUDE_FLAG = "-I";
        public static final String DEFINE_FLAG = "-D";
        public static final String OUTPUT_FLAG = "-o";
        public static final String DEBUG_FLAG = "-ggdb";

        public static final String OUTPUT_EXTENSION = ".out";
    }

    public static class CPP_14_GNU_NATIVE extends TEMPLATE {
        public static final String NAME = "[GNU Native] C++ 14";
        
        public static final String COMPILE_CMD = "g++ -c -std=c++14 -Werror-implicit-function-declaration";
        public static final String PRE_PRECESS_CMD = "g++ -c -E -std=c++14";
        public static final String LINK_CMD = "g++ -std=c++14 -Werror-implicit-function-declaration";
        public static final String DEBUG_CMD = "gdb";

        public static final String INCLUDE_FLAG = "-I";
        public static final String DEFINE_FLAG = "-D";
        public static final String OUTPUT_FLAG = "-o";
        public static final String DEBUG_FLAG = "-ggdb";

        public static final String OUTPUT_EXTENSION = ".out";
    }

    public static class C_GNU_NATIVE_WINDOWS_MINGW extends TEMPLATE {
        public static final String NAME = "[GNU Native Ming Windows] C";

        public static final String COMPILE_CMD = "gcc -c -Werror-implicit-function-declaration";
        public static final String PRE_PRECESS_CMD = "gcc -c -E";
        public static final String LINK_CMD = "gcc -Werror-implicit-function-declaration";
        public static final String DEBUG_CMD = "gdb";

        public static final String INCLUDE_FLAG = "-I";
        public static final String DEFINE_FLAG = "-D";
        public static final String OUTPUT_FLAG = "-o";
        public static final String DEBUG_FLAG = "-ggdb";

        public static final String OUTPUT_EXTENSION = ".out";
    }

    public static class CPP_GNU_NATIVE_WINDOWS_MINGW extends TEMPLATE {
        public static final String NAME = "[GNU Native Ming Windows] C++";

        public static final String COMPILE_CMD = "g++ -c -Werror-implicit-function-declaration";
        public static final String PRE_PRECESS_CMD = "g++ -c -E";
        public static final String LINK_CMD = "g++ -Werror-implicit-function-declaration";
        public static final String DEBUG_CMD = "gdb";

        public static final String INCLUDE_FLAG = "-I";
        public static final String DEFINE_FLAG = "-D";
        public static final String OUTPUT_FLAG = "-o";
        public static final String DEBUG_FLAG = "-ggdb";

        public static final String OUTPUT_EXTENSION = ".out";
    }

    public static class CPP_11_GNU_NATIVE_WINDOWS_MINGW extends TEMPLATE {
        public static final String NAME = "[GNU Native Ming Windows] C++ 11";

        public static final String COMPILE_CMD = "g++ -c -std=gnu++11 -Werror-implicit-function-declaration";
        public static final String PRE_PRECESS_CMD = "g++ -c -E -std=gnu++11";
        public static final String LINK_CMD = "g++ -std=gnu++11 -Werror-implicit-function-declaration";
        public static final String DEBUG_CMD = "gdb";

        public static final String INCLUDE_FLAG = "-I";
        public static final String DEFINE_FLAG = "-D";
        public static final String OUTPUT_FLAG = "-o";
        public static final String DEBUG_FLAG = "-ggdb";

        public static final String OUTPUT_EXTENSION = ".out";
    }

    public static class CPP_14_GNU_NATIVE_WINDOWS_MINGW extends TEMPLATE {
        public static final String NAME = "[GNU Native Ming Windows] C++ 14";

        public static final String COMPILE_CMD = "g++ -c -std=gnu++14 -Werror-implicit-function-declaration";
        public static final String PRE_PRECESS_CMD = "g++ -c -E -std=gnu++14";
        public static final String LINK_CMD = "g++ -std=gnu++14 -Werror-implicit-function-declaration";
        public static final String DEBUG_CMD = "gdb";

        public static final String INCLUDE_FLAG = "-I";
        public static final String DEFINE_FLAG = "-D";
        public static final String OUTPUT_FLAG = "-o";
        public static final String DEBUG_FLAG = "-ggdb";

        public static final String OUTPUT_EXTENSION = ".out";
    }
}
