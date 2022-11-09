package com.dse.debugger.gdb;

public interface IGDBMI {

    String GDB_SET = "-gdb-set ";

    String GDB_RUN = "-exec-run";

    String GDB_NEXT = "-exec-continue";

    String GDB_NEXT_LINE = "-exec-next";

    String GDB_STEP_IN = "-exec-step";

    String GDB_STEP_OUT = "-exec-return";

    String GDB_KILL = "kill";

    String GDB_EXEC_FINISH = "-exec-finish";

    String GDB_EXIT = "-gdb-exit";

    /**
     * make gdb never step in all functions in [filename]
     * skip -gfile "[filename]"
     */
    String GDB_SKIP_FILE = "skip file ";

    String GDB_SKIP_FUNCTION = "skip function ";

    String GDB_EXEC = "-exec-arguments";

    /**
     * put breakpoints at line
     * -break-insert [line number]
     * -break-insert "[filename]:[line number]"
     */
    String GDB_BR = "-break-insert ";

    /**
     * show list breakpoint
     */
    String GDB_BR_LIST = "-break-list";

    /**
     * add a watchpoint ( the program will stop if there is any change in value of watchpoint)
     * -break-watch [line number]
     * -break-watch "[filename]:[line number]"
     */
    String GDB_ADD_WATCH = "-break-watch ";

    /**
     * delete a breakpoint
     * -break-delete [breakpoint number]
     */
    String GDB_DEL_POINT = "-break-delete ";

    /**
     *
     */
    String GDB_CLEAR = "clear ";

    /**
     * disable a breakpoint
     * -break-disable [breakpoint number]
     */
    String GDB_DISABLE = "-break-disable ";

    /**
     * enable a break point
     * -break-enable [breakpoint number]
     */
    String GDB_ENABLE = "-break-enable ";

    /**
     * /1 put a condition on a breakpoint
     * -break-condition [breakpoint number] [condition expression]
     * /2 remove condition on breakpoint
     * -break-condition [breakpoint number]
     */
    String GDB_BREAK_CONDITION = "-break-condition ";

    /**
     * show list of frame
     * -stack-list-frames
     */
    String GDB_FRAME_LIST = "-stack-list-frames";

    /**
     * change the current frame in gdb
     * -stack-select-frame [frame level]
     */
    String GDB_SELECT_FRAME = "-stack-select-frame";

    /**
     * show local variable in current frame
     * -stack-list-locals [number]
     *
     * @param number:
     * 0: show only name
     * 1: show name and value
     * 2: show name, type and value
     */
    String GDB_FRAME_LOCAL = "-stack-list-locals 2";

    /**
     * show arguments in current frame
     * -stack-list-arguments [number]
     *
     * @param number:
     * 0: show only name
     * 1: show name and value
     * 2: show name, type and value
     */
    String GDB_FRAME_ARGUMENTS = "-stack-list-arguments 2";

    /**
     * show local and argument variables in current frame
     * -stack-list-variables [number]
     *
     * @param number:
     * 0: show only name
     * 1: show name and value
     * 2: show name, type and value
     */
    String GDB_FRAME_VARIABLES = "-stack-list-variables 2";

    String GDB_CREATE_VARIABLE = "-var-create - * \"";

    String GDB_ASSIGN_VARIABLE = "-var-assign ";

    String GDB_VARIABLE_UPDATE = "-var-update --all-values *";

    String GDB_DELETE_VARIABLE = "-var-delete ";

    String GDB_GET_CHILD_VARIABLE = "-var-list-children --all-values \"";

    String GDB_END_STRING = "\"";

    /**
     * show value of a specific expression in current frame such as variable,...
     * -data-evaluate-expression [expr]
     */
    String GDB_DATA_EVALUATE_EXPRESSION = "-data-evaluate-expression";


}
