package hr.avrbanac.docsis.app;

import javafx.beans.NamedArg;
import javafx.collections.ObservableList;
import javafx.scene.chart.Axis;
import javafx.scene.chart.LineChart;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

/**
 * Extended version of the {@link LineChart} since there is a need for partial background color fill and line chart does not support that
 * feature.
 * @param <X>
 * @param <Y>
 */
public class ICFRLineChart <X, Y> extends LineChart<X, Y> {

    /**
     * The only needed CTOR for the current implementation.
     * @param xAxis {@link Axis} X axis
     * @param yAxis {@link Axis} Y axis
     */
    public ICFRLineChart(
            @NamedArg("xAxis") final Axis<X> xAxis,
            @NamedArg("yAxis") final Axis<Y> yAxis) {

        super(xAxis, yAxis);
    }

    /**
     * Overloaded method will plot original data but also background polygons. This class should be used for specific purpose (plotting out
     * ICFR graph), thus making suppressed warnings for unchecked cast ok.
     */
    @SuppressWarnings("unchecked")
    @Override
    protected void layoutPlotChildren() {
        super.layoutPlotChildren();
        Axis<String> xAxis = ((Axis<String>) getXAxis());
        Axis<Double> yAxis = ((Axis<Double>) getYAxis());
        if (getData().size() != 0) {
            Series<String, Double> series = (Series<String, Double>) getData().get(0);
            ObservableList<Data<String, Double>> listOfData = series.getData();
            double x0 = xAxis.getDisplayPosition(listOfData.get(0).getXValue());
            double width = xAxis.getDisplayPosition(listOfData.get(31).getXValue()) - x0;
            double y4p = yAxis.getDisplayPosition(4d);
            double y2p = yAxis.getDisplayPosition(2d);
            double y1p = yAxis.getDisplayPosition(1d);
            double y1n = yAxis.getDisplayPosition(-1d);
            double y2n = yAxis.getDisplayPosition(-2d);
            double y4n = yAxis.getDisplayPosition(-4d);

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

            for (Rectangle rectangle : rectangles) {
                rectangle.setOpacity(0.1d);
                rectangle.toBack();
                getPlotChildren().add(rectangle);
            }
        }
    }
}
