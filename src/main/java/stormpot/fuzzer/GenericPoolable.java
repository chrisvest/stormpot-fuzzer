package stormpot.fuzzer;

import stormpot.Poolable;
import stormpot.Slot;

public class GenericPoolable implements Poolable {
  private final Slot slot;
  
  public GenericPoolable(Slot slot) {
    this.slot = slot;
  }

  @Override
  public void release() {
    slot.release(this);
  }
}
