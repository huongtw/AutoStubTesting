package com.dse.probe_point_manager;

import com.dse.config.WorkspaceConfig;
import com.dse.environment.Environment;
import com.dse.parser.object.IFunctionNode;
import com.dse.parser.object.INode;
import com.dse.parser.object.ISourcecodeFileNode;
import com.dse.probe_point_manager.objects.ProbePoint;
import com.dse.probe_point_manager.objects.ProbePointExporter;
import com.dse.probe_point_manager.objects.ProbePointImporter;
import com.dse.search.Search;
import com.dse.search.condition.FunctionNodeCondition;
import com.dse.testcase_manager.TestCase;
import com.dse.logger.AkaLogger;
import com.dse.util.PathUtils;
import com.dse.util.Utils;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import javafx.collections.FXCollections;
import javafx.collections.ObservableMap;

import java.io.File;
import java.io.FilenameFilter;
import java.lang.reflect.Type;
import java.util.*;

public class ProbePointManager {

    private final static AkaLogger logger = AkaLogger.get(ProbePointManager.class);

    /**
     * Singleton partern
     */
    private static ProbePointManager instance = null;

    private static final String PROBE_FOLDER = new WorkspaceConfig().fromJson().getProbePointDirectory();
    private static final String INFO_JSON = "info.json";

    public static ProbePointManager getInstance() {
        if (instance == null) {
            instance = new ProbePointManager();
        }
        return instance;
    }

    private final Map<String, ProbePoint> nameToProbePointMap = new HashMap<>();
    private ObservableMap<String, TreeSet<Integer>> probePointMap = FXCollections.observableHashMap();
    private final Map<TestCase, List<ProbePoint>> testCaseToProbePointMap = new HashMap<>();
    private final HashMap<IFunctionNode, ArrayList<ProbePoint>> funPpMap = new HashMap<>();
    private final Map<String, ProbePoint> nameToInvalidProbePointMap = new HashMap<>();

    public void clear() {
        nameToProbePointMap.clear();
        probePointMap.clear();
        funPpMap.clear();
    }

    // called when open existed Environment or after creating an Environment
    public void loadProbePoints() {

        // todo: synchronize PP locations file and probe point files

        boolean importSuccess = importProbePointsLocation();
        if (!importSuccess)
            return;

        File probePointDirectory = new File(PROBE_FOLDER);
        FilenameFilter ppFilter = (f, name) -> name.endsWith("pp");
        FilenameFilter jsonFilter = (f, name) -> name.endsWith("json");

        for (File file : Objects.requireNonNull(probePointDirectory.listFiles(ppFilter))) {
            for (File jsonFile : Objects.requireNonNull(file.listFiles(jsonFilter))) {
                String content = Utils.readFileContent(jsonFile);
                JsonObject jsonObject = JsonParser.parseString(content).getAsJsonObject();
                ProbePoint probePoint = new ProbePointImporter().importProbePoint(jsonObject);

                if (!probePoint.isValid())
                    addToInvalid(probePoint);
                else {
                    IFunctionNode functionNode = probePoint.getFunctionNode();
                    if (!funPpMap.containsKey(functionNode)) {
                        ArrayList<ProbePoint> newSet = new ArrayList<>();
                        funPpMap.put(functionNode, newSet);
                    }
                    funPpMap.get(functionNode).add(probePoint);

                    nameToProbePointMap.put(probePoint.getName(), probePoint);

                    // Update line in source code file attribute of probe point.
                    // and update probe point locations on disk
                    int oldLIS = probePoint.getLineInSourceCodeFile();
                    if (ProbePointUtils.updateLineInSourceCodeFile(probePoint)) {
                        if (searchProbePoints(probePoint.getSourcecodeFileNode(), oldLIS).size() == 0) {
                            probePointMap.get(probePoint.getSourcecodeFileNode().getAbsolutePath()).remove(oldLIS);
                        }
                        int newLIS = probePoint.getLineInSourceCodeFile();
                        probePointMap.get(probePoint.getSourcecodeFileNode().getAbsolutePath()).add(newLIS);

                        exportProbePointsLocation();
                        exportProbePointToFile(probePoint); // update to disk
                    }

                    updateTestCasesPPMapAfterAdd(probePoint);
                }
            }
        }
    }

    public void updateTestCasesPPMapAfterAdd(ProbePoint probePoint) {
        updateTestCasesProbePointMap(probePoint, new ArrayList<>(), probePoint.getTestCases());
    }

    public void updateTestCasesPPMapAfterEdit(ProbePoint probePoint, List<TestCase> oldTestCases, List<TestCase> newTestCases) {
        updateTestCasesProbePointMap(probePoint, oldTestCases, newTestCases);
    }

    public void updateTestCasesPPMapAfterRemove(ProbePoint probePoint) {
        updateTestCasesProbePointMap(probePoint, probePoint.getTestCases(), new ArrayList<>());
    }

