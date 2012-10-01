package stormpot.fuzzer;

import stormpot.Allocator;
import stormpot.Slot;

public class FuzzyAllocator implements Allocator<GenericPoolable> {

  @Override
  public GenericPoolable allocate(Slot slot) throws Exception {
    return new GenericPoolable(slot);
  }

  @Override
  public void deallocate(GenericPoolable obj) throws Exception {
  }
}
