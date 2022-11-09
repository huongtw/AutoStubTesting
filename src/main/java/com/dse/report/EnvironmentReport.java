package com.dse.report;

import com.dse.config.WorkspaceConfig;
import com.dse.environment.EnvironmentSearch;
import com.dse.environment.object.*;
import com.dse.environment.Environment;
import com.dse.parser.object.INode;
import com.dse.report.element.*;

import java.io.File;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

public class EnvironmentReport extends ReportView {
    private Environment environment;

    public EnvironmentReport(Environment environment, LocalDateTime creationDateTime) {
        // set report name
        super(String.format("Environment %s Report", environment.getName()));

        // set report attributes
        this.environment = environment;
        this.creationDateTime = creationDateTime;

        // set report location path to default
        setPathDefault();

        // generate test case report
        generate();
    }

    protected void generate() {
        // STEP 1: generate table of contents section
        sections.add(generateTableOfContents());

        // STEP 2: generate configuration data section
        sections.add(generateConfigurationData());

        // STEP 3:
        sections.add(generateCompilerConfig());

        // STEP 4:
        sections.add(generateBuildConfig());

        // STEP 5:
        sections.add(generateSourcesLocation());

        // STEP 6:
        sections.add(generateUUTsAndStubsConfig());

    }

    protected TableOfContents generateTableOfContents() {
        TableOfContents tableOfContents = new TableOfContents();

        tableOfContents.getBody().add(new TableOfContents.Item("Configuration Data", "config-data"));
        tableOfContents.getBody().add(new TableOfContents.Item("Compiler Configuration", "compiler-config"));
        tableOfContents.getBody().add(new TableOfContents.Item("Build Configuration", "build-config"));
        tableOfContents.getBody().add(new TableOfContents.Item("Source Codes Location", "sources-location"));
        tableOfContents.getBody().add(new TableOfContents.Item("UUTs & Stubs Configuration", "uuts-n-stubs"));
//        tableOfContents.getBody().add(new TableOfContents.Item("User Code", "user-code"));

        return tableOfContents;
    }

    protected Section generateUUTsAndStubsConfig() {
        Section section = new Section("uuts-n-stubs");

        section.getTitle().add(new Section.Line("UUTs & Stubs Configuration", COLOR.DARK));

        List<INode> uuts = Environment.getInstance().getUUTs();
        List<INode> sbfs = Environment.getInstance().getSBFs();
        List<INode> stubs = Environment.getInstance().getStubs();

        Table table = new Table(false);

        for (INode source : uuts) {
            Table.Row row = new Table.Row(source.getName());

            if (uuts.indexOf(source) == 0)
                row.getCells().add(0, new Table.Cell<Text>("Unit Under Test (UUT):"));
            else
                row.getCells().add(0, new Table.Cell<Text>(""));

            table.getRows().add(row);
        }

        for (INode source : sbfs) {
            Table.Row row = new Table.Row(source.getName());

            if (sbfs.indexOf(source) == 0)
                row.getCells().add(0, new Table.Cell<Text>("Unit Under Test (SBF):"));
            else
                row.getCells().add(0, new Table.Cell<Text>(""));

            table.getRows().add(row);
        }

        for (INode source : stubs) {
            Table.Row row = new Table.Row(source.getName());

            if (stubs.indexOf(source) == 0)
                row.getCells().add(0, new Table.Cell<Text>("Stub:"));
            else
                row.getCells().add(0, new Table.Cell<Text>(""));

            table.getRows().add(row);
        }

        EnviroLibraryStubNode stubLibs = (EnviroLibraryStubNode) EnvironmentSearch
                .searchNode(environment.getEnvironmentRootNode(), new EnviroLibraryStubNode())
                .stream()
                .findFirst()
                .orElse(null);

        if (stubLibs != null) {
            List<String> libraries = stubLibs.getLibraryNames();

            for (String lib : libraries) {
                Table.Row row = new Table.Row(lib);

                if (libraries.indexOf(lib) == 0)
                    row.getCells().add(0, new Table.Cell<Text>("Stub Library:"));
                else
                    row.getCells().add(0, new Table.Cell<Text>(""));

                table.getRows().add(row);
            }
        }

        section.getBody().add(table);

        section.getBody().add(new Section.BlankLine());

        return section;
    }

