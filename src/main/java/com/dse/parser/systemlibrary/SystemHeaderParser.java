package com.dse.parser.systemlibrary;

import com.dse.environment.Environment;
import com.dse.guifx_v3.helps.UILogger;
import com.dse.parser.SourcecodeFileParser;
import com.dse.parser.dependency.IncludeHeaderDependency;
import com.dse.parser.object.HeaderNode;
import com.dse.parser.object.INode;
import com.dse.parser.object.ProjectNode;
import com.dse.thread.AbstractAkaTask;

import java.io.File;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class SystemHeaderParser {
    private String path;

    private String library;

    public SystemHeaderParser(String path, String library) {
        this.path = path;
        this.library = library;
    }

    public HeaderNode parse() throws Exception {
        SourcecodeFileParser parser = new SourcecodeFileParser();
        INode node = parser.parseSourcecodeFile(new File(path));

        HeaderNode headerNode = new HeaderNode();
        headerNode.setAST(parser.getTranslationUnit());
        headerNode.setAbsolutePath(library);
        headerNode.setChildren(node.getChildren());

        generateDependency(headerNode);

        return headerNode;
    }

    private void generateDependency(HeaderNode headerNode) {
        String path = headerNode.getAbsolutePath();
        List<INode> includes = IncludeCache.getInstance().get(path);

        for (INode node : includes) {
            IncludeHeaderDependency dependency = new IncludeHeaderDependency(node, headerNode);

            if (!node.getDependencies().contains(dependency))
                node.getDependencies().add(dependency);

            if (!headerNode.getDependencies().contains(dependency))
                headerNode.getDependencies().add(dependency);
        }
    }

    public static class ReloadThread extends AbstractAkaTask<ProjectNode> {

//        @Override
//        public void run() {
//            ProjectNode projectRoot = Environment.getInstance().getProjectNode();
//
//            IncludeFilter filter = new IncludeFilter();
//            filter.findAllIncluded(projectRoot);
//
//            ProjectNode systemRoot = new ProjectNode();
//            systemRoot.setAbsolutePath("SYSTEM");
//
//            Environment.getInstance().setSystemRoot(systemRoot);
//
//            IncludeCache cache = IncludeCache.getInstance();
//
//            ExecutorService executor = Executors.newFixedThreadPool(cache.keySet().size());
//
//            for (String systemPath : cache.keySet()) {
//                executor.execute(new SystemHeaderReloadTask(systemRoot, systemPath));
//            }
//
//            executor.shutdown();
//        }

        @Override
        protected ProjectNode call() throws Exception {
            ProjectNode projectRoot = Environment.getInstance().getProjectNode();

            IncludeFilter filter = new IncludeFilter();
            filter.findAllIncluded(projectRoot);

            ProjectNode systemRoot = new ProjectNode();
            systemRoot.setAbsolutePath("SYSTEM");

            Environment.getInstance().setSystemRoot(systemRoot);

            IncludeCache cache = IncludeCache.getInstance();

            ExecutorService executor = Executors.newFixedThreadPool(cache.keySet().size());

            for (String systemPath : cache.keySet()) {
                executor.execute(new SystemHeaderReloadTask(systemRoot, systemPath));
            }

            executor.shutdown();
            while (!executor.awaitTermination(10, TimeUnit.MINUTES));
            return systemRoot;
        }
    }

    public static class InitThread extends AbstractAkaTask<ProjectNode> {
//        @Override
//        public void run() {
//            try {
//                ProjectNode projectRoot = Environment.getInstance().getProjectNode();
//
//                IncludeFilter filter = new IncludeFilter();
//                filter.findAllIncluded(projectRoot);
//
//                IncludeCache cache = IncludeCache.getInstance();
//
//                ProjectNode systemRoot = new ProjectNode();
//                systemRoot.setAbsolutePath("SYSTEM");
//
//                Environment.getInstance().setSystemRoot(systemRoot);
//
//                ExecutorService executor = Executors.newFixedThreadPool(cache.keySet().size());
//
//                Set<String> nHeaders = cache.keySet();
//                int count = 1;
//                UILogger.getUiLogger().logToBothUIAndTerminal("Starting parsing header files");
//                for (String systemPath : nHeaders) {
//                    UILogger.getUiLogger().logToBothUIAndTerminal("[" + count + "/" + nHeaders.size() + "] Start parsing " + systemPath);
//                    executor.execute(new SystemHeaderParseTask(systemRoot, systemPath));
//                    UILogger.getUiLogger().logToBothUIAndTerminal("[" + count + "/" + nHeaders.size() + "] Parsing " + systemPath + " ............................. done");
//
//                    count++;
//                }
//
//                UILogger.getUiLogger().logToBothUIAndTerminal("Shutdown system header file parsing engine");
//                executor.shutdown();
//
//                while (!executor.awaitTermination(1, TimeUnit.MINUTES));
//
//            } catch (Exception ex) {
//                ex.printStackTrace();
//            }
//
//        }

        @Override
        protected ProjectNode call() throws Exception {
            try {
                ProjectNode projectRoot = Environment.getInstance().getProjectNode();

                IncludeFilter filter = new IncludeFilter();
                filter.findAllIncluded(projectRoot);

                IncludeCache cache = IncludeCache.getInstance();

                ProjectNode systemRoot = new ProjectNode();
                systemRoot.setAbsolutePath("SYSTEM");

                Environment.getInstance().setSystemRoot(systemRoot);

                ExecutorService executor = Executors.newFixedThreadPool(cache.keySet().size());

                Set<String> nHeaders = cache.keySet();
                int count = 1;
                UILogger.getUiLogger().logToBothUIAndTerminal("Starting parsing header files");
                for (String systemPath : nHeaders) {
                    UILogger.getUiLogger().logToBothUIAndTerminal("[" + count + "/" + nHeaders.size() + "] Start parsing " + systemPath);
                    executor.execute(new SystemHeaderParseTask(systemRoot, systemPath));
                    UILogger.getUiLogger().logToBothUIAndTerminal("[" + count + "/" + nHeaders.size() + "] Parsing " + systemPath + " ............................. done");

                    count++;
                }

                UILogger.getUiLogger().logToBothUIAndTerminal("Shutdown system header file parsing engine");
                executor.shutdown();

                while (!executor.awaitTermination(10, TimeUnit.MINUTES));
                return systemRoot;

            } catch (Exception ex) {
                ex.printStackTrace();
                return null;
            }
        }
    }
}
