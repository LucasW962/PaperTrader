package papertrader.UI;

import javafx.scene.control.Button;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;

import java.util.List;

import java.awt.*;

public class Portfolio extends VBox {

    private String filterSetting = "Start";

    private final List<String> filterButtons = List.of("Start of Game", "This Year", "Past Month", "Last Week");

    Portfolio() {
        super(10);
        MakeButtons();
    }

    private void MakeButtons() {
        for (String buttonName : filterButtons) {
            Button button = new Button(buttonName);
            button.setFont(Window.SMALL_TEXT);
            this.getChildren().add(button);
        }
    }
}