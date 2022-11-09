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
import org.eclipse.cdt.dsf.mi.service.command.output.*;

import java.util.ArrayList;
import java.util.List;

import static com.dse.debugger.gdb.analyzer.OutputSyntax.*;

public class OutputAnalyzer extends AbstractOutputAnalyzer {

    private static final AkaLogger logger = AkaLogger.get(OutputAnalyzer.class);

    @Override
    public GDBStatus analyze(String output, String command) {
        if (output != null) {
            String removedOutput = removeUnnecessary(output);
            if (removedOutput.contains("*stopped,") && !removedOutput.contains("reason=")) {
                removedOutput = removedOutput.replace("*stopped,", "*stopped,reason=\"exited-normally\",");
            }
            String json = parseOutputToJson(removedOutput, command);

            if (json.contains("*stopped,") || json.contains("id:\"2\",group-id:\"i1\"")) {
                json = json.replace("*stopped,", "");
                json = json.replace("id:\"2\",group-id:\"i1\"", "");
            }
            logger.debug("Parsing the result of command " +  command +":\n" + json);
            JsonObject jsonOb = JsonParser.parseString(json).getAsJsonObject();
            GDBStatus res;
            if (jsonOb.get("reason") != null) {
                String reason = jsonOb.get("reason").getAsString();
                if (reason.equals(EXIT_HIT.getSyntax())) {
                    res = GDBStatus.EXIT;
                    res.setReason(reason);
                    return res;
                }
                if (reason.equals(EXIT_ERROR.getSyntax())) {
                    res = GDBStatus.ERROR;
                    res.setReason(reason);
                    return res;
                }
                res = GDBStatus.CONTINUABLE;
                res.setReason(reason);
                return res;
            } else {
                if (command.equals(GDB.GDB_STEP_OUT)) {
                    return GDBStatus.CONTINUABLE;
                } else if (command.equals(GDB.GDB_KILL)) {
                    return GDBStatus.EXIT;
                }
                return GDBStatus.ERROR;
            }
        } else
            return GDBStatus.ERROR;
    }

    @Override
    public ArrayList<GDBFrame> analyzeFrames(String output, String command) {
        String removeOutput = removeUnnecessary(output);
        String json = parseOutputToJson(removeOutput, command);
        JsonObject jsonOb = JsonParser.parseString(json).getAsJsonObject();
        if (command.equals(GDB.GDB_FRAME_LIST))
            return extractFrame(jsonOb.getAsJsonArray("stack"));
        return null;
    }

    @Override
    public GDBVar getVarCreated(String output) {
        String removeOutput = removeUnnecessary(output);
        if (removeOutput.startsWith(DONE_RESULT.getSyntax())){
            String json = parseOutputToJson(removeOutput, "");
            JsonObject jsonOb = JsonParser.parseString(json).getAsJsonObject();
            return gson.fromJson(jsonOb,GDBVar.class);
        }
        return null;
    }

    @Override
    public GDBVar analyzeInternalVariable(String output) {
        String removedOutput = removeUnnecessary(output);
        String json = parseOutputToJson(removedOutput, "");
        JsonObject jsonOb = JsonParser.parseString(json).getAsJsonObject();
        return gson.fromJson(jsonOb, GDBVar.class);
    }

    @Override
    public ArrayList<GDBVar> analyzeChildInternalVariable(String childOutput) {
        String removedOutput = removeUnnecessary(childOutput).replaceAll("child=\\{", "\\{");
        String json = parseOutputToJson(removedOutput, "");
        JsonObject jsonOb = JsonParser.parseString(json).getAsJsonObject();
        ArrayList<GDBVar> res = new ArrayList<>();
        JsonArray jsonArray = jsonOb.getAsJsonArray("children");
        if(jsonArray != null){
            jsonArray.forEach(jsonElement -> {
                JsonObject jo = jsonElement.getAsJsonObject();
                res.add(gson.fromJson(jo, GDBVar.class));
            });
        }
        return res;
    }

    @Override
    public ArrayList<GDBVar> analyzeVariables(String output, String command) {
        String removeOutput = removeUnnecessary(output);
        String json = parseOutputToJson(removeOutput, command);
        JsonArray jsonArr = JsonParser.parseString(json).getAsJsonObject().getAsJsonArray("variables");
        if (jsonArr == null) {
            return new ArrayList<>();
        }
        return extractVar(jsonArr);
    }


    @Override
    public OutputGDB analyzeOutput(String output,String cmd) {
        String removedOutput = removeUnnecessary(output);
        if (cmd.equals(GDB.GDB_DEL_POINT) || cmd.equals(GDB.GDB_SELECT_FRAME) || cmd.equals(GDB.GDB_DATA_EVALUATE_EXPRESSION) ||
                cmd.equals(GDB.GDB_DISABLE) || cmd.equals(GDB.GDB_ENABLE) || cmd.equals(GDB.GDB_BREAK_CONDITION)){
            if (removedOutput.equals("")){
                return new OutputGDB(false,"");
            }
        }
        if (removedOutput.startsWith(ERROR_RESULT.getSyntax())){
            String json = parseOutputToJson(removedOutput,"");
            return new OutputGDB(true,json);
        } else if (removedOutput.startsWith(DONE_RESULT.getSyntax())) {
            String json = parseOutputToJson(removedOutput,"");
            return new OutputGDB(false,json);
        }
        return null;
    }

