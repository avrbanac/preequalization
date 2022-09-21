package hr.avrbanac.docsis.app;

import hr.avrbanac.docsis.lib.analysis.PreEqAnalysis;
import hr.avrbanac.docsis.lib.struct.Coefficient;
import hr.avrbanac.docsis.lib.struct.DefaultPreEqData;
import hr.avrbanac.docsis.lib.struct.PreEqData;
import hr.avrbanac.docsis.lib.util.ParsingUtility;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;

import java.util.List;

public class PreEqualizationController {
    @FXML
    private TextField preEqStringInput;
    @FXML
    private TableView<TableCoefficient> coefficientTable;
    @FXML
    private VBox leftTextVBox;
    @FXML
    private VBox rightTextVBox;
    @FXML
    private TapBarChart<String, Double> tapsBarChart;
    @FXML
    private LineChart<String, Double> icfrLineChart;

    /**
     * Plotting for this app happens only after pre-eq string has been provided. That is why it is safe to apply lookup inline styling to
     * the charts since css has already been parsed and applied.
     */
    public void onCalculateClick() {
        String inputString = preEqStringInput.getText();
        if (ParsingUtility.isPreEqStringValid(inputString, DefaultPreEqData.INPUT_STRING_LENGTH)) {
            PreEqData preEqData = new DefaultPreEqData(inputString);

            coefficientTable.setItems(getTableCoefficients(preEqData));
            addMetricsToVBoxes(preEqData);

            tapsBarChart.setData(getBarChartData(preEqData));
            icfrLineChart.setData(getICFRChartData(preEqData));
        } else {
            preEqStringInput.clear();
        }
    }

    /**
     * Helper method to fill up both {@link VBox} with metrics text.
     * @param preEqData {@link PreEqData} provided parsed pre-eq data
     */
    private void addMetricsToVBoxes(final PreEqData preEqData) {
        Text title = new Text("Key metrics:");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        VBox.setMargin(title, new Insets(0, 0, 10, 0));
        leftTextVBox.getChildren().setAll(title);

        Text invisibleTitle = new Text("");
        invisibleTitle.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        VBox.setMargin(invisibleTitle, new Insets(0, 0, 10, 0));
        rightTextVBox.getChildren().setAll(invisibleTitle);

        Text[] keys = new Text[] {
                new Text("MTE (main tap energy):"),
                new Text("MTNA (main tap nominal amplitude):"),
                new Text("MTNE (main tap nominal energy):"),
                new Text("preMTE (pre-main tap energy):"),
                new Text("postMTE (post-main tap energy):"),
                new Text("TTE (total tap energy):"),
                new Text("MTC (main tap compression):"),
                new Text("MTR (main tap ratio):"),
                new Text("NMTER (non-main tap to total energy):"),
                new Text("preMTTER (pre-main tap to total energy):"),
                new Text("postMTTER (post-main tap to total energy):"),
                new Text("PPESR (pre-post energy symetry ratio):"),
                new Text("PPTSR (pre-post tap symetry ratio):")
        };
        for (Text text : keys) {
            VBox.setMargin(text, new Insets(0, 0, 0, 10));
            leftTextVBox.getChildren().add(text);
        }

        Text[] values = new Text[] {
                new Text(String.valueOf(preEqData.getMTE())),
                new Text(String.valueOf(preEqData.getMTNA())),
                new Text(String.valueOf(preEqData.getMTNE())),
                new Text(String.valueOf(preEqData.getPreMTE())),
                new Text(String.valueOf(preEqData.getPostMTE())),
                new Text(String.valueOf(preEqData.getTTE())),
                new Text(preEqData.getMTC() + " dB"),
                new Text(preEqData.getMTR() + " dB"),
                new Text(preEqData.getNMTER() + " dB"),
                new Text(preEqData.getPreMTTER() + " dB"),
                new Text(preEqData.getPostMTTER() + " dB"),
                new Text(preEqData.getPPESR() + " dB"),
                new Text(preEqData.getPPTSR() + " dB"),
        };
        for (Text text : values) {
            rightTextVBox.getChildren().add(text);
        }
    }

    /**
     * Helper method to fetch table coefficients from preEqString.
     * @param preEqData {@link PreEqData} validated pre-eq string
     * @return {@link ObservableList} of {@link TableCoefficient} pojo elements created from pre-eq coefficients
     */
    private ObservableList<TableCoefficient> getTableCoefficients(final PreEqData preEqData) {
        List<Coefficient> coefficients = preEqData.getCoefficients();
        long lMTNA = preEqData.getMTNA();
        long lMTNE = preEqData.getMTNE();

        ObservableList<TableCoefficient> tableCoefficients = FXCollections.observableArrayList();
        for (int i = 0; i < coefficients.size(); i++) {
            tableCoefficients.add(new TableCoefficient(coefficients.get(i),i + 1, lMTNA, lMTNE));
        }

        return tableCoefficients;
    }

    /**
     * Returns stacked barchart data (for taps graph) with filled series. It needs to be stacked barchart instead of barchart since there is
     * no easy way to show negative values from bottom to top with barchart. This problem is solved by using stacked graph. There is an
     * invisible bar over each visible tap bar, spanning all the way up to the zero (Y value).
     * @param preEqData {@link PreEqData} provided parsed pre-eq data
     * @return {@link ObservableList} of {@link XYChart.Series}
     */
    private ObservableList<XYChart.Series<String, Double>> getBarChartData(final PreEqData preEqData) {
        long lMTNE = preEqData.getMTNE();
        ObservableList<XYChart.Series<String, Double>> barData = createChartSeries(2);

        preEqData.getCoefficients().forEach(coefficient -> {
            double value = Math.max(coefficient.getEnergyRatio(lMTNE), -60.0d);
            String index = String.valueOf(coefficient.getIndex());
            barData.get(0).getData().add(new XYChart.Data<>(index, value));
            barData.get(1).getData().add(new XYChart.Data<>(index, -60 - value));
        });

        return barData;
    }

    /**
     * Returns line chart data (for ICFR graph) with filled series. Instead of coloring parts of line chart's background, colored additional
     * line boundaries are added to chart.
     * @param preEqData {@link PreEqData} provided parsed pre-eq data
     * @return {@link ObservableList} of {@link XYChart.Series}
     */
    private ObservableList<XYChart.Series<String, Double>> getICFRChartData(final PreEqData preEqData) {
        double[] icfrPoints = new PreEqAnalysis(preEqData).getInChannelFrequencyResponseMagnitude();
        ObservableList<XYChart.Series<String, Double>> lineData = createChartSeries(1);

        for (int i = 0; i < icfrPoints.length; i++) {
            lineData.get(0).getData().add(new XYChart.Data<>(String.valueOf(i + 1), icfrPoints[i]));
        }

        return lineData;
    }

    /**
     * Helper method to fill up chart data with series.
     * @param numberOfSeries int number of series to be added to chart data
     * @return {@link ObservableList} of {@link XYChart.Series}
     */
    ObservableList<XYChart.Series<String, Double>> createChartSeries(final int numberOfSeries) {
        ObservableList<XYChart.Series<String, Double>> data = FXCollections.observableArrayList();
        for (int i = 0; i < numberOfSeries; i++) {
            data.add(new XYChart.Series<>());
        }

        return data;
    }
}
