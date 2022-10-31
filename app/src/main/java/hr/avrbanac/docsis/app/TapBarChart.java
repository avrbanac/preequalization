package hr.avrbanac.docsis.app;

import hr.avrbanac.docsis.lib.struct.PreEqData;
import javafx.beans.NamedArg;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.chart.Axis;
import javafx.scene.chart.StackedBarChart;
import javafx.scene.chart.XYChart;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

/**
 * Original {@link StackedBarChart} class is currently broken. It works fine with positive values, but negative values tend to be
 * overwritten. This happens because negative values are defined using CSS class "negative", which at some point gets overwritten by using
 * style .setAll() method instead of .addAll() method. Fixed version of the class takes care of this problem by overwriting broken methods.
 * Also, additional lines were added to barchart, since currently there is no FX chart that can plot bar data together with line data.
 * Generics for super class are also fixed.
 */
public class TapBarChart extends StackedBarChart<String, Double> {

    /**
     * This list of added lines is here to make sure all the additional lines added to chart are removed before adding new ones.
     * If app is reused (calculating/plotting multiple pre-eq strings), this mechanism makes sure there are no overlapping/leaking objects.
     */
    private final List<Line> addedLines = new ArrayList<>();
    private double nearMicroreflectionBoundary = -25d;
    private double farMicroreflectionBoundary = -35d;
    private int numberOfNearTaps = 2;

    /**
     * The only needed CTOR for this fixed version of the barchart.
     * @param xAxis {@link Axis} axis ({@link String} values)
     * @param yAxis {@link Axis} axis ({@link Double} values)
     */
    public TapBarChart(
            @NamedArg("xAxis") final Axis<String> xAxis,
            @NamedArg("yAxis") final Axis<Double> yAxis) {

        super(xAxis, yAxis);
    }

    public double getNearMicroreflectionBoundary() {
        return nearMicroreflectionBoundary;
    }

    public void setNearMicroreflectionBoundary(double nearMicroreflectionBoundary) {
        this.nearMicroreflectionBoundary = nearMicroreflectionBoundary;
    }

    public double getFarMicroreflectionBoundary() {
        return farMicroreflectionBoundary;
    }

    public void setFarMicroreflectionBoundary(double farMicroreflectionBoundary) {
        this.farMicroreflectionBoundary = farMicroreflectionBoundary;
    }

    public int getNumberOfNearTaps() {
        return numberOfNearTaps;
    }

    public void setNumberOfNearTaps(int numberOfNearTaps) {
        this.numberOfNearTaps = numberOfNearTaps;
    }

    /**
     * Sets stacked barchart data (for taps graph) with filled series. It needs to be stacked barchart instead of barchart since there is
     * no easy way to show negative values from bottom to top with barchart. This problem is solved by using stacked graph. There is an
     * invisible bar over each visible tap bar, spanning in Y value all the way up to the zero.
     * @param preEqData {@link PreEqData} provided parsed pre-eq data
     */
    public void setBarChartData(final PreEqData preEqData) {
        long lMTNE = preEqData.getMTNE();
        ObservableList<XYChart.Series<String, Double>> barData = FXCollections.observableArrayList();
        XYChart.Series<String, Double> invisibleSeries = new XYChart.Series<>();
        barData.add(invisibleSeries);
        XYChart.Series<String, Double> visibleSeries = new XYChart.Series<>();
        barData.add(visibleSeries);

        preEqData.getCoefficients().forEach(coefficient -> {
            double value = Math.max(coefficient.getNominalEnergyRatio(lMTNE), -60.0d);
            String index = String.valueOf(coefficient.getIndex());
            invisibleSeries.getData().add(new XYChart.Data<>(index, value));
            visibleSeries.getData().add(new XYChart.Data<>(index, -60 - value));
        });

        setData(barData);
    }

    /**
     * Override the method that breaks the graph, patched to add missing "negative" CSS class.
     * @param series {@link Series} provided series to add (forwarded to super)
     * @param itemIndex int index of the item (forwarded to super)
     * @param item {@link Data} (forwarded to super), also used to determine whether "negative" style class needs to be added
     */
    @Override
    protected void dataItemAdded(
            final Series<String, Double> series,
            final int itemIndex,
            final Data<String, Double> item) {

        super.dataItemAdded(series, itemIndex, item);

        double value = item.getYValue() != null ? item.getYValue() : 1d;

        if (value < 0) {
            // add missing CSS class for negative values if value is not-null
            item.getNode().getStyleClass().add("negative");
        }
    }

    /**
     * Override the method that breaks the graph - patched, so it doesn't override styles. Suppressed warnings, it seems sonar got lost here
     * for some reason. Maybe needs additional checking.
     * @param c {@link ListChangeListener.Change} just overriding original method (don't need this data here)
     */
    @SuppressWarnings("squid:S1854")
    @Override
    protected void seriesChanged(final ListChangeListener.Change<? extends Series> c) {
        for (int i = 0; i < getData().size(); i++) {
            List<Data<String, Double>> items = getData().get(i).getData();
            for (int j = 0; j < items.size(); j++) {
                Node bar = items.get(j).getNode();
                // change .setAll to .addAll to avoid overriding styles
                bar.getStyleClass().removeIf(s -> s.matches("chart-bar|(series|data)\\d+"));
                bar.getStyleClass().addAll("chart-bar", "series" + i, "data" + j);
            }
        }
    }

    /**
     * There is no easy way to add lines in barchart. By overriding {@link StackedBarChart#layoutPlotChildren()} (in super) it is possible
     * to add lines to list of plot children.
     */
    @Override
    protected void layoutPlotChildren() {
        super.layoutPlotChildren();
        if (getData().size() != 0) {
            Series<String, Double> series = getData().get(0);
            ObservableList<Data<String, Double>> listOfData = series.getData();
            double max = -60d;
            int xIndex = -1;
            for (int i = 0; i < listOfData.size(); i++) {
                Data<String, Double> data =  listOfData.get(i);
                if (data.getYValue() > max) {
                    max = data.getYValue();
                    xIndex = i;
                }
            }

            if (xIndex > -1) {
                double x0Prev = getXAxis().getDisplayPosition(listOfData.get(xIndex).getXValue());
                double x0 = getXAxis().getDisplayPosition(listOfData.get(xIndex + 1).getXValue());
                double x1 = getXAxis().getDisplayPosition(listOfData.get(xIndex + numberOfNearTaps).getXValue());
                double x2 = getXAxis().getDisplayPosition(listOfData.get(xIndex + 16).getXValue());
                double y0 = getYAxis().getDisplayPosition(nearMicroreflectionBoundary);
                double y1 = getYAxis().getDisplayPosition(farMicroreflectionBoundary);
                double offset = (x0 - x0Prev) / 2;

                Line[] lines = new Line[]{
                        new Line(x0 - offset, y0, x1 + offset, y0),
                        new Line(x1 + offset, y0, x1 + offset, y1),
                        new Line(x1 + offset, y1, x2 + offset, y1)
                };

                ListIterator<Line> iterator = addedLines.listIterator();
                while(iterator.hasNext()){
                    Line line = iterator.next();
                    getPlotChildren().remove(line);
                    iterator.remove();
                }

                for (Line line : lines) {
                    addedLines.add(line);
                    getPlotChildren().add(line);
                    line.toFront();
                    line.setStroke(Color.RED);
                    line.setStrokeWidth(2d);
                }
            }
        }
    }
}

