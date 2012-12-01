package stormpot.fuzzer

import stormpot._
import java.util.concurrent.{CountDownLatch, TimeUnit}
import java.util.concurrent.atomic.AtomicInteger
import collection.Iterator
import scala.collection.mutable.ArrayStack

class Fuzzer(
    val config: Config[Poolable],
    var absoluteThreadCount: Int = -1,
    var relativeThreadFactor: Double = -1.0,
    var poolSize: Int = 10,
    var fuzzTime: Long = 500,
    var workList: List[() => Unit] = List()) {
  
  val longTimeout = new Timeout(1, TimeUnit.SECONDS)
  val serviceThreads = new ArrayStack[Runnable]
  var pool: PoolType = null;
  
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
  
  def setTargetSize(size: Int) {
    println("setting runtime target size to " + size)
    pool.setTargetSize(size)
  }
  
  def fuzz(poolFactory: PoolFactory) {
    config.setSize(poolSize)
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
    
    if (!pool.shutdown().await(longTimeout)) {
      throw new IllegalStateException("Pool did not shut down!")
    }
  }
  
  def buildWorkers(latch: CountDownLatch, threadCount: Int): List[Thread] = {
    val defaultWorkList = Iterator.continually(defaultWork())
    workList = workList ++ defaultWorkList.take(threadCount - workList.length)
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
            work()
          }
        }
      }
    }
    new Thread(script, "Worker-" + workerCounter.incrementAndGet())
  }
  
  def defaultWork(): (() => Unit) = () => {
    val obj = pool.claim(longTimeout)
    obj.release()
  }
}
