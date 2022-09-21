package hr.avrbanac.docsis.app;

import javafx.beans.NamedArg;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.chart.Axis;
import javafx.scene.chart.StackedBarChart;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;

import java.util.List;

/**
 * Original {@link StackedBarChart} class is currently broken. It works fine with positive values, but negative values tend to be
 * overwritten. This happens because negative values are defined using CSS class "negative", which at some point gets overwritten by using
 * style .setAll() method instead of .addAll() method. Fixed version of the class takes care of this problem by overwriting broken methods.
 * Also, additional lines were added to barchart, since currently there is no FX chart that can plot bar data together with line data.
 * @param <X>
 * @param <Y>
 */
public class TapBarChart<X, Y> extends StackedBarChart<X, Y> {

    private double nearMicroreflectionBoundary = -25d;
    private double farMicroreflectionBoundary = -35d;
    private int numberOfNearTaps = 2;

    /**
     * The only needed CTOR for this fixed version of the barchart.
     * @param xAxis {@link Axis} X forwarded to super
     * @param yAxis {@link Axis} Y forwarded to super
     */
    public TapBarChart(
            @NamedArg("xAxis") final Axis<X> xAxis,
            @NamedArg("yAxis") final Axis<Y> yAxis) {

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
     * Override the method that breaks the graph, patched to add missing "negative" CSS class.
     * @param series {@link Series} provided series to add (forwarded to super)
     * @param itemIndex int index of the item (forwarded to super)
     * @param item {@link Data} (forwarded to super), also used to determine whether "negative" style class needs to be added
     */
    @Override
    protected void dataItemAdded(
            final Series<X, Y> series,
            final int itemIndex,
            final Data<X, Y> item) {

        super.dataItemAdded(series, itemIndex, item);

        Number val = (Number) (item.getYValue() instanceof Number
                ? item.getYValue()
                : item.getXValue());

        if (val.doubleValue() < 0) {
            // add missing CSS class
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
    protected void seriesChanged(
            final ListChangeListener.Change<? extends Series> c) {

        for (int i = 0; i < getData().size(); i++) {
            List<Data<X, Y>> items = getData().get(i).getData();
            for (int j = 0; j < items.size(); j++) {
                Node bar = items.get(j).getNode();
                // change .setAll to .addAll to avoid overriding styles
                bar.getStyleClass().removeIf(s -> s.matches("chart-bar|(series|data)\\d+"));
                bar.getStyleClass().addAll("chart-bar", "series" + i, "data" + j);
            }
        }
    }

    /**
     * There is no easy way to add lines in barchart. By overriding {@link super#layoutPlotChildren()} it is possible to add lines to list
     * of plot children. This class should be used for specific purpose (plotting out tap graph), thus making suppressed warnings for
     * unchecked cast ok.
     */
    @SuppressWarnings("unchecked")
    @Override
    protected void layoutPlotChildren() {
        super.layoutPlotChildren();
        if (getData().size() != 0) {
            Series<String, Double> series = (Series<String, Double>) getData().get(0);
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
                Axis<String> xAxis = ((Axis<String>) getXAxis());
                Axis<Double> yAxis = ((Axis<Double>) getYAxis());
                double x0Prev = xAxis.getDisplayPosition(listOfData.get(xIndex).getXValue());
                double x0 = xAxis.getDisplayPosition(listOfData.get(xIndex + 1).getXValue());
                double x1 = xAxis.getDisplayPosition(listOfData.get(xIndex + numberOfNearTaps).getXValue());
                double x2 = xAxis.getDisplayPosition(listOfData.get(xIndex + 16).getXValue());
                double y0 = yAxis.getDisplayPosition(nearMicroreflectionBoundary);
                double y1 = yAxis.getDisplayPosition(farMicroreflectionBoundary);
                double offset = (x0 - x0Prev) / 2;

                Line[] lines = new Line[]{
                        new Line(x0 - offset, y0, x1 + offset, y0),
                        new Line(x1 + offset, y0, x1 + offset, y1),
                        new Line(x1 + offset, y1, x2 + offset, y1)
                };
                for (Line line : lines) {
                    getPlotChildren().add(line);
                    line.toFront();
                    line.setStroke(Color.RED);
                    line.setStrokeWidth(2d);
                }
            }
        }
    }
}

