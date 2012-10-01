package stormpot.fuzzer;

import java.util.concurrent.atomic.AtomicLong;

import stormpot.Allocator;
import stormpot.Slot;

public class FuzzyAllocator implements Allocator<GenericPoolable> {
  private final AtomicLong counter = new AtomicLong();
  volatile long allocationFailureRate = 0;

  @Override
  public GenericPoolable allocate(Slot slot) throws Exception {
    if (slot == null) {
      throw new AssertionError("tried to allocate a null slot!");
    }
    long failureRate = allocationFailureRate;
    if (failureRate > 0 && counter.incrementAndGet() % failureRate == 0) {
      throw new RandomAllocationFailureException();
    }
    return new GenericPoolable(slot);
  }

  @Override
  public void deallocate(GenericPoolable obj) throws Exception {
    if (obj == null) {
      throw new AssertionError("attempted deallocation of null!");
    }
  }
}
