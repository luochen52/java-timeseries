/*
 * Copyright (c) 2016 Jacob Rachiele
 */

package timeseries.models;

import java.awt.Color;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JPanel;

import org.knowm.xchart.XChartPanel;
import org.knowm.xchart.XYChart;
import org.knowm.xchart.XYChartBuilder;
import org.knowm.xchart.XYSeries;
import org.knowm.xchart.XYSeries.XYSeriesRenderStyle;
import org.knowm.xchart.style.Styler.ChartTheme;
import org.knowm.xchart.style.XYStyler;
import org.knowm.xchart.style.markers.Circle;
import org.knowm.xchart.style.markers.None;

import stats.distributions.Distribution;
import stats.distributions.Normal;
import timeseries.TimeScale;
import timeseries.TimeSeries;

public final class RandomWalk implements Model {

  private final TimeSeries timeSeries;
  private final TimeSeries fittedSeries;
  private final TimeSeries residuals;

  public RandomWalk(final TimeSeries observed) {
    this.timeSeries = observed.copy();
    this.fittedSeries = fitSeries();
    this.residuals = calculateResiduals();
  }

  /**
   * Simulate a random walk assuming that the errors follow the given Distribution.
   * 
   * @param dist The probability distribution that observations are drawn from.
   * @param n The number of observations to simulate.
   * @return A simulated RandomWalk model.
   */
  public static final Model simulate(final Distribution dist, final int n) {
    final double[] series = new double[n];
    for (int t = 0; t < n; t++) {
      series[t] = dist.rand();
    }
    return new RandomWalk(new TimeSeries(series));
  }

  /**
   * Simulate a random walk assuming errors follow a Normal (Gaussian) Distribution with the given mean and standard
   * deviation.
   * 
   * @param mean The mean of the Normal distribution the observations are drawn from.
   * @param sigma The standard deviation of the Normal distribution the observations are drawn from.
   * @param n The number of observations to simulate.
   * @return
   */
  public static final Model simulate(final double mean, final double sigma, final int n) {
    final Distribution dist = new Normal(mean, sigma);
    return simulate(dist, n);
  }

  public static final Model simulate(final double sigma, final int n) {
    final Distribution dist = new Normal(0, sigma);
    return simulate(dist, n);
  }

  public static final Model simulate(final int n) {
    final Distribution dist = new Normal(0, 1);
    return simulate(dist, n);
  }

  /* (non-Javadoc)
   * @see timeseries.models.Model#pointForecast(int)
   */
  @Override
  public final TimeSeries pointForecast(final int steps) {
    int n = timeSeries.n();
    long periodLength = timeSeries.periodLength();
    TimeScale timeScale = timeSeries.timeScale();
    
    double[] forecast = new double[steps];
    for (int t = 0; t < steps; t++) {
      forecast[t] = timeSeries.at(n - 1);
    }
    final OffsetDateTime startTime = timeSeries.observationTimes().get(n - 1).plus(
        periodLength * timeScale.periodLength(), timeScale.timeUnit());
    return new TimeSeries(timeScale, startTime, periodLength, forecast);
  }
  
  /* (non-Javadoc)
   * @see timeseries.models.Model#newForecast(int, double)
   */
  @Override
  public final Forecast newForecast(final int steps, final double alpha) {
    return new RandomWalkForecast(this, steps, alpha);
  }

  /* (non-Javadoc)
   * @see timeseries.models.Model#timeSeries()
   */
  @Override
  public final TimeSeries timeSeries() {
    return this.timeSeries.copy();
  }

  /* (non-Javadoc)
   * @see timeseries.models.Model#fittedSeries()
   */
  @Override
  public final TimeSeries fittedSeries() {
    return this.fittedSeries.copy();
  }

  /* (non-Javadoc)
   * @see timeseries.models.Model#residuals()
   */
  @Override
  public final TimeSeries residuals() {
    return this.residuals.copy();
  }

