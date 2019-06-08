// code by ob
package ch.ethz.idsc.sophus.app.ob;

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.util.EnumMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.jfree.chart.ChartUtils;
import org.jfree.chart.JFreeChart;

import ch.ethz.idsc.sophus.app.api.GeodesicDisplay;
import ch.ethz.idsc.sophus.app.api.Se2GeodesicDisplay;
import ch.ethz.idsc.sophus.filter.BiinvariantMeanCenter;
import ch.ethz.idsc.sophus.filter.GeodesicCenter;
import ch.ethz.idsc.sophus.filter.GeodesicCenterFilter;
import ch.ethz.idsc.sophus.filter.GeodesicCenterMidSeeded;
import ch.ethz.idsc.sophus.filter.GeodesicCenterTangentSpace;
import ch.ethz.idsc.sophus.math.SmoothingKernel;
import ch.ethz.idsc.sophus.math.SpectrogramAnalogous;
import ch.ethz.idsc.sophus.math.SpectrogramArray;
import ch.ethz.idsc.sophus.math.TransferFunctionResponse;
import ch.ethz.idsc.subare.util.plot.ListPlot;
import ch.ethz.idsc.subare.util.plot.VisualRow;
import ch.ethz.idsc.subare.util.plot.VisualSet;
import ch.ethz.idsc.tensor.RationalScalar;
import ch.ethz.idsc.tensor.Scalar;
import ch.ethz.idsc.tensor.Tensor;
import ch.ethz.idsc.tensor.Tensors;
import ch.ethz.idsc.tensor.io.HomeDirectory;
import ch.ethz.idsc.tensor.io.ResourceData;
import ch.ethz.idsc.tensor.opt.TensorUnaryOperator;
import ch.ethz.idsc.tensor.qty.Quantity;
import ch.ethz.idsc.tensor.red.Mean;

/* package */ class FourierWindowPlot {
  private static final Scalar WINDOW_DURATION = Quantity.of(2, "s");
  private static final Scalar SAMPLING_FREQUENCY = Quantity.of(20, "s^-1");
  private static final TensorUnaryOperator FOURIER_WINDOW = SpectrogramArray.of(WINDOW_DURATION, SAMPLING_FREQUENCY, 1);

  private static enum Filter {
    GEODESIC, //
    GEODESIC_MID_SEEDED, //
    TANGENT_SPACE, //
    BIINVARIANT_MEAN;
  }

  // TODO OB: make logPlot (standard)
  private static void plot(Tensor data) throws IOException {
    Tensor yData = Tensors.empty();
    for (Tensor meanData : data)
      yData.append(TransferFunctionResponse.MAGNITUDE.apply(meanData));
    // ---
    Tensor xAxis = Tensors.empty();
    for (int index = -yData.get(0).length() / 2; index < yData.get(0).length() / 2; ++index)
      xAxis.append(RationalScalar.of(index, yData.get(0).length()).multiply(SAMPLING_FREQUENCY));
    VisualSet visualSet = new VisualSet();
    visualSet.setPlotLabel("Filter Gain");
    visualSet.setAxesLabelX("Frequency [Hz]");
    visualSet.setAxesLabelY("Magnitude");
    int index = 0;
    for (Tensor yAxis : yData) {
      VisualRow visualRow = visualSet.add( //
          xAxis, //
          Tensor.of(yAxis.append(yAxis).flatten(1)).extract(xAxis.length() / 2, xAxis.length() * 3 / 2));
      visualRow.setLabel(Filter.values()[index].toString());
      ++index;
    }
    JFreeChart jFreeChart = ListPlot.of(visualSet);
    jFreeChart.setBackgroundPaint(Color.WHITE);
    // Exportable as SVG?
    File file = HomeDirectory.Pictures("FilterGain.png");
    // impove DPI?
    ChartUtils.saveChartAsPNG(file, jFreeChart, 1024, 768);
  }

  private static void process(List<String> listData, Map<Filter, TensorUnaryOperator> map, int radius, int signal) throws IOException {
    Tensor smoothed = Tensors.empty();
    Iterator<String> iterator = listData.iterator();
    int limit = 2;
    for (int index = 0; index < limit; ++index) {
      Tensor control = Tensor.of(ResourceData.of("/dubilab/app/pose/" + iterator.next() + ".csv").stream().map(row -> row.extract(1, 4)));
      Tensor temp = Tensors.empty();
      for (TensorUnaryOperator tensorUnaryOperator : map.values())
        temp.append(SpectrogramAnalogous.of(control, GeodesicCenterFilter.of(tensorUnaryOperator, radius), signal, FOURIER_WINDOW));
      smoothed.append(temp);
    }
    plot(Mean.of(smoothed));
  }

  public static void main(String[] args) throws IOException {
    GeodesicDisplay geodesicDisplay = Se2GeodesicDisplay.INSTANCE;
    SmoothingKernel smoothingKernel = SmoothingKernel.GAUSSIAN;
    Map<Filter, TensorUnaryOperator> map = new EnumMap<>(Filter.class);
    map.put(Filter.GEODESIC, GeodesicCenter.of(geodesicDisplay.geodesicInterface(), smoothingKernel));
    map.put(Filter.GEODESIC_MID_SEEDED, GeodesicCenterMidSeeded.of(geodesicDisplay.geodesicInterface(), smoothingKernel));
    map.put(Filter.TANGENT_SPACE, GeodesicCenterTangentSpace.of(geodesicDisplay.lieGroup(), geodesicDisplay.lieExponential(), smoothingKernel));
    map.put(Filter.BIINVARIANT_MEAN, BiinvariantMeanCenter.of(geodesicDisplay.biinvariantMeanInterface(), smoothingKernel));
    List<String> listData = ResourceData.lines("/dubilab/app/pose/index.vector");
    int radius = 7;
    // TensorUnaryOperator tensorUnaryOperator = GeodesicCenter.of(Se2Geodesic.INSTANCE, SmoothingKernel.GAUSSIAN);
    // // FourierWindowPlot fwp =
    // new FourierWindowPlot();
    // List<String> list = ResourceData.lines("/dubilab/app/pose/index.vector");
    // signal cases: 0:x , 1:y, 2;heading
    process(listData, map, radius, 1);
  }
}