    private void updateTestCasesProbePointMap(ProbePoint probePoint, List<TestCase> oldTestCases, List<TestCase> newTestCases) {
        for (TestCase testCase : oldTestCases) {
            if (testCaseToProbePointMap.get(testCase) == null) {
                logger.error("An old test case is not in testCaseToProbePointMap.");
                return;
            } else {
                if (!newTestCases.contains(testCase)) {
                    testCaseToProbePointMap.get(testCase).remove(probePoint);
                    if (testCaseToProbePointMap.get(testCase).size() == 0) {
                        testCaseToProbePointMap.remove(testCase);
                    }
                }
            }
        }

        for (TestCase testCase : newTestCases) {
            if (testCaseToProbePointMap.get(testCase) == null) {
                testCaseToProbePointMap.put(testCase, new ArrayList<>());
            }

            if (!testCaseToProbePointMap.get(testCase).contains(probePoint)) {
                testCaseToProbePointMap.get(testCase).add(probePoint);
            }
        }
    }

    public boolean add(ProbePoint probePoint) {
        if (!isExisted(probePoint)) {
            nameToProbePointMap.put(probePoint.getName(), probePoint);

            String path = probePoint.getSourcecodeFileNode().getAbsolutePath();
            if (probePointMap.get(path) == null) {
                probePointMap.put(path, new TreeSet<>());
            }

            probePointMap.get(path).add(probePoint.getLineInSourceCodeFile());
            exportProbePointsLocation();
            return true;
        } else {
            logger.debug("The name of ProbePoint is existed. " + probePoint.getName());
            return false;
        }
    }

    public void addToFunctionMap(ProbePoint probePoint) {
        IFunctionNode functionNode = probePoint.getFunctionNode();
        if (funPpMap.containsKey(functionNode)) {
            funPpMap.get(functionNode).add(probePoint);
        } else {
            ArrayList<ProbePoint> newSet = new ArrayList<ProbePoint>() {{
                add(probePoint);
            }};
            funPpMap.put(functionNode, newSet);
        }
    }

    public void removeFromFunctionMap(ProbePoint probePoint) {
        IFunctionNode functionNode = probePoint.getFunctionNode();
        if (funPpMap.containsKey(functionNode)) {
            funPpMap.get(functionNode).remove(probePoint);
            if (funPpMap.get(functionNode).size() == 0) {
                funPpMap.remove(functionNode);
            }
        } else {
            // maybe never happen
        }
    }

    public boolean isExisted(ProbePoint probePoint) {
        return nameToProbePointMap.containsKey(probePoint.getName());
    }

    public void remove(ProbePoint probePoint) {
        if (nameToProbePointMap.containsKey(probePoint.getName())) {
            nameToProbePointMap.remove(probePoint.getName());
            funPpMap.get(probePoint.getFunctionNode()).remove(probePoint);
            deleteProbePointFile(probePoint);
            updateTestCasesPPMapAfterRemove(probePoint);

            String path = probePoint.getSourcecodeFileNode().getAbsolutePath();
            if (searchProbePoints(probePoint.getSourcecodeFileNode(), probePoint.getLineInSourceCodeFile()).size() == 0) {
                probePointMap.get(path).remove(probePoint.getLineInSourceCodeFile());

                if (searchProbePoints(probePoint.getSourcecodeFileNode()).size() == 0) {
                    probePointMap.remove(path);
                }
                exportProbePointsLocation();
            }
        }
    }

    public ProbePoint getProbePointByName(String name) {
        ProbePoint probePoint = nameToProbePointMap.get(name);
        if (probePoint == null) {
            probePoint = nameToInvalidProbePointMap.get(name);
        }
        return probePoint;
    }

    public List<ProbePoint> searchProbePoints(ISourcecodeFileNode sourcecodeFileNode) {
        List<ProbePoint> probePoints = new ArrayList<>();
        for (ProbePoint pp : nameToProbePointMap.values()) {
            if (PathUtils.equals(pp.getSourcecodeFileNode().getAbsolutePath(), sourcecodeFileNode.getAbsolutePath())) {
                probePoints.add(pp);
            }
        }
        return probePoints;
    }

    public List<ProbePoint> searchProbePoints(ISourcecodeFileNode sourcecodeFileNode, int lineNumber) {
        List<ProbePoint> probePoints = new ArrayList<>();
        for (ProbePoint pp : searchProbePoints(sourcecodeFileNode)) {
            if (pp.getLineInSourceCodeFile() == lineNumber) {
                probePoints.add(pp);
            }
        }
        return probePoints;
    }

    public List<ProbePoint> getAllProbePoint() {
        List<ProbePoint> probePoints = new ArrayList<>();
        probePoints.addAll(nameToProbePointMap.values());
        probePoints.addAll(nameToInvalidProbePointMap.values());
        return probePoints;
    }

    public void exportProbePointsLocation() {
        String probePointPath = PROBE_FOLDER + File.separator
                + Environment.getInstance().getName()
                + ".pplocations.json";
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        String json = gson.toJson(probePointMap);
        Utils.writeContentToFile(json, probePointPath);
    }

