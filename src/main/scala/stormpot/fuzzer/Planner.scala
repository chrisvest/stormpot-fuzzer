package stormpot.fuzzer

import stormpot._
import scala.collection.mutable.ArrayStack
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.TimeUnit

object Planner {
  private val actionCounter = new AtomicInteger()
  
  case class Action(
      name: String,
      effect: Fuzzer => Unit,
      installsThread: Boolean,
      requireObjs: Int,
      id: Int = actionCounter.incrementAndGet())
  
  case class Part(actions: Seq[Action]) {
    def configure(fuzzer: Fuzzer) {
      actions.foreach(_.effect(fuzzer))
    }
    
    override def toString() = actions.map(_.name).mkString("Part(", "; ", ")")
  }
  
  case class Phase(threads: Int, poolSize: Int, parts: Seq[Part]) {
    def run(fuzzer: Fuzzer) {
      fuzzer.absoluteThreadCount = threads
      fuzzer.poolSize = poolSize
      for (part <- parts) {
        printf("Fuzzing %s threads, %s objs: %s\n", threads, poolSize, part)
        part.configure(fuzzer)
        fuzzer.fuzz()
      }
    }
    
    override def toString() = parts.mkString(
        "  Phase " + threads + " threads, " + poolSize + " items [\n    ",
        ",\n    ", "]")
  }
  
  class Plan(phases: Seq[Phase], timePerPart: Long, factory: PoolFactory) {
    def execute(): Unit = {
      for (phase <- phases) {
        val fuzzer = new Fuzzer(new Config())
        fuzzer.fuzzTime = timePerPart
        fuzzer.poolFactory = factory
        phase.run(fuzzer)
      }
    }
    
    override def toString() = phases.mkString("Plan [\n", "\n", "\n]")
  }
  
  /*
   * Interesting scenarios:
   *  
   *  - elevated unparks
   *  - threads claiming just one object
   *  - threads claiming more than one object
   *  
   *  - high allocation failure rate
   *  - low allocation failure rate
   *  - deallocation failures
   *  - long timeouts
   *  - short timeouts
   *  - zero timeouts
   *  - objects expire fast
   *  - objects expire slowly
   *  - expiration throws exception
   *  - expiration does not throw exception
   */
  
  val longTimeout = new Timeout(1, TimeUnit.SECONDS)
  val shortTimeout = new Timeout(1, TimeUnit.MILLISECONDS)
  val zeroTimeout = new Timeout(0, TimeUnit.MILLISECONDS)
  val fillAction = Action("C1", _.claimRelease(1, longTimeout), true, 1)
  val actions = List(
      fillAction,
      Action("C2", _.claimRelease(2, longTimeout), true, 2),
      Action("C3", _.claimRelease(3, longTimeout), true, 3),
      Action("C1 w/ short timeout", _.claimRelease(1, shortTimeout), true, 1),
      Action("C1 w/ zero timeout", _.claimRelease(1, zeroTimeout), true, 1),
      Action("elevated unparks", f => f.everyMs(1, _.unparkThreads), false, 0),
      Action("elevated allocation failures", _.allocationFailureRate(0.5), false, 0)
  )
  
  def plan(time: Long, poolFactory: PoolFactory): Plan = {
    
    val cpus = Runtime.getRuntime().availableProcessors()
    val contentions = List(cpus * 4 + 1, cpus, math.max(cpus / 2, 1))
    val poolSizes = List(20, 8, 2, 1)
    
    var phases = for (threads <- contentions;
                      size <- Set(threads + 10, threads, 2, 1))
      yield Phase(threads, size, planWindow(threads, size))
    
    var partTime = 500L
    val partsPossible = (time / partTime).toInt
    val partSum = phases.foldLeft(0)(_ + _.parts.size)
    
    if (partsPossible > partSum) {
      partTime = time / partSum
    } else {
      phases = takeLimitParts(phases, partsPossible)
    }
    
    new Plan(phases, partTime, poolFactory)
  }
  
  def takeLimitParts(phases: List[Phase], limit: Int): List[Phase] = {
    val head = phases.head
    val headSize = head.parts.size
    if (headSize < limit) head :: takeLimitParts(phases.tail, limit - headSize)
    else List(Phase(head.threads, head.poolSize, head.parts.take(limit)))
  }
  
  def planWindow(threads: Int, size: Int): Seq[Part] = {
    val baseActions = actions.filter(_.requireObjs <= size)
    val actives = baseActions.filter(_.installsThread)
    val passives = baseActions.filterNot(_.installsThread)
    val parts = for (activeLimit <- 1 to threads;
                     activeActs <- actives.combinations(activeLimit);
                     passiveLimit <- 0 to passives.size;
                     passiveActs <- passives.combinations(passiveLimit))
      yield new Part(actSet(activeActs, passiveActs, threads - activeLimit))
    parts.distinct.sortBy(1 - _.actions.size)
  }
  
  def actSet(active: List[Action], passive: List[Action], fill: Int): Seq[Action] = {
    val acts = active ++ passive ++ Stream.continually(fillAction).take(fill)
    acts.sortBy(_.id)
  }
  
  def main(args: Array[String]) {
    planWindow(3, 10).foreach(println(_))
  }
}
