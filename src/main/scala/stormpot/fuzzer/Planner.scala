package stormpot.fuzzer

import stormpot._
import scala.collection.mutable.ArrayStack

object Planner {
  sealed abstract class Conflictivity
  case object Contention extends Conflictivity
  case object Size extends Conflictivity
  case object Meshing extends Conflictivity
  
  case class Action(
      name: String,
      conflictivity: Conflictivity,
      effect: Fuzzer => Unit)
  
  class Part(actions: Seq[Action]) {
    def configure(fuzzer: Fuzzer) {
      actions.foreach(_.effect(fuzzer))
    }
    
    override def toString() = actions.map(_.name).mkString("Part(", "; ", ")")
  }
  
  class Plan(parts: Seq[Part], timePerPart: Long, factory: PoolFactory) {
    def execute(): Unit = {
      for (part <- parts) {
        val alloc = new TheAllocator()
        val fuzzer = new Fuzzer(new Config().setAllocator(alloc))
        fuzzer.fuzzTime = timePerPart
        part.configure(fuzzer)
        fuzzer.fuzz(factory)
        print('.')
      }
    }
    
    override def toString() = parts.mkString("Plan [\n  ", "\n  ", "\n]")
  }
  
  val minimumPartTime = 500 // milliseconds
  val mediumContention = Runtime.getRuntime().availableProcessors()
  val highContention = mediumContention * 4 + 1
  val lowContention = math.max(mediumContention / 2, 1)
  
  /*
   * Interesting scenarios:
   *  - thread count > core count
   *  - thread count = core count
   *  - thread count < core count
   *  
   *  - more threads than pool objects
   *  - fewer threads than pool objects
   *  - resizing the pool larger
   *  - resizing the pool smaller
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
  
  val actions = List(
//      Action(Contention, _.absoluteThreadCount = mediumContention),
      Action("High contention", Contention, _.absoluteThreadCount = highContention),
//      Action(Contention, _.absoluteThreadCount = lowContention),
//      Action(Contention, _.relativeThreadFactor = 1.0),
//      Action(Contention, _.relativeThreadFactor = 1.2),
//      Action(Contention, _.relativeThreadFactor = 0.8),
      
      Action("Pool size 1", Size, _.poolSize = 1),
      Action("Pool size 2", Size, _.poolSize = 2),
      Action("Pool size 10", Size, _.poolSize = 10),
      Action("Pool size 100", Size, _.poolSize = 100),
      
      Action("Continuously adjusting pool size up to 10", Size,
          (f:Fuzzer) => f.withService(2, 5, f.setTargetSize(_:Int), incBound(10)))
  )
  
  def incBound(top:Int) = {
    x:Int => 1 + ((1 + x) % top)
  }
  
  def plan(time: Long, poolFactory: PoolFactory): Plan = {
    // group conflicting elements together
    // iterate combinations of actions from each group
    // fill with combinations of non-conflicting actions
    val actionGroups = actions.groupBy(_.conflictivity)
    val meshingActions = actionGroups.get(Meshing)
    val conflictingActions = (actionGroups - Meshing).values
    val actionCombos = combinationsOf(conflictingActions.toList).toList
    
    var plan = actionCombos.map(x => new Part(x))
    var partTime = 500L
    val partsPossible = (time / partTime).toInt
    
    if (partsPossible > plan.length) {
      partTime = time / plan.length
    } else {
      plan = plan.take(partsPossible)
    }
    
    new Plan(plan, partTime, poolFactory)
  }
  
  def combinationsOf(xs: Iterable[List[Action]]): Seq[Seq[Action]] = {
    val output = ArrayStack[List[Action]]()
    combineInto(xs, List(), output)
    output
  }
  
  def combineInto(
      xs: Iterable[List[Action]],
      path: List[Action],
      res: ArrayStack[List[Action]]): Unit = xs match {
    case actions :: Nil =>
      actions.foreach(action => res.push(action :: path))
    case actions :: more =>
      actions.foreach(action => combineInto(more, action :: path, res))
  }
  
  def main(args: Array[String]) {
    val groups = actions.groupBy(_.conflictivity)
    val combos = combinationsOf(groups.values.toList)
    println(combos.count(_ == null) + " nulls to begin with")
    val parts = combos.map(new Part(_))
    println(parts.count(_ == null) + " nulls found after mapping")
  }
}
