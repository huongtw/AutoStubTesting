package auto_testcase_generation.cte;

import auto_testcase_generation.cte.UI.CteClassificationTree.CteView;
import auto_testcase_generation.cte.UI.OptionTable.CteOptionTable;
import auto_testcase_generation.cte.core.ClassificationTree;
import auto_testcase_generation.cte.core.ClassificationTreeManager;
import com.dse.environment.Environment;
import com.dse.parser.ProjectParser;
import com.dse.parser.object.FunctionNode;
import com.dse.parser.object.ProjectNode;
import com.dse.search.Search;
import com.dse.search.condition.FunctionNodeCondition;
import com.dse.testcase_manager.TestCase;
import com.dse.testcase_manager.TestCaseManager;
import javafx.application.Application;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.ScrollPane;
import javafx.scene.effect.InnerShadow;
import javafx.scene.effect.InnerShadowBuilder;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextBuilder;
import javafx.stage.Stage;
import javafx.scene.shape.Rectangle;

import java.io.File;

public class CTEMain extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        primaryStage.setTitle("Keyboard");
        Group root = new Group();
        Scene scene = new Scene(root, 530, 300, Color.WHITE);

        final StringProperty statusProperty = new SimpleStringProperty();

        InnerShadow iShadow = InnerShadowBuilder.create()
                .offsetX(3.5f)
                .offsetY(3.5f)
                .build();
        final Text status = TextBuilder.create()
                .effect(iShadow)
                .x(100)
                .y(50)
                .fill(Color.LIME)
                .font(Font.font(null, FontWeight.BOLD, 35))
                .translateY(50)
                .build();
        status.textProperty().bind(statusProperty);
        statusProperty.set("Line\nLine2\nLine");
        root.getChildren().add(status);

        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args)
    {
        launch(args);
    }
}
