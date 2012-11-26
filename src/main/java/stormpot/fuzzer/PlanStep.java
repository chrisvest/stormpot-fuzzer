package stormpot.fuzzer;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import stormpot.fuzzer.situation.Situation;

public class PlanStep {
  private final List<Situation> situations;
  private final long durationMillis;
  
  public PlanStep(long durationMillis) {
    this.durationMillis = durationMillis;
    situations = new ArrayList<>();
  }

  public void describe(StringBuilder sb) {
    sb.append(" -- ");
    sb.append(durationMillis);
    sb.append(" ms.: ");
    Iterator<Situation> itr = situations.iterator();
    if (itr.hasNext()) {
      sb.append(itr.next());
    }
    while (itr.hasNext()) {
      sb.append(", ");
      sb.append(itr.next());
    }
    sb.append('\n');
  }

  public void add(Situation situation) {
    situations.add(situation);
  }
}