    protected Section generateSourcesLocation() {
        Section section = new Section("sources-location");

        section.getTitle().add(new Section.Line("Source Codes Location", COLOR.DARK));

        List<EnviroSearchListNode> searchList = EnvironmentSearch
                .searchNode(environment.getEnvironmentRootNode(), new EnviroSearchListNode())
                .stream()
                .map(n -> (EnviroSearchListNode) n)
                .collect(Collectors.toList());

        List<EnviroLibraryIncludeDirNode> includeList = EnvironmentSearch
                .searchNode(environment.getEnvironmentRootNode(), new EnviroLibraryIncludeDirNode())
                .stream()
                .map(n -> (EnviroLibraryIncludeDirNode) n)
                .collect(Collectors.toList());

        List<EnviroTypeHandledSourceDirNode> typeHandlerList = EnvironmentSearch
                .searchNode(environment.getEnvironmentRootNode(), new EnviroTypeHandledSourceDirNode())
                .stream()
                .map(n -> (EnviroTypeHandledSourceDirNode) n)
                .collect(Collectors.toList());

        Table table = new Table(false);

        for (EnviroSearchListNode dir : searchList) {
            Table.Row row = new Table.Row(dir.getSearchList());

            if (searchList.indexOf(dir) == 0)
                row.getCells().add(0, new Table.Cell<Text>("Search List:"));
            else
                row.getCells().add(0, new Table.Cell<Text>(""));

            table.getRows().add(row);
        }

        for (EnviroLibraryIncludeDirNode dir : includeList) {
            Table.Row row = new Table.Row(dir.getLibraryIncludeDir());

            if (includeList.indexOf(dir) == 0)
                row.getCells().add(0, new Table.Cell<Text>("Library Include List:"));
            else
                row.getCells().add(0, new Table.Cell<Text>(""));

            table.getRows().add(row);
        }

        for (EnviroTypeHandledSourceDirNode dir : typeHandlerList) {
            Table.Row row = new Table.Row(dir.getTypeHandledSourceDir());

            if (typeHandlerList.indexOf(dir) == 0)
                row.getCells().add(0, new Table.Cell<Text>("Type Handler List:"));
            else
                row.getCells().add(0, new Table.Cell<Text>(""));

            table.getRows().add(row);
        }

        section.getBody().add(table);

        section.getBody().add(new Section.BlankLine());

        return section;
    }

    protected Section generateBuildConfig() {
        Section section = new Section("build-config");

        section.getTitle().add(new Section.Line("Build Configuration", COLOR.DARK));

        EnviroWhiteBoxNode whiteBox = (EnviroWhiteBoxNode) EnvironmentSearch
                .searchNode(environment.getEnvironmentRootNode(), new EnviroWhiteBoxNode())
                .stream()
                .findFirst()
                .orElse(null);

        Table table = new Table();

        if (whiteBox == null) {
            table.getRows().add(new Table.Row("Cant find any white box configuration"));

        } else {
            table.getRows().add(new Table.Row("Coverage Type:", environment.getTypeofCoverage()));
            table.getRows().add(new Table.Row("White Box:", whiteBox.isActive() ? "YES" : "NO"));
        }

        section.getBody().add(table);

        section.getBody().add(new Section.BlankLine());

        return section;
    }

    protected Section generateCompilerConfig() {
        Section section = new Section("compiler-config");

        section.getTitle().add(new Section.Line("Compiler Configuration", COLOR.DARK));

        EnviroCompilerNode compilerNode = (EnviroCompilerNode) EnvironmentSearch
                .searchNode(environment.getEnvironmentRootNode(), new EnviroCompilerNode())
                .stream()
                .findFirst()
                .orElse(null);

        Table table = new Table();

        if (compilerNode == null) {
            table.getRows().add(new Table.Row("Cant find any compiler configuration"));

        } else {
            table.getRows().add(new Table.Row(new Table.SpanCell<Text>("Preprocessor/Compiler", COLOR.MEDIUM, 2)));

            table.getRows().add(new Table.Row("Compiler Name:", compilerNode.getName()));
            table.getRows().add(new Table.Row("Preprocessor Command:", compilerNode.getPreprocessCmd()));
            table.getRows().add(new Table.Row("Include Flag:", compilerNode.getIncludeFlag()));
            table.getRows().add(new Table.Row("Define Flag:", compilerNode.getDefineFlag()));
            table.getRows().add(new Table.Row("Compile Command:", compilerNode.getCompileCmd()));

            List<EnviroDefinedVariableNode> defines = EnvironmentSearch
                    .searchNode(environment.getEnvironmentRootNode(), new EnviroDefinedVariableNode())
                    .stream()
                    .map(n -> (EnviroDefinedVariableNode) n)
                    .collect(Collectors.toList());

            for (EnviroDefinedVariableNode define : defines) {
                Table.Row row = new Table.Row(define.toString());

                if (defines.indexOf(define) == 0)
                    row.getCells().add(0, new Table.Cell<Text>("Defined Variable:"));
                else
                    row.getCells().add(0, new Table.Cell<Text>(""));

                table.getRows().add(row);
            }

            table.getRows().add(new Table.Row(new Table.SpanCell<Text>("Linker/Debugger", COLOR.MEDIUM, 2)));

            table.getRows().add(new Table.Row("Output File Flag:", compilerNode.getOutputFlag()));
            table.getRows().add(new Table.Row("Output File Extension:", compilerNode.getOutputExt()));
            table.getRows().add(new Table.Row("Output File Extension:", compilerNode.getOutputExt()));
            table.getRows().add(new Table.Row("Linker Command:", compilerNode.getLinkCmd()));
            table.getRows().add(new Table.Row("Debugger Command:", compilerNode.getDebugCmd()));
        }

        section.getBody().add(table);

        section.getBody().add(new Section.BlankLine());

        return section;
    }

    @Override
    protected Section generateConfigurationData() {
        Section section = new Section("config-data");

        section.getTitle().add(new Section.Line("Configuration Data", COLOR.DARK));

        Table table = new Table();

        table.getRows().add(new Table.Row("This report include data for: ", environment.getName()));
        table.getRows().add(new Table.Row("Date of Report Creation:", getCreationDate()));
        table.getRows().add(new Table.Row("Time of Report Creation:", getCreationTime()));
        section.getBody().add(table);

        section.getBody().add(new Section.BlankLine());

        return section;
    }

    protected void setPathDefault() {
        path = new WorkspaceConfig().fromJson().getReportDirectory() + File.separator + environment.getName() + ".html";
    }
}
