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
}