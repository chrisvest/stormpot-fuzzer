package stormpot.fuzzer

import stormpot._
import java.util.Random

object TheAllocator {
  val rnd: Random = new Random()
  val expectableException = new IllegalStateException("Allocation failure!")
}

class TheAllocator extends Allocator[Poolable] {
  @volatile var failureRate: Double = 0.0
  
  def setFailureRate(rate: Double) {
    failureRate = rate
  }
  
  def allocate(slot : Slot) = {
    if (TheAllocator.rnd.nextGaussian() < failureRate)
      throw TheAllocator.expectableException
    else new Poolable() {
      def release() = slot.release(this)
    }
  }
  
  def deallocate(obj : Poolable) = ()
}
