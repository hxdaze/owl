// code by jph
package ch.ethz.idsc.sophus.app.avg;

import ch.ethz.idsc.sophus.app.api.AbstractDemoHelper;
import junit.framework.TestCase;

public class BezierFunctionSplitsDemoTest extends TestCase {
  public void testSimple() {
    AbstractDemoHelper.offscreen(new BezierFunctionSplitsDemo());
  }
}
