package stormpot.fuzzer

import stormpot._

class TheAllocator extends Allocator[Poolable] {
  def allocate(slot : Slot) = new Poolable {
    def release() = slot.release(this)
  }
  def deallocate(obj : Poolable) = ()
}