    private boolean importProbePointsLocation() {
        String probePointPath = PROBE_FOLDER + File.separator
                + Environment.getInstance().getName()
                + ".pplocations.json";
        if (new File(probePointPath).exists()) {
            String content = Utils.readFileContent(probePointPath);
            Gson gson = new Gson();
            Type empMapType = new TypeToken<HashMap<String, TreeSet<Integer>>>() {
            }.getType();
            probePointMap = FXCollections.observableMap(gson.fromJson(content, empMapType));
            return true;
        } else {
            return false;
        }
    }

    public TreeSet<Integer> getListOfProbePointLines(ISourcecodeFileNode sourcecodeFileNode) {
        TreeSet<Integer> treeSet = probePointMap.get(sourcecodeFileNode.getAbsolutePath());
        if (treeSet != null)
            return treeSet;
        else
            return new TreeSet<>();
    }

    public void exportProbePointToFile(ProbePoint probePoint) {
        String content = new ProbePointExporter().export(probePoint);
        Utils.writeContentToFile(content, probePoint.getPath() + File.separator + INFO_JSON);
    }

    public void deleteProbePointFile(ProbePoint probePoint) {
        Utils.deleteFileOrFolder(new File(probePoint.getPath()));
    }

    public List<ProbePoint> getProbePointsByTestCase(TestCase testCase) {
        List<ProbePoint> list = testCaseToProbePointMap.get(testCase);
        if (list == null)
            list = new ArrayList<>();

        return list;
    }

    public HashMap<IFunctionNode, ArrayList<ProbePoint>> getFunPpMap() {
        return funPpMap;
    }

    public void moveAllProbePointsToInvalid(IFunctionNode functionNode) {
        if (funPpMap.get(functionNode) != null) {
            List<ProbePoint> probePoints = new ArrayList<>(funPpMap.get(functionNode));
            ProbePointManager.getInstance().addToInvalid(probePoints);
        }
    }

    public void deleteProbePointsOfFunction(IFunctionNode functionNode) {
        if (funPpMap.get(functionNode) != null) {
            List<ProbePoint> probePoints = new ArrayList<>(funPpMap.get(functionNode));
            for (ProbePoint pp : probePoints) {
                remove(pp);
            }
        }
    }

    /**
     * After change a source code file (change a function, insert, delete line of code,v.v.),
     * attribute lineInSourceCodeFile of probe points are incorrect.
     * This method update line in source code for probe points in a source code file.
     * This method is also update probe point locations saved on disk.
     * Call this method after loading probe point from disk and after incremental building, updating.
     *
     * @param sourcecodeFileNode source code file node
     */
    public void updateLineInSourceCode(ISourcecodeFileNode sourcecodeFileNode) {
        List<INode> nodes = Search.searchNodes(sourcecodeFileNode, new FunctionNodeCondition());
        for (INode node : nodes) {
            if (funPpMap.get(node) != null) {
                for (ProbePoint pp : funPpMap.get(node)) {
                    int oldLIS = pp.getLineInSourceCodeFile();
                    if (ProbePointUtils.updateLineInSourceCodeFile(pp)) {
                        if (searchProbePoints(sourcecodeFileNode, oldLIS).size() == 0) {
                            probePointMap.get(sourcecodeFileNode.getAbsolutePath()).remove(oldLIS);
                        }
                        int newLIS = pp.getLineInSourceCodeFile();
                        probePointMap.get(sourcecodeFileNode.getAbsolutePath()).add(newLIS);

                        exportProbePointsLocation();
                        exportProbePointToFile(pp); // update to disk
                    }
                }
            }
        }
    }

    public void addToInvalid(List<ProbePoint> probePoints) {
        for (ProbePoint pp : probePoints)
            addToInvalid(pp);
    }

    private void addToInvalid(ProbePoint probePoint) {
        // add to invalid PPs map
        if (!nameToInvalidProbePointMap.containsKey(probePoint.getName())) {
            nameToInvalidProbePointMap.put(probePoint.getName(), probePoint);
            probePoint.setValid(false);
            // remove from other maps
            if (nameToProbePointMap.containsKey(probePoint.getName())) {
                nameToProbePointMap.remove(probePoint.getName());
                funPpMap.get(probePoint.getFunctionNode()).remove(probePoint);
                updateTestCasesPPMapAfterRemove(probePoint);

                String path = probePoint.getSourcecodeFileNode().getAbsolutePath();
                if (searchProbePoints(probePoint.getSourcecodeFileNode(), probePoint.getLineInSourceCodeFile()).size() == 0) {
                    probePointMap.get(path).remove(probePoint.getLineInSourceCodeFile());

                    if (searchProbePoints(probePoint.getSourcecodeFileNode()).size() == 0) {
                        probePointMap.remove(path);
                    }
                    exportProbePointsLocation();
                }
            }
            ProbePointManager.getInstance().exportProbePointToFile(probePoint);
        }
    }

    public ObservableMap<String, TreeSet<Integer>> getProbePointMap() {
        return probePointMap;
    }
}
