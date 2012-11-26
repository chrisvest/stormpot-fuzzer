package stormpot.fuzzer

import stormpot._

class ThePoolable(slot : Slot) extends Poolable {
  def release(): Unit = {
    slot.release(this)
  }
}
