package com.dse.debugger.gdb;

import com.dse.debugger.component.breakpoint.BreakPoint;
import com.dse.debugger.component.frame.GDBFrame;
import com.dse.debugger.component.variable.GDBVar;
import com.dse.debugger.component.watches.WatchPoint;
import com.dse.debugger.gdb.analyzer.GDBStatus;
import com.dse.debugger.gdb.analyzer.GDBToken;
import com.dse.debugger.gdb.analyzer.OutputGDB;
import com.dse.logger.AkaLogger;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.util.ArrayList;
import java.util.List;

import static com.dse.debugger.gdb.analyzer.OutputSyntax.*;

public interface IOutputAnalyzer {

    GDBStatus analyze(String output, String command);

    ArrayList<GDBFrame> analyzeFrames(String output, String command);

    GDBVar getVarCreated(String output);

    GDBVar analyzeInternalVariable(String output);

    ArrayList<GDBVar> analyzeChildInternalVariable(String childOutput);

    ArrayList<GDBVar> analyzeVariables(String output, String command);

    OutputGDB analyzeOutput(String output, String cmd);

    WatchPoint analyzeWatchPoint(OutputGDB outputGDB);

    BreakPoint analyzeBreakAdd(String output);

}
