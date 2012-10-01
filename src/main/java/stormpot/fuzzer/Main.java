package stormpot.fuzzer;

import java.util.concurrent.TimeUnit;

import stormpot.Config;

public class Main {
  private final static long hours = Integer.getInteger("hours", 0);
  private final static long minutes = Integer.getInteger("minutes", 0);
  private final static long seconds = Integer.getInteger("seconds", 0);
  
  public static void main(String[] args) {
    long timeMillis = calculateTimeMills();
    
    Config<GenericPoolable> config = new Config<GenericPoolable>();
    FuzzyAllocator allocator = new FuzzyAllocator();
    config.setAllocator(allocator);
    
    
    
    System.out.println("poke!!! " + timeMillis + " ms.");
  }

  private static long calculateTimeMills() {
    return
        TimeUnit.HOURS.toMillis(hours) +
        TimeUnit.MINUTES.toMillis(minutes) +
        TimeUnit.SECONDS.toMillis(seconds);
  }
  
  /*
   * Interesting scenarios:
   *  - more threads than pool objects
   *  - fewer threads than pool objects
   *  - high allocation failure rate
   *  - low allocation failure rate
   *  - deallocation failures
   *  - threads claiming just one object
   *  - threads claiming more than one object
   *  - resizing the pool larger
   *  - resizing the pool smaller
   *  - long timeouts
   *  - short timeouts
   *  - zero timeouts
   *  - objects expire fast
   *  - objects expire slowly
   *  - expiration throws exception
   *  - expiration does not throw exception
   *  - elevated unparks
   *  - thread count < core count
   *  - thread count = core count
   *  - thread count > core count
   */
}
