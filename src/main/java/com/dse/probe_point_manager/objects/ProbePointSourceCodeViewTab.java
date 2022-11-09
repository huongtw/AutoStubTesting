package com.dse.probe_point_manager.objects;

import com.dse.parser.object.ISourcecodeFileNode;
import com.dse.parser.object.SourcecodeFileNode;
import com.dse.project_init.ProjectClone;
import javafx.collections.FXCollections;
import javafx.collections.ObservableSet;
import javafx.scene.control.Tab;
import javafx.scene.layout.StackPane;
import org.fxmisc.richtext.CodeArea;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;

public class ProbePointSourceCodeViewTab extends Tab {
    private CodeArea codeEditor;
    private ISourcecodeFileNode sourcecodeFileNode;

    public ISourcecodeFileNode getSourcecodeFileNode() {
        return sourcecodeFileNode;
    }

    public ProbePointSourceCodeViewTab(ISourcecodeFileNode sourcecodeFileNode, TreeSet<Integer> probePointSet) {
        setText(sourcecodeFileNode.getName());
        this.sourcecodeFileNode = sourcecodeFileNode;
        StackPane codeAreaPane = new StackPane();

        if (probePointSet == null){
            probePointSet = new TreeSet<>();
        }

        ObservableSet<Integer> observableProbePointLines = FXCollections.synchronizedObservableSet(FXCollections.observableSet(probePointSet));
        String path = sourcecodeFileNode.getAbsolutePath();
        codeEditor = ProbePointFXCodeView.getCodeEditor(codeAreaPane, observableProbePointLines,sourcecodeFileNode);
        codeEditor.replaceText(0, 0, this.readData(path));
        codeEditor.setEditable(false);
        codeEditor.getStylesheets().add(Object.class.getResource("/css/keywords.css").toExternalForm());
        this.setContent(codeAreaPane);
    }

    private String readData(String path) {
        List<String> data = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(path))) {

            String sCurrentLine;

            while ((sCurrentLine = br.readLine()) != null) {
                data.add(sCurrentLine);
            }

        } catch (IOException e) {
              e.printStackTrace();
        }
        String res = String.join("\n", data);
        res = ProjectClone.simplify(res);
        return res;
    }

}