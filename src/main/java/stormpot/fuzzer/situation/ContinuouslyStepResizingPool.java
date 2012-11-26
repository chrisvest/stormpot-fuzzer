package stormpot.fuzzer.situation;

public class ContinuouslyStepResizingPool extends Situation {

  private final int increment;

  public ContinuouslyStepResizingPool(int increment) {
    this.increment = increment;
  }

  @Override
  public String toString() {
    return "ContinuouslyStepResizingPool(" + increment + ")";
  }
}


