package game;

import java.awt.Color;
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.HashMap;

import javax.swing.JFrame;
import javax.swing.JPanel;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.LookupPaintScale;
import org.jfree.chart.renderer.xy.VectorRenderer;
import org.jfree.chart.renderer.xy.XYBlockRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.DefaultXYZDataset;
import org.jfree.data.xy.VectorSeries;
import org.jfree.data.xy.VectorSeriesCollection;
import org.jfree.data.xy.VectorXYDataset;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.data.xy.XYZDataset;
import org.jfree.ui.ApplicationFrame;
import org.jfree.ui.RectangleInsets;

public class Weatherplots extends ApplicationFrame {

	
	public Weatherplots(String title, double[][] data){
		super(title);
		DefaultXYZDataset dataset = new DefaultXYZDataset();
		double[] xvalues = new double[data.length*data[0].length];
		double[] yvalues = new double[data.length*data[0].length];
		double[] zvalues = new double[data.length*data[0].length];
		int i = 0;
		for(int j=0;j<xvalues.length;j++){
			if(j%data[0].length == 0 && j!= 0){
				i++;
			}
			xvalues[j] = i;
		}
		for(int j=0;j<yvalues.length;j++){
			yvalues[j] = j%data[0].length;
		}
		for(int j=0;j<zvalues.length;j++){
			zvalues[j] = data[(int)xvalues[j]][(int)yvalues[j]];
		}
		
		dataset.addSeries("Temperature 1", new double[][]{xvalues,yvalues,zvalues});
		JPanel chartPanel = createContourPanel(dataset, title);
		chartPanel.setPreferredSize(new Dimension(500,500));
		setContentPane(chartPanel);
		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
	}
	public Weatherplots(String title, HashMap<int[],double[]> data){
		super(title);
		
        VectorSeries vectorseries = new VectorSeries("Series 1");   
        for(int[] key: data.keySet()){
        	vectorseries.add(key[0],key[1],data.get(key)[0],data.get(key)[1]);
        }
   
        VectorSeriesCollection dataset = new VectorSeriesCollection();   
        dataset.addSeries(vectorseries);
        
        JPanel chartPanel = createVectorPanel(dataset);
        chartPanel.setPreferredSize(new Dimension(500,500));
        setContentPane(chartPanel);
	}
	public Weatherplots(String title, ArrayList<double[]> data){
		super(title);
		XYSeriesCollection dataset = new XYSeriesCollection();
		XYSeries d = new XYSeries("Cloud data");
		for(double[] j: data){
			d.add(j[0], j[1]);
		}
		dataset.addSeries(d);
		JPanel chartPanel = createScatterPanel(dataset);
		chartPanel.setPreferredSize(new Dimension(500,500));
		setContentPane(chartPanel);
	}
	
	public JPanel createVectorPanel(VectorXYDataset dataset){
		return new ChartPanel(createChart(dataset));
	}
	
	public JPanel createContourPanel(XYZDataset dataset, String title){
		return new ChartPanel(createChart(dataset, title));
	}
	
	public JPanel createScatterPanel(XYDataset dataset){
		return new ChartPanel(createChart(dataset));
	}
	
    private static JFreeChart createChart(VectorXYDataset vectorxydataset)   
    {   
        NumberAxis numberaxis = new NumberAxis("X");   
        numberaxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());   
        numberaxis.setLowerMargin(0.01D);   
        numberaxis.setUpperMargin(0.01D);   
        numberaxis.setAutoRangeIncludesZero(false);   
        NumberAxis numberaxis1 = new NumberAxis("Y");   
        numberaxis1.setStandardTickUnits(NumberAxis.createIntegerTickUnits());   
        numberaxis1.setLowerMargin(0.01D);   
        numberaxis1.setUpperMargin(0.01D);   
        numberaxis1.setAutoRangeIncludesZero(false);   
        VectorRenderer vectorrenderer = new VectorRenderer();   
        vectorrenderer.setSeriesPaint(0, Color.blue);   
        XYPlot xyplot = new XYPlot(vectorxydataset, numberaxis, numberaxis1, vectorrenderer);  
        xyplot.getRangeAxis().setInverted(true);
        xyplot.setBackgroundPaint(Color.lightGray);   
        xyplot.setDomainGridlinePaint(Color.white);   
        xyplot.setRangeGridlinePaint(Color.white);   
        xyplot.setAxisOffset(new RectangleInsets(5D, 5D, 5D, 5D));   
        xyplot.setOutlinePaint(Color.black);   
        JFreeChart jfreechart = new JFreeChart("Wind Speeds", xyplot);   
        jfreechart.setBackgroundPaint(Color.white);   
        return jfreechart;   
    }
    
    private static JFreeChart createChart(XYDataset dataset){
        JFreeChart chart = ChartFactory.createScatterPlot(
                "Cloud Positions",                  // chart title
                "X",                      // x axis label
                "Y",                      // y axis label
                dataset,                  // data
                PlotOrientation.VERTICAL,
                true,                     // include legend
                true,                     // tooltips
                false                     // urls
            );
        XYPlot plot = (XYPlot) chart.getPlot();
        plot.getRangeAxis().setInverted(true);
        XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer();
        renderer.setSeriesLinesVisible(0, false);
        plot.setRenderer(renderer);
        return chart;
    }
	
    private static JFreeChart createChart(XYZDataset dataset, String title) {
        NumberAxis xAxis = new NumberAxis("X");
        xAxis.setLowerMargin(0.0);
        xAxis.setUpperMargin(0.0);
        NumberAxis yAxis = new NumberAxis("Y");
        yAxis.setAutoRangeIncludesZero(true);
        yAxis.setInverted(true);
        yAxis.setLowerMargin(0.0);
        yAxis.setUpperMargin(0.0);
        yAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
        XYBlockRenderer renderer = new XYBlockRenderer();
        LookupPaintScale paintScale = new LookupPaintScale(0, 15, Color.black);
        for(int j=0;j<40;j++){
        	paintScale.add((double)j/2.0+10, new Color((int)(255.0/40.0)*j,(int)((255.0/40.0)*(-0.1*Math.pow(j-20, 2)+ 40.0)),(int)(255.0/40.0)*(40-j)));
        }
        renderer.setPaintScale(paintScale);
        XYPlot plot = new XYPlot(dataset, xAxis, yAxis, renderer);
        plot.setBackgroundPaint(Color.lightGray);
        plot.setDomainGridlinePaint(Color.white);
        plot.setRangeGridlinePaint(Color.white);
        plot.setForegroundAlpha(0.66f);
        plot.setAxisOffset(new RectangleInsets(5, 5, 5, 5));
        JFreeChart chart = new JFreeChart(title, plot);
        chart.removeLegend();
        chart.setBackgroundPaint(Color.white);
        /*SymbolAxis scaleAxis = new SymbolAxis(null, new String[] {"", "OK",
                "Uncertain", "Bad"});
        scaleAxis.setRange(0.5, 3.5);
        scaleAxis.setPlot(new PiePlot());
        scaleAxis.setGridBandsVisible(false);
        PaintScaleLegend psl = new PaintScaleLegend(paintScale, scaleAxis);
        psl.setAxisOffset(5.0);
        psl.setPosition(RectangleEdge.BOTTOM);
        psl.setMargin(new RectangleInsets(5, 5, 5, 5));
        chart.addSubtitle(psl);*/
        return chart;
    }
}

