package stormpot.fuzzer

import stormpot._
import java.util.Random
import java.util.concurrent.atomic.AtomicLong

object TheAllocator {
  val rnd: Random = new Random()
  val expectableException = new IllegalStateException("Allocation failure!")
}

class TheAllocator extends Allocator[Poolable] {
  val allocCount = new AtomicLong()
  val deallocCount = new AtomicLong()
  
  @volatile var allocationFailureRate: Double = 0.0
  @volatile var deallocationFailureRate: Double = 0.0
  
  def reset() {
    allocationFailureRate = 0.0
    deallocationFailureRate = 0.0
    
    allocCount.set(0)
    deallocCount.set(0)
  }
  
  def setAllocationFailureRate(rate: Double) {
    allocationFailureRate = rate
  }
  
  def setDeallocationFailureRate(rate: Double) {
    deallocationFailureRate = rate
  }
  
  def allocate(slot : Slot) = {
    if (slot == null) {
      error("Error: Attempt to allocate to null slot!")
    }
    if (TheAllocator.rnd.nextGaussian() < allocationFailureRate) {
      throw TheAllocator.expectableException
    }
    allocCount.incrementAndGet()
    new Poolable() {
      def release() = slot.release(this)
    }
  }
  
  def deallocate(obj : Poolable) {
    if (obj == null) {
      error("Error: Attempt to deallocate null!")
    }
    deallocCount.incrementAndGet()
    if (TheAllocator.rnd.nextGaussian() < deallocationFailureRate) {
      throw TheAllocator.expectableException
    }
  }
  
  def validateAllocations() {
    val allocations = allocCount.get()
    val deallocations = deallocCount.get()
    if (allocations != deallocations) {
      val msg = "Did " + allocations + " allocations, but " +
                deallocations + " deallocations"
      throw new IllegalStateException(msg)
    }
  }
}
