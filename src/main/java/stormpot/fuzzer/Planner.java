package stormpot.fuzzer;

import static stormpot.fuzzer.situation.Category.*;
import static stormpot.fuzzer.situation.Mode.*;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import stormpot.fuzzer.situation.*;

public class Planner {
  private static final long minSlotTimeMillis = 500;

  private final FuzzyAllocator allocator;
  private final List<Situation> situations;

  private int coreCount;

  private int maxThreadCount;
  
  /*
   * Interesting scenarios:
   *  - thread count > core count
   *  - thread count = core count
   *  - thread count < core count
   *  
   *  - more threads than pool objects
   *  - fewer threads than pool objects
   *  - resizing the pool larger
   *  - resizing the pool smaller
   *  
   *  - elevated unparks
   *  - threads claiming just one object
   *  - threads claiming more than one object
   *  
   *  - high allocation failure rate
   *  - low allocation failure rate
   *  - deallocation failures
   *  - long timeouts
   *  - short timeouts
   *  - zero timeouts
   *  - objects expire fast
   *  - objects expire slowly
   *  - expiration throws exception
   *  - expiration does not throw exception
   */

  public Planner(FuzzyAllocator allocator) {
    this.allocator = allocator;
    this.situations = new  ArrayList<>();
    
    coreCount = Runtime.getRuntime().availableProcessors();
    maxThreadCount = coreCount * 4 + 1;
    
    add(99, new SetThreadCount(maxThreadCount), threadCount, passive);
    add(50, new SetThreadCount(coreCount), threadCount, passive);
    add(50, new SetThreadCount(Math.max(coreCount / 2, 1)), threadCount, passive);

    add(50, new SetPoolSizeRelativeToThreads(-2), poolSize, passive);
    add(40, new SetPoolSizeRelativeToThreads(10), poolSize, passive);
    add(60, new ContinuouslyStepResizingPool(1), poolSize, background);
    add(40, new ContinuouslyStepResizingPool(-1), poolSize, background);
    
    
  }
  
  private void add(int weight, Situation situation, Category category, Mode mode) {
    situation.init(weight, category, mode);
    situations.add(situation);
  }

  public Plan createPlan(long timeMillis) {
    long minSlotsPossible = timeMillis / minSlotTimeMillis;
    Plan plan = new Plan();
    System.out.println("min-slots possible: " + minSlotsPossible);
    
    /*
     * all possible sets
     * remove ones with conflicting exclusives
     * remove ones with more active modes than threads
     */
    
    for (int threads = maxThreadCount; threads > 0; threads--) {
      generatePlanPermutations(plan, threads);
    }
    
//    EnumSet<Category> visited = EnumSet.noneOf(Category.class);
//    for (Category a : Category.values()) {
//      visited.add(a);
//      for (Category b : Category.values()) {
//        if (visited.contains(b)) {
//          continue;
//        }
//        List<Situation> as = filterSituation(a);
//        List<Situation> bs = filterSituation(b);
//        for (Situation ax : as) {
//          PlanStep step = new PlanStep(minSlotsPossible);
//          step.add(ax);
//          for (Situation bx : bs) {
//            step.add(bx);
//          }
//          plan.add(step);
//        }
//      }
//    }
    
    return plan;
  }

  private void generatePlanPermutations(Plan plan, int threads) {
    // TODO Auto-generated method stub
    
  }

  private List<Situation> filterSituation(Category category) {
    List<Situation> list = new ArrayList<>();
    for (Situation situation : situations) {
      if (!situation.hasCategory(category)) {
        list.add(situation);
      }
    }
    return list;
  }
}
