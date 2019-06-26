// code by jph
package ch.ethz.idsc.sophus.crv;

import ch.ethz.idsc.tensor.RealScalar;
import ch.ethz.idsc.tensor.Tensor;
import ch.ethz.idsc.tensor.Tensors;
import ch.ethz.idsc.tensor.sca.Chop;
import junit.framework.TestCase;

public class CurveCurvatureTest extends TestCase {
  public void testString2() {
    Tensor points = Tensors.fromString("{{0,0},{1,1}}");
    Tensor vector = CurveCurvature.string(points);
    Chop._12.requireClose(vector, Tensors.vector(0, 0));
  }

  public void testString3() {
    Tensor points = Tensors.fromString("{{0,0},{1,1},{2,0}}");
    Tensor vector = CurveCurvature.string(points);
    Chop._12.requireClose(vector, Tensors.vector(-1, -1, -1));
  }

  public void testStringEmpty() {
    Tensor points = Tensors.empty();
    Tensor vector = CurveCurvature.string(points);
    assertEquals(points, vector);
  }

  public void testFailHi() {
    Tensor points = Tensors.fromString("{{0,0,0},{1,1,0},{2,0,0}}");
    try {
      CurveCurvature.string(points);
      fail();
    } catch (Exception exception) {
      // ---
    }
  }

  public void testFailStringScalar() {
    try {
      CurveCurvature.string(RealScalar.ZERO);
      fail();
    } catch (Exception exception) {
      // ---
    }
  }
}
