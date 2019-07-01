// code by jph
package ch.ethz.idsc.owl.bot.se2.glc;

import ch.ethz.idsc.owl.bot.se2.Se2CoveringWrap;
import ch.ethz.idsc.owl.bot.se2.Se2Wrap;
import ch.ethz.idsc.owl.glc.adapter.GlcExpand;
import ch.ethz.idsc.owl.glc.core.CheckedGlcTrajectoryPlanner;
import ch.ethz.idsc.owl.glc.core.GlcTrajectoryPlanner;
import ch.ethz.idsc.owl.glc.core.HeuristicAssert;
import ch.ethz.idsc.owl.math.region.So2Region;
import ch.ethz.idsc.owl.math.state.StateTime;
import ch.ethz.idsc.tensor.RealScalar;
import ch.ethz.idsc.tensor.Tensors;
import junit.framework.TestCase;

public class Se2WrapDemoTest extends TestCase {
  public void testSe2Wrap() {
    GlcTrajectoryPlanner trajectoryPlanner = CheckedGlcTrajectoryPlanner.wrap( //
        Se2WrapDemo.createPlanner(Se2Wrap.INSTANCE, So2Region.periodic(RealScalar.ZERO, RealScalar.of(.3))));
    trajectoryPlanner.insertRoot(new StateTime(Tensors.vector(0.1, 0, 0), RealScalar.ZERO));
    GlcExpand glcExpand = new GlcExpand(trajectoryPlanner);
    glcExpand.findAny(20);
    assertEquals(glcExpand.getExpandCount(), 20);
    glcExpand.findAny(200);
    assertTrue(glcExpand.getExpandCount() < 100);
    assertTrue(trajectoryPlanner.getBest().isPresent());
    HeuristicAssert.check(trajectoryPlanner);
  }

  public void testSe2CoveringWrap() {
    GlcTrajectoryPlanner trajectoryPlanner = //
        CheckedGlcTrajectoryPlanner.wrap(Se2WrapDemo.createPlanner(Se2CoveringWrap.INSTANCE, So2Region.covering(RealScalar.ZERO, RealScalar.of(.3))));
    trajectoryPlanner.insertRoot(new StateTime(Tensors.vector(0.1, 0, 0), RealScalar.ZERO));
    GlcExpand glcExpand = new GlcExpand(trajectoryPlanner);
    glcExpand.findAny(20);
    assertEquals(glcExpand.getExpandCount(), 20);
    glcExpand.findAny(10_000);
    assertTrue(glcExpand.getExpandCount() < 2000);
    assertTrue(trajectoryPlanner.getBest().isPresent());
    HeuristicAssert.check(trajectoryPlanner);
  }
}
