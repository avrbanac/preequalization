package hr.avrbanac.docsis.app;

import javafx.beans.NamedArg;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.chart.Axis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

/**
 * Extended version of the {@link LineChart} since there is a need for partial background color fill and line chart does not support that
 * feature.
 */
public class ICFRLineChart extends LineChart<String, Double> {
    /**
     * This list of added rectangles is here to make sure all the additional rectangles added to chart are removed before adding new ones.
     * If app is reused (calculating/plotting multiple pre-eq strings), this mechanism makes sure there are no overlapping transparent
     * leaking objects.
     */
    private final List<Rectangle> addedRectangles = new ArrayList<>();

    /**
     * The only needed CTOR for the current implementation.
     * @param xAxis {@link Axis} axis ({@link String} values)
     * @param yAxis {@link Axis} axis ({@link Double} values)
     */
    public ICFRLineChart(
            @NamedArg("xAxis") final Axis<String> xAxis,
            @NamedArg("yAxis") final Axis<Double> yAxis) {

        super(xAxis, yAxis);
    }

    /**
     * Sets line chart data (for ICFR graph) with filled series.
     * @param icfrPoints array of doubles with provided parsed pre-eq data after FFT analysis
     * @param channelWidth float value for the channel width in MHz
     */
    public void setICFRData(
            final double[] icfrPoints,
            final float channelWidth) {

        float increment = channelWidth / (icfrPoints.length - 1);
        ObservableList<XYChart.Series<String, Double>> lineData = FXCollections.observableArrayList();
        XYChart.Series<String, Double> series = new XYChart.Series<>();
        lineData.add(series);
        for (int i = 0; i < icfrPoints.length; i++) {
            series.getData().add(new XYChart.Data<>(String.format("%.2f", -1f * channelWidth / 2 + increment * i), icfrPoints[i]));
        }

        setData(lineData);
    }

    /**
     * Overloaded method will plot original data but also background polygons.
     */
    @Override
    protected void layoutPlotChildren() {
        super.layoutPlotChildren();

        if (getData().size() != 0) {
            Series<String, Double> series = getData().get(0);
            ObservableList<Data<String, Double>> listOfData = series.getData();
            double x0 = getXAxis().getDisplayPosition(listOfData.get(0).getXValue());
            double width = getXAxis().getDisplayPosition(listOfData.get(listOfData.size() - 1).getXValue()) - x0;
            double y4p = getYAxis().getDisplayPosition(4d);
            double y2p = getYAxis().getDisplayPosition(2d);
            double y1p = getYAxis().getDisplayPosition(1d);
            double y1n = getYAxis().getDisplayPosition(-1d);
            double y2n = getYAxis().getDisplayPosition(-2d);
            double y4n = getYAxis().getDisplayPosition(-4d);

            Rectangle recRed1 = new Rectangle(x0, y4p, width, y2p - y4p);
            recRed1.setFill(Color.RED);
            Rectangle recRed2 = new Rectangle(x0, y2n, width, y4n - y2n);
            recRed2.setFill(Color.RED);
            Rectangle recYel1 = new Rectangle(x0, y2p, width, y1p - y2p);
            recYel1.setFill(Color.YELLOW);
            Rectangle recYel2 = new Rectangle(x0, y1n, width, y2n - y1n);
            recYel2.setFill(Color.YELLOW);
            Rectangle recGrn0 = new Rectangle(x0, y1p, width, y1n - y1p);
            recGrn0.setFill(Color.GREEN);
            Rectangle[] rectangles = new Rectangle[]{recRed1, recRed2, recYel1, recYel2, recGrn0};

            ListIterator<Rectangle> iterator = addedRectangles.listIterator();
            while(iterator.hasNext()) {
                Rectangle rectangle = iterator.next();
                getPlotChildren().remove(rectangle);
                iterator.remove();
            }

            for (Rectangle rectangle : rectangles) {
                rectangle.setOpacity(0.1d);
                rectangle.toBack();
                addedRectangles.add(rectangle);
                getPlotChildren().add(rectangle);
            }
            ObservableList<Axis.TickMark<String>> tickMarks = getXAxis().getTickMarks();
            int size = tickMarks.size();
            for (int i = 0; i < size; i++) {
                tickMarks.get(i).setTextVisible((i < size / 2 && i % 2 == 0) || (i > size / 2 && i % 2 == 1));
            }
        }
    }
}