    @Override
    public WatchPoint analyzeWatchPoint(OutputGDB outputGDB) {
        String json = outputGDB.getJson();
        JsonObject watch = JsonParser.parseString(json).getAsJsonObject().get("wpt").getAsJsonObject();
        return gson.fromJson(watch,WatchPoint.class);
    }
     public BreakPoint changeBreakPointType(MIBreakpoint miBreakpoint) {
         BreakPoint breakPoint = new BreakPoint();
         breakPoint.setNumber(miBreakpoint.getNumber());
         breakPoint.setFile(miBreakpoint.getFile());
         breakPoint.setLine(miBreakpoint.getLine());
         breakPoint.setFull(miBreakpoint.getFullName());
         breakPoint.setTimes(miBreakpoint.getTimes());
         breakPoint.setDisp(miBreakpoint.getDisposition());
         breakPoint.setAddr(miBreakpoint.getAddress());
         breakPoint.setFunc(miBreakpoint.getFunction());
         if(miBreakpoint.isEnabled()) {
             breakPoint.setEnabled("y");
             breakPoint.setSelected(true);
         } else {
             breakPoint.setEnabled("n");
             breakPoint.setSelected(false);
         }
         breakPoint.setCond(miBreakpoint.getCondition());
        return breakPoint;
     }

    @Override
    public BreakPoint analyzeBreakAdd(String output) {
        MIParser parser = new MIParser();
        MIResultRecord record = parser.parseMIResultRecord(output);
        MIOOBRecord mioobRecord = parser.parseMIOOBRecord(output);
        MIOutput miOutput = new MIOutput(record, new MIOOBRecord[]{mioobRecord});
        MIBreakInsertInfo info = new MIBreakInsertInfo(miOutput);
        if (info.getMIBreakpoints().length == 1) {
            return changeBreakPointType(info.getMIBreakpoints()[0]);
        }
        else if (info.getMIBreakpoints().length > 1) {
            List<BreakPoint> children = new ArrayList<>();
            BreakPoint newBr = changeBreakPointType(info.getMIBreakpoints()[0]);
            for (int i = 1 ; i < info.getMIBreakpoints().length; i++) {
                BreakPoint breakPoint = changeBreakPointType(info.getMIBreakpoints()[i]);
                newBr.setFile(breakPoint.getFile());
                newBr.setLine(breakPoint.getLine());
                newBr.setFull(breakPoint.getFull());
                children.add(breakPoint);
            }
            newBr.setChildren(children);
            return newBr;
        }
        return null;
    }

    public String normalizeOutput(String str) {
        int mark1 = 0;
        int mark2 = 0;
        int mark3 = 0;
        boolean isContain = false;
        for (int i = 0; i < str.length(); i++) {
            if (str.charAt(i) == 'v' && str.charAt(i + 1) == 'a' && str.charAt(i + 2) == 'l') {
               int j = i;
               while (str.charAt(j) != '}' && str.charAt(j + 1) != ',') {
                   if (str.charAt(j) == '\\') {
                       isContain = true;
                       break;
                   }
                   j++;
               }
            }
            if (isContain == true) {
                mark1 = i;
                break;
            }
        }
        if (isContain) {
            for (int i = mark1; i < str.length(); i++) {
                if (str.charAt(i) == '\\') {
                    mark2 = i;
                    break;
                }
            }
            for (int i = mark1; i < str.length(); i++) {
                if (str.charAt(i) == '"' && (str.charAt(i + 1) == ',' || str.charAt(i + 1) == '}')) {
                    mark3 = i;
                    break;
                }
            }
            if (str.charAt(mark2 - 1) == '\'') {
                mark2--;
            }
            str = str.substring(0, mark2 - 1) + str.substring(mark3, str.length());
        }
        return str;
    }

    protected String removeUnnecessary(String output) {
        String[] lines = output.split("\\R");
        StringBuilder res = new StringBuilder();
        for (String line : lines) {
//            if (line.contains("*stopped,") && !line.contains("reason=")) {
//                line = line.substring(0,9) + "reason=\"exited-normally\"," + line.substring(9, line.length());
//            }
            if (!(isUseless(line) || isNotContainingInfo(line))) {
                res.append(line).append("\n");
            }
        }
        String tmp = res.toString();
        tmp = normalizeOutput(tmp);
        return tmp;
    }

    protected boolean isUseless(String line) {
        return line.startsWith(GDBToken.NOTIFY_ASYNC_OUTPUT.getToken()) ||
                line.startsWith(GDBToken.CONSOLE_STREAM_OUTPUT.getToken()) ||
                line.startsWith(GDBToken.TARGET_STREAM_OUTPUT.getToken()) ||
                line.startsWith(GDBToken.LOG_STREAM_OUTPUT.getToken()) ||
                line.startsWith(END_LOG.getSyntax());
    }

    protected boolean isNotContainingInfo(String line) {
        if (line.startsWith(DONE_RESULT.getSyntax() + ",time="))
            return true;
        return line.startsWith(GDBToken.RESULT_RECORD.getToken() + RUNNING_RESULT.getSyntax()) ||
                line.startsWith(GDBToken.EXEC_ASYNC_OUTPUT.getToken() + RUNNING_RESULT.getSyntax());
    }
}
