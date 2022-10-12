package hr.avrbanac.docsis.app;

import hr.avrbanac.docsis.lib.analysis.ChannelWidth;
import hr.avrbanac.docsis.lib.analysis.PreEqAnalysis;
import hr.avrbanac.docsis.lib.analysis.Signature;
import hr.avrbanac.docsis.lib.struct.Coefficient;
import hr.avrbanac.docsis.lib.struct.DefaultPreEqData;
import hr.avrbanac.docsis.lib.struct.PreEqData;
import hr.avrbanac.docsis.lib.util.ParsingUtility;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import org.apache.commons.math3.util.Precision;

import java.util.List;

public class PreEqualizationController {
    private static final String FONT_FAMILY = "Arial";
    public static final int ROUND_SCALE = 5;
    @FXML
    private TextField preEqStringInput;
    @FXML
    private ToggleGroup channelWidthGroup;
    @FXML
    private TableView<TableCoefficient> coefficientTable;
    @FXML
    private VBox leftTextVBox;
    @FXML
    private VBox rightTextVBox;
    @FXML
    private TapBarChart tapsBarChart;
    @FXML
    private ICFRLineChart icfrLineChart;
    @FXML
    private Label signature;

    private ChannelWidth channelWidth = ChannelWidth.CW_US_6_4;

    /**
     * Plotting for this app happens only after pre-eq string has been provided. That is why it is safe to apply lookup inline styling to
     * the charts since css has already been parsed and applied.
     */
    public void onCalculateClick() {
        String inputString = preEqStringInput.getText();
        channelWidth = ChannelWidth.valueOf(channelWidthGroup.getSelectedToggle().getUserData().toString());
        if (ParsingUtility.isPreEqStringValid(inputString, DefaultPreEqData.INPUT_STRING_LENGTH)) {
            PreEqData preEqData = new DefaultPreEqData(inputString);
            PreEqAnalysis preEqAnalysis = new PreEqAnalysis(preEqData);

            coefficientTable.setItems(getTableCoefficients(preEqData));
            addMetricsToVBoxes(preEqData, preEqAnalysis.getTDR(channelWidth, 2, false));

            tapsBarChart.setBarChartData(preEqData);
            icfrLineChart.setICFRData(preEqAnalysis.getInChannelFrequencyResponseMagnitude(), channelWidth.getValue());

            addMRSeverity(preEqAnalysis);
        } else {
            preEqStringInput.clear();
        }
    }

    /**
     * Helper method to fill up both {@link VBox} with metrics text.
     * @param preEqData {@link PreEqData} provided parsed pre-eq data
     */
    private void addMetricsToVBoxes(
            final PreEqData preEqData,
            final double tdr) {

        Text title = new Text("Key metrics:");
        title.setFont(Font.font(FONT_FAMILY, FontWeight.BOLD, 14));
        VBox.setMargin(title, new Insets(0, 0, 5, 0));
        leftTextVBox.getChildren().setAll(title);

        Text invisibleTitle = new Text("");
        invisibleTitle.setFont(Font.font(FONT_FAMILY, FontWeight.BOLD, 14));
        VBox.setMargin(invisibleTitle, new Insets(0, 0, 5, 0));
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
                new Text("PPTSR (pre-post tap symetry ratio):"),
                new Text("TDR (time domain reflectometry - (" + channelWidth.getValue() + " Mhz)):")
        };
        for (Text text : keys) {
            VBox.setMargin(text, new Insets(0, 0, 0, 10));
            leftTextVBox.getChildren().add(text);
        }

        Text[] values = new Text[]{
                new Text(String.valueOf(preEqData.getMTE())),
                new Text(String.valueOf(preEqData.getMTNA())),
                new Text(String.valueOf(preEqData.getMTNE())),
                new Text(String.valueOf(preEqData.getPreMTE())),
                new Text(String.valueOf(preEqData.getPostMTE())),
                new Text(String.valueOf(preEqData.getTTE())),
                new Text(Precision.round(preEqData.getMTC(), ROUND_SCALE) + " dB"),
                new Text(Precision.round(preEqData.getMTR(), ROUND_SCALE) + " dB"),
                new Text(Precision.round(preEqData.getNMTER(), ROUND_SCALE) + " dB"),
                new Text(Precision.round(preEqData.getPreMTTER(), ROUND_SCALE) + " dB"),
                new Text(Precision.round(preEqData.getPostMTTER(), ROUND_SCALE) + " dB"),
                new Text(Precision.round(preEqData.getPPESR(), ROUND_SCALE) + " dB"),
                new Text(Precision.round(preEqData.getPPTSR(), ROUND_SCALE) + " dB"),
                new Text(Precision.round(tdr, 2) + " m")
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
            tableCoefficients.add(new TableCoefficient(coefficients.get(i),i + 1, lMTNA, lMTNE, ROUND_SCALE));
        }

        return tableCoefficients;
    }

    /**
     * Helper method to deal with MR and severity label.
     * @param preEqAnalysis {@link PreEqAnalysis} needed as severity data
     */
    private void addMRSeverity(final PreEqAnalysis preEqAnalysis) {
        Signature preEqSignature = preEqAnalysis.getSignature();
        int severity = preEqSignature.getMicroReflectionSeverity().getLevel();
        signature.setFont(Font.font(FONT_FAMILY, FontWeight.BOLD, 12));
        signature.setTextFill(calculateSeverityColor(severity));
        signature.setText(String.format("Severity: %s, calculated micro-reflection: %.2f",
                preEqSignature.getMicroReflectionSeverity().getName(),
                preEqSignature.getMicroReflection()));
    }

    private Color calculateSeverityColor(final int severity) {
        switch (severity) {
            case 2: return Color.DARKRED;
            case 1: return Color.GOLDENROD;
            default: return Color.DARKGREEN;
        }
    }
}
