package com.dse.probe_point_manager.objects;

import com.dse.debugger.utils.CodeViewHelpers;
import com.dse.guifx_v3.helps.UIController;
import com.dse.parser.object.ISourcecodeFileNode;
import com.dse.parser.object.SourcecodeFileNode;
import com.dse.probe_point_manager.ProbePointManager;
import com.dse.probe_point_manager.controllers.ProbePointsOfALineController;
import javafx.collections.ObservableSet;
import javafx.collections.WeakSetChangeListener;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.fxmisc.richtext.CodeArea;

import java.util.List;
import java.util.function.IntFunction;

public class ProbePointFactory implements IntFunction<Node> {

    // TAG_BREAK to highlight line of code
    private static final String TAG_BREAK = "break";
    private static Background DEFAULT_BACKGROUND = new Background(new BackgroundFill(Color.web("#ddd"), null, null));
    private final CodeArea area;
    private ISourcecodeFileNode sourcecodeFileNode;
    private int currentSelectedLine = -1;

    private ObservableSet<Integer> list;

    public ProbePointFactory(CodeArea area, ObservableSet<Integer> list, ISourcecodeFileNode sourcecodeFileNode) {
        this.area = area;
        this.list = list;
        this.sourcecodeFileNode = sourcecodeFileNode;
    }

    @Override
    public Node apply(int value) {
        Image icon = new Image(ProbePointFXCodeView.class.getResourceAsStream("/icons/open_source/probe_point_16px.png"));
        ImageView imageView = new ImageView(icon);
        if (!list.contains(value))
            imageView.setVisible(false);

        StackPane pane = new StackPane();
        StackPane.setAlignment(imageView, Pos.CENTER);
        pane.setPrefSize(15, 15);
        pane.getChildren().add(imageView);
        pane.setBackground(DEFAULT_BACKGROUND);
        pane.setCursor(Cursor.HAND);

        pane.setOnMouseClicked(event -> {
            if (ProbePointFXCodeView.isInstructionLine(area.getParagraph(value).getText())) {
                CodeViewHelpers.addParagraphStyle(area, value, TAG_BREAK);
                list.remove(value);
                list.add(value);

                ProbePointsOfALineController controller = ProbePointsOfALineController.getInstance(sourcecodeFileNode, value, area);
                if (controller != null) {
                    Stage stage = controller.getStage();
                    if (stage != null) {
                        stage.setResizable(false);
                        stage.initModality(Modality.WINDOW_MODAL);
                        stage.initOwner(UIController.getPrimaryStage().getScene().getWindow());
                        stage.setOnCloseRequest(e -> {
                            List<ProbePoint> probePoints = ProbePointManager.getInstance().searchProbePoints(sourcecodeFileNode, value);
                            if (probePoints.size() == 0)
                            {
                                list.add(value);
                                list.remove(value);
                            }
                        });
                        stage.show();
                    }
                }
            }
        });

        list.addListener(
                new WeakSetChangeListener<>(
                        change -> {
                            if (list.contains(value)) {
                                imageView.setVisible(true);
                                if (change.getElementAdded() != null && change.getElementAdded().intValue() != value) {
                                    CodeViewHelpers.tryRemoveParagraphStyle(area, value, TAG_BREAK);
                                }
                            } else {
                                imageView.setVisible(false);
                                CodeViewHelpers.tryRemoveParagraphStyle(area, value, TAG_BREAK);
                            }
                        }));

        return pane;
    }
}
