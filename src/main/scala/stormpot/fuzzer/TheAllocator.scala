package stormpot.fuzzer

import stormpot._

class TheAllocator extends Allocator[ThePoolable] {
  def allocate(slot : Slot): ThePoolable = new ThePoolable(slot)
  def deallocate(obj : ThePoolable): Unit = ()
}
