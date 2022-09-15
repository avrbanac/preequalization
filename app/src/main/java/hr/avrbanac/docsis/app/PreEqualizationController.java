package hr.avrbanac.docsis.app;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.chart.LineChart;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

public class PreEqualizationController {
    @FXML
    private TextField preEqStringInput;
    @FXML
    public TextArea metricsTextArea;
    @FXML
    public LineChart icfrLineChart;

    public void onCalculateClick(ActionEvent actionEvent) {
        System.out.println(preEqStringInput.getText());
    }
}
