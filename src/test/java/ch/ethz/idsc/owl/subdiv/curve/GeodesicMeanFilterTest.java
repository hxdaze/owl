// code by jph
package ch.ethz.idsc.owl.subdiv.curve;

import ch.ethz.idsc.tensor.ExactScalarQ;
import ch.ethz.idsc.tensor.RealScalar;
import ch.ethz.idsc.tensor.Tensor;
import ch.ethz.idsc.tensor.Tensors;
import ch.ethz.idsc.tensor.alg.UnitVector;
import ch.ethz.idsc.tensor.img.MeanFilter;
import ch.ethz.idsc.tensor.opt.TensorUnaryOperator;
import ch.ethz.idsc.tensor.red.Total;
import ch.ethz.idsc.tensor.sca.Unitize;
import junit.framework.TestCase;

public class GeodesicMeanFilterTest extends TestCase {
  public void testSimple() {
    for (int radius = 0; radius < 4; ++radius) {
      TensorUnaryOperator geodesicMeanFilter = GeodesicMeanFilter.of(RnGeodesic.INSTANCE, radius);
      Tensor tensor = Tensors.vector(1, 2, 3, 4, 6, 7);
      Tensor result = geodesicMeanFilter.apply(tensor);
      assertEquals(result.length(), tensor.length());
    }
  }

  public void testRadiusOne() {
    TensorUnaryOperator geodesicMeanFilter = GeodesicMeanFilter.of(RnGeodesic.INSTANCE, 1);
    Tensor tensor = UnitVector.of(10, 5);
    Tensor result = geodesicMeanFilter.apply(tensor);
    assertEquals(Total.of(result), RealScalar.ONE);
    Tensor expect = UnitVector.of(10, 4).add(UnitVector.of(10, 5)).add(UnitVector.of(10, 6));
    assertEquals(Unitize.of(result), expect);
  }

  public void testMultiRadius() {
    for (int radius = 0; radius < 5; ++radius) {
      TensorUnaryOperator geodesicMeanFilter = GeodesicMeanFilter.of(RnGeodesic.INSTANCE, radius);
      Tensor tensor = UnitVector.of(2 * radius + 1, radius);
      Tensor apply = geodesicMeanFilter.apply(tensor);
      Tensor compar = MeanFilter.of(tensor, radius);
      assertEquals(apply.Get(radius), compar.Get(radius));
      assertTrue(ExactScalarQ.all(apply));
    }
  }
}
