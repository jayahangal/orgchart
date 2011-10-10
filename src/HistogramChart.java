import javax.swing.JPanel;
import javax.swing.JFrame;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.statistics.HistogramDataset;

public class HistogramChart extends JFrame {

    String title;
    HistogramDataset dataset;

    /**
     * Default constructor.
     *
     * @param title  the frame title.
     */
    public HistogramChart(String title, HistogramDataset dataset) {
        super(title);
	this.title = title;
	this.dataset = dataset;
        setContentPane(createDemoPanel());
    }

    /**
     * Creates a chart.
     *
     * @param dataset  the dataset.
     *
     * @return A chart.
     */
    private JFreeChart createChart(HistogramDataset dataset) {

        JFreeChart chart = ChartFactory.createHistogram(
            this.title,  // chart title
	    "Time in milli seconds",
	    "Tasks",
            this.dataset,             // data
	    PlotOrientation.VERTICAL,
            true,               // include legend
            true,
            false
        );

//        HistogramPlot plot = (HistogramPlot) chart.getPlot();
  //      plot.setSectionOutlinesVisible(false);
    //    plot.setNoDataMessage("No data available");

        return chart;

    }

    /**
     * Creates a panel 
     *
     * @return A panel.
     */
    public JPanel createDemoPanel() {
        JFreeChart chart = createChart(dataset);
        return new ChartPanel(chart);
    }

    public static void showChart() {
        HistogramChart demo = new HistogramChart("Histogram Chart", createDataset());
        demo.pack();
	demo.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        demo.setVisible(true);
    }

    /**
     * Creates a sample dataset.
     *
     * @return A sample dataset.
     */
    private static HistogramDataset createDataset() {
	double[] values = new double[] {43.2, 10.0, 27.5, 17.5, 11.0, 19.4};
        HistogramDataset dataset = new HistogramDataset();
	String key = "Methods";
        dataset.addSeries(key, values, values.length);
        return dataset;
    }

    /**
     * Starting point for the demonstration application.
     *
     * @param args  ignored.
     */
    public static void main(String[] args) {
	showChart();
    }
}

