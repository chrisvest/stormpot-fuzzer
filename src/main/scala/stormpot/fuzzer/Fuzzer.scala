package stormpot.fuzzer

import stormpot._
import java.util.concurrent.{CountDownLatch, TimeUnit}
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.locks.LockSupport
import collection.Iterator
import scala.collection.mutable.ArrayStack

class Fuzzer(
    val config: Config[Poolable],
    var poolFactory: PoolFactory = null,
    var absoluteThreadCount: Int = -1,
    var relativeThreadFactor: Double = -1.0,
    var poolSize: Int = 10,
    var fuzzTime: Long = 500,
    var alloc: TheAllocator = new TheAllocator(),
    var workList: List[() => Unit] = List()) {
  
  val serviceThreads = new ArrayStack[Runnable]
  var pool: PoolType = null;
  @volatile var workers: Option[List[Thread]] = None
  
  def withService[X](
      interval: Long,
      init: X,
      step: (X => Unit),
      inc: (X => X)): Unit = {
    serviceThreads += new Runnable {
      def run() {
        var x = init
        while (sleep()) {
          step(x)
          x = inc(x)
        }
      }
      
      def sleep() = {
        try {
          Thread.sleep(interval)
          true
        } catch {
        case e => false
        }
      }
    }
  }
  
  def everyMs(interval: Long, act: Fuzzer => Unit) {
    withService(interval, this, act, identity: (Fuzzer => Fuzzer))
  }
  
  def countEveryMs(interval: Long, act: Int => Unit) {
    withService(interval, 1, act, (n: Int) => n+1)
  }
  
  def unparkThreads() {
    workers.foreach(_.foreach(LockSupport.unpark))
  }
  
  def doClaims(claims: Int, timeout: Timeout) {
    val deadline = timeout.getDeadline()
    val obj = pool.claim(timeout)
    val excessTime = timeout.getTimeLeft(deadline)
    if (obj == null && excessTime > 0) {
      val unit = timeout.getBaseUnit()
      val msg = "claimed 'null' with " + excessTime + " " + unit + " left."
      throw new IllegalStateException(msg)
    }
    try {
      if (claims > 0) {
        doClaims(claims - 1, timeout)
      }
    } finally {
      if (obj != null) {
        obj.release()
      }
    }
  }
  
  def claimRelease(count: Int, timeout: Timeout) {
    val f = () => {
      doClaims(count, timeout)
    }
    workList = f :: workList
  }
  
  def allocationFailureRate(rate: Double) {
    alloc.setAllocationFailureRate(rate)
  }
  
  def deallocationFailureRate(rate: Double) {
    alloc.setDeallocationFailureRate(rate)
  }
  
  def setTargetSize(size: Int) {
    pool.setTargetSize(size)
  }
  
  def fuzz() {
    config.setSize(poolSize)
    config.setAllocator(alloc)
    pool = poolFactory(config)
    
    val relativeThreadCount = (poolSize * relativeThreadFactor)
    val threadCount = math.max(absoluteThreadCount, relativeThreadCount).toInt
    
    val startLatch = new CountDownLatch(1)
    val workers = buildWorkers(startLatch, threadCount)
    workers.foreach(_.start())
    startLatch.countDown()
    Thread.sleep(fuzzTime)
    workers.foreach(_.interrupt())
    workers.foreach(_.join())
    
    if (!pool.shutdown().await(new Timeout(10, TimeUnit.SECONDS))) {
      throw new IllegalStateException("Pool did not shut down!")
    }
    
    alloc.validateAllocations()
    
    workList = List()
    alloc.reset()
  }
  
  def buildWorkers(latch: CountDownLatch, threadCount: Int): List[Thread] = {
    val workers = workList.map(worker(latch, _))
    workers
  }
  
  val workerCounter = new AtomicInteger
  def worker(latch: CountDownLatch, work: () => Unit): Thread = {
    val script: Runnable = new Runnable {
      def run() {
        latch.await()
        try {
          doWork()
        } catch {
          case e: InterruptedException => ()
        }
      }
      
      def doWork() {
        while (!Thread.currentThread().isInterrupted()) {
          for (_ <- 1 to 100) {
            try {
              work()
            } catch {
              case e: PoolException => if (e.getCause() != TheAllocator.expectableException)
                errorT(e)
            }
          }
        }
      }
    }
    new Thread(script, "Worker-" + workerCounter.incrementAndGet())
  }
}
