package com.dse.parser.systemlibrary;

import com.dse.compiler.AvailableCompiler;
import com.dse.compiler.Compiler;
import com.dse.compiler.Terminal;
import com.dse.environment.Environment;
import com.dse.parser.ProjectParser;
import com.dse.parser.object.INode;
import com.dse.parser.object.ProjectNode;
import com.dse.search.Search;
import com.dse.search.condition.SourcecodeFileNodeCondition;
import com.dse.util.CompilerUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class IncludeFilter {
    public static final String ALL_HEADER_FLAG = "-M";
    public static final String USER_HEADER_FLAG = "-MM";

    public static void main(String[] args) throws NoSuchFieldException, IllegalAccessException, InterruptedException {
        String path = "/home/lamnt/IdeaProjects/akautauto/datatest/duc-anh/Algorithm";

        ProjectParser projectParser = new ProjectParser(new File(path));

        projectParser.setCpptoHeaderDependencyGeneration_enabled(true);
        projectParser.setExpandTreeuptoMethodLevel_enabled(true);
        projectParser.setExtendedDependencyGeneration_enabled(true);

        ProjectNode projectRoot = projectParser.getRootTree();
        Environment.getInstance().setProjectNode(projectRoot);

        Compiler compiler = new Compiler(AvailableCompiler.CPP_11_GNU_NATIVE.class);
        Environment.getInstance().setCompiler(compiler);

        IncludeFilter filter = new IncludeFilter();
        filter.findAllIncluded(projectRoot);

        IncludeCache cache = IncludeCache.getInstance();

        ProjectNode systemRoot = new ProjectNode();
        systemRoot.setAbsolutePath("SYSTEM");

        ExecutorService executor = Executors.newFixedThreadPool(cache.keySet().size());

        for (String systemPath : cache.keySet()) {
            executor.execute(new SystemHeaderParseTask(systemRoot, systemPath));
        }

        executor.shutdown();

        while(!executor.awaitTermination(1, TimeUnit.MINUTES));

        System.out.println();
    }

    public void findAllIncluded(ProjectNode root) {
        List<INode> sources = Search.searchNodes(root, new SourcecodeFileNodeCondition());

        Compiler compiler = Environment.getInstance().getCompiler();
        String preprocessCmd = compiler.getPreprocessCommand();
        String preprocessCmdAllHeader = preprocessCmd + " " + ALL_HEADER_FLAG;
        String preprocessCmdUserHeader = preprocessCmd + " " + USER_HEADER_FLAG;

        IncludeCache cache = IncludeCache.getInstance();

        for (INode source : sources) {
            try {
                String filePath = source.getAbsolutePath();

                String command = compiler.generatePreprocessCommand(filePath);

                String[] allHeaderCmd = CompilerUtils
                    .prepareForTerminal(
                            compiler,
                            command.replace(preprocessCmd, preprocessCmdAllHeader)
                    );

                String allHeaderStdout = new Terminal(allHeaderCmd).getStdout();

                String[] allHeaders = parseStdout(allHeaderStdout);

                String[] userHeaderCmd = CompilerUtils
                        .prepareForTerminal(
                                compiler,
                                command.replace(preprocessCmd, preprocessCmdUserHeader)
                        );

                String userHeaderStdout = new Terminal(userHeaderCmd).getStdout();

                String[] userHeaders = parseStdout(userHeaderStdout);

                String[] systemHeaders = getSystemHeader(allHeaders, userHeaders);



                for (String systemHeader : systemHeaders) {
                    List<INode> includes = cache.get(systemHeader);

                    if (includes == null)
                        includes = new ArrayList<>();

                    includes.add(source);

                    cache.put(systemHeader, includes);
                }

            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private String[] getSystemHeader(String[] all, String[] user) {
        List<String> _user = Arrays.asList(user);

        return Arrays.stream(all)
                .filter(path -> !_user.contains(path))
                .toArray(String[]::new);
    }

    private String[] parseStdout(String stdout) {
        String[] rawPaths = stdout.split("\\R");

        rawPaths = Arrays.stream(rawPaths)
                .map(path -> path.replace(" \\", "").trim())
                .filter(Objects::nonNull)
                .collect(Collectors.toList())
                .toArray(rawPaths);

        List<String> paths = new ArrayList<>();

        for (String path : rawPaths) {
            String[] items = path.split("\\s");

            int i = 0;

            while (i < items.length) {
                String item = items[i].trim();

                if (item.isEmpty()) {
                    i++;
                    continue;
                }

                if (item.endsWith("\\")) {
                    String completedPath = item.substring(0, item.length() - 1) + " " + items[i + 1];
                    paths.add(completedPath);
                    i += 2;
                } else {
                    paths.add(item);
                    i++;
                }
            }
        }

        paths.remove(0);

        return paths.toArray(rawPaths);
    }
}
