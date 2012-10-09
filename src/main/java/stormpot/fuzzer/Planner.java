package stormpot.fuzzer;

public class Planner {

  private FuzzyAllocator allocator;
  
  /*
   * Interesting scenarios:
   *  - thread count > core count
   *  - thread count = core count
   *  - thread count < core count
   *  
   *  - more threads than pool objects
   *  - fewer threads than pool objects
   *  
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
   *  
   *  - elevated unparks
   */

  public Planner(FuzzyAllocator allocator) {
    this.allocator = allocator;
    
    
  }

  public Plan createPlan(long timeMillis) {
    // TODO Auto-generated method stub
    return null;
  }

}
