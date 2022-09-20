package hr.avrbanac.docsis.app;

import javafx.beans.NamedArg;
import javafx.collections.ListChangeListener;
import javafx.scene.Node;
import javafx.scene.chart.Axis;
import javafx.scene.chart.StackedBarChart;

import java.util.List;

/**
 * Original {@link StackedBarChart} class is currently broken. It works fine with positive values, but negative values tend to be
 * overwritten. This happens because negative values are defined using CSS class "negative", which at some point gets overwritten by using
 * style .setAll() method instead of .addAll() method. Fixed version of the class takes care of this problem by overwriting broken methods.
 * @param <X> x axis
 * @param <Y> y axis
 */
public class FixedStackedBarChart<X, Y> extends StackedBarChart<X, Y> {

    /**
     * The only needed CTOR for this fixed version of the barchart.
     * @param xAxis {@link Axis} X
     * @param yAxis {@link Axis} Y
     */
    public FixedStackedBarChart(
            @NamedArg("xAxis") final Axis<X> xAxis,
            @NamedArg("yAxis") final Axis<Y> yAxis) {

        super(xAxis, yAxis);
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
}

