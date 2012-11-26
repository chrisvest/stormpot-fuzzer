package stormpot.fuzzer;

import java.util.ArrayList;
import java.util.List;

public class Plan {
  private final List<PlanStep> steps;
  
  public Plan() {
    steps = new ArrayList<PlanStep>();
  }

  public void execute(PoolFactory poolFactory) {
    // TODO Auto-generated method stub
    
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("Plan:\n");
    for (PlanStep step : steps) {
      step.describe(sb);
    }
    return sb.toString();
  }

  public void add(PlanStep step) {
    steps.add(step);
  }
}