  /* (non-Javadoc)
   * @see timeseries.models.Model#plotFit()
   */
  @Override
  public final void plotFit() {
    
    new Thread(() -> {
      final List<Date> xAxis = new ArrayList<>(fittedSeries.observationTimes().size());
      for (OffsetDateTime dateTime : fittedSeries.observationTimes()) {
        xAxis.add(Date.from(dateTime.toInstant()));
      }
      List<Double> seriesList = com.google.common.primitives.Doubles.asList(timeSeries.series());
      List<Double> fittedList = com.google.common.primitives.Doubles.asList(fittedSeries.series());
      final XYChart chart = new XYChartBuilder().theme(ChartTheme.GGPlot2).height(600).width(800)
          .title("Random Walk Fitted vs Actual").build();
      XYSeries fitSeries = chart.addSeries("Fitted Values", xAxis, fittedList);
      XYSeries observedSeries = chart.addSeries("Actual Values", xAxis, seriesList);
      XYStyler styler = chart.getStyler();
      styler.setDefaultSeriesRenderStyle(XYSeriesRenderStyle.Line);
      observedSeries.setLineWidth(0.75f);
      observedSeries.setMarker(new None()).setLineColor(Color.RED);
      fitSeries.setLineWidth(0.75f);
      fitSeries.setMarker(new None()).setLineColor(Color.BLUE);
      
      JPanel panel = new XChartPanel<>(chart);
      JFrame frame = new JFrame("Random Walk Fit");
      frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
      frame.add(panel);
      frame.pack();
      frame.setVisible(true);
    }).start();

  }

  /* (non-Javadoc)
   * @see timeseries.models.Model#plotResiduals()
   */
  @Override
  public final void plotResiduals() {
    
    new Thread(() -> {
      final List<Date> xAxis = new ArrayList<>(fittedSeries.observationTimes().size());
      for (OffsetDateTime dateTime : fittedSeries.observationTimes()) {
        xAxis.add(Date.from(dateTime.toInstant()));
      }
      List<Double> seriesList = com.google.common.primitives.Doubles.asList(residuals.series());
      final XYChart chart = new XYChartBuilder().theme(ChartTheme.XChart).height(600).width(800)
          .title("Random Walk Residuals").build();
      XYSeries residualSeries = chart.addSeries("Model Residuals", xAxis, seriesList);
      residualSeries.setXYSeriesRenderStyle(XYSeriesRenderStyle.Scatter);
      residualSeries.setMarker(new Circle()).setMarkerColor(Color.RED);

      JPanel panel = new XChartPanel<>(chart);
      JFrame frame = new JFrame("Random Walk Residuals");
      frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
      frame.add(panel);
      frame.pack();
      frame.setVisible(true);
    }).start();
    
  }

  private final TimeSeries fitSeries() {
    final double[] fitted = new double[timeSeries.n()];
    fitted[0] = timeSeries.at(0);
    for (int t = 1; t < timeSeries.n(); t++) {
      fitted[t] = timeSeries.at(t - 1);
    }
    return new TimeSeries(timeSeries.timeScale(), timeSeries.observationTimes().get(0), timeSeries.periodLength(),
        fitted);
  }

  private final TimeSeries calculateResiduals() {
    final double[] residuals = new double[timeSeries.n()];
    for (int t = 1; t < timeSeries.n(); t++) {
      residuals[t] = timeSeries.at(t) - fittedSeries.at(t);
    }
    return new TimeSeries(timeSeries.timeScale(), timeSeries.observationTimes().get(0), timeSeries.periodLength(),
        residuals);
  }

  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder();
    builder.append("timeSeries: ").append(timeSeries).append("\nfittedSeries: ").append(fittedSeries)
        .append("\nresiduals: ").append(residuals);
    return builder.toString();
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((fittedSeries == null) ? 0 : fittedSeries.hashCode());
    result = prime * result + ((residuals == null) ? 0 : residuals.hashCode());
    result = prime * result + ((timeSeries == null) ? 0 : timeSeries.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    RandomWalk other = (RandomWalk) obj;
    if (fittedSeries == null) {
      if (other.fittedSeries != null) {
        return false;
      }
    } else if (!fittedSeries.equals(other.fittedSeries)) {
      return false;
    }
    if (residuals == null) {
      if (other.residuals != null) {
        return false;
      }
    } else if (!residuals.equals(other.residuals)) {
      return false;
    }
    if (timeSeries == null) {
      if (other.timeSeries != null) {
        return false;
      }
    } else if (!timeSeries.equals(other.timeSeries)) {
      return false;
    }
    return true;
  }
}