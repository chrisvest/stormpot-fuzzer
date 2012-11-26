package stormpot.fuzzer;

import java.util.concurrent.TimeUnit;

import stormpot.Config;

public class Main {
  private final static long hours = Integer.getInteger("hours", 0);
  private final static long minutes = Integer.getInteger("minutes", 0);
  private final static long seconds = Integer.getInteger("seconds", 0);
  
  public static void main(String[] args) {
    long timeMillis = calculateTimeMills();
    PoolFactory poolFactory = createPoolFactory();
    
    Config<GenericPoolable> config = new Config<GenericPoolable>();
    FuzzyAllocator allocator = new FuzzyAllocator();
    config.setAllocator(allocator);
    
    Planner planner = new Planner(allocator);
    Plan plan = planner.createPlan(timeMillis);
    
    System.out.println("Fuzz-Test Execution Plan:");
    System.out.println(plan);
    
    plan.execute(poolFactory);
  }

  private static long calculateTimeMills() {
    return
        TimeUnit.HOURS.toMillis(hours) +
        TimeUnit.MINUTES.toMillis(minutes) +
        TimeUnit.SECONDS.toMillis(seconds);
  }

  private static PoolFactory createPoolFactory() {
    String pfprop = System.getProperty("pool", "blaze");
    if (pfprop.equals("blaze")) {
      return new BlazePoolFactory();
    }
    if (pfprop.equals("queue")) {
      return new QueuePoolFactory();
    }
    throw new IllegalArgumentException("I don't know this pool: " + pfprop);
  }
}
