package hr.avrbanac.docsis.app;

import hr.avrbanac.docsis.lib.analysis.PreEqAnalysis;
import hr.avrbanac.docsis.lib.struct.Coefficient;
import hr.avrbanac.docsis.lib.struct.DefaultPreEqData;
import hr.avrbanac.docsis.lib.struct.PreEqData;
import hr.avrbanac.docsis.lib.util.ParsingUtility;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

import java.util.List;

public class PreEqualizationController {
    private static final String CHART_SERIES_SELECTOR = ".chart-series-line";
    @FXML
    private TextField preEqStringInput;
    @FXML
    private TableView<TableCoefficient> coefficientTable;
    @FXML
    private TextArea metricsTextArea;
    @FXML
    private BarChart<String, Double> tapsBarChart;
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
            metricsTextArea.setText(ParsingUtility.preEqDataToMetricsToString(preEqData));

            tapsBarChart.setData(getBarChartData(preEqData));
            for (int i = 0; i < preEqData.getTapCount(); i++) {
                tapsBarChart.lookup(".data"+ i +".chart-bar").setStyle("-fx-bar-fill: #2caede");
            }

            icfrLineChart.setData(getICFRChartData(preEqData));
            icfrLineChart.getData().get(0).getNode().lookup(CHART_SERIES_SELECTOR).setStyle("-fx-stroke: #2caede");
            icfrLineChart.getData().get(1).getNode().lookup(CHART_SERIES_SELECTOR).setStyle("-fx-stroke: #58b758");
            icfrLineChart.getData().get(2).getNode().lookup(CHART_SERIES_SELECTOR).setStyle("-fx-stroke: #58b758");
            icfrLineChart.getData().get(3).getNode().lookup(CHART_SERIES_SELECTOR).setStyle("-fx-stroke: #fba81d");
            icfrLineChart.getData().get(4).getNode().lookup(CHART_SERIES_SELECTOR).setStyle("-fx-stroke: #fba81d");

        } else {
            preEqStringInput.clear();
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

    private ObservableList<XYChart.Series<String, Double>> getBarChartData(final PreEqData preEqData) {
        long lMTNE = preEqData.getMTNE();
        ObservableList<XYChart.Series<String, Double>> barData = FXCollections.observableArrayList();
        XYChart.Series<String, Double> series = new XYChart.Series<>();
        barData.add(series);

        preEqData.getCoefficients().forEach(coefficient ->
                series.getData().add(
                        new XYChart.Data<>(
                                String.valueOf(coefficient.getIndex()),
                                60d + Math.max(coefficient.getEnergyRatio(lMTNE), -60.0d))));

        return barData;
    }

    /**
     * Calculates ICFR data {@link PreEqAnalysis#getInChannelFrequencyResponseMagnitude()} for provided {@link PreEqData}. Suppressed
     * warning for unchecked generic array creation for varargs parameter since there is a clear definition of what will be added to
     * line data.
     * @param preEqData {@link PreEqData} provided validate and parsed pre-eq data
     * @return {@link ObservableList} of {@link XYChart.Series}
     */
    @SuppressWarnings("unchecked")
    private ObservableList<XYChart.Series<String, Double>> getICFRChartData(final PreEqData preEqData) {
        double[] icfrPoints = new PreEqAnalysis(preEqData).getInChannelFrequencyResponseMagnitude();
        ObservableList<XYChart.Series<String, Double>> lineData = FXCollections.observableArrayList();
        XYChart.Series<String, Double> series0 = new XYChart.Series<>();
        XYChart.Series<String, Double> series1 = new XYChart.Series<>();
        XYChart.Series<String, Double> series2 = new XYChart.Series<>();
        XYChart.Series<String, Double> series3 = new XYChart.Series<>();
        XYChart.Series<String, Double> series4 = new XYChart.Series<>();
        lineData.addAll(series0, series1, series2, series3, series4);

        for (int i = 0; i < icfrPoints.length; i++) {
            series0.getData().add(new XYChart.Data<>(String.valueOf(i), icfrPoints[i]));
            series1.getData().add(new XYChart.Data<>(String.valueOf(i), 1d));
            series2.getData().add(new XYChart.Data<>(String.valueOf(i), -1d));
            series3.getData().add(new XYChart.Data<>(String.valueOf(i), 2d));
            series4.getData().add(new XYChart.Data<>(String.valueOf(i), -2d));
        }

        return lineData;
    }
}
