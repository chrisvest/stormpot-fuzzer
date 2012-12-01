package stormpot.fuzzer

import stormpot._
import stormpot.qpool.QueuePool
import stormpot.bpool.BlazePool
import java.util.concurrent.TimeUnit

object Main {
  
  def main(args: Array[String]): Unit = {
    val poolName = System.getProperty("pool", "blaze")
    val poolFactory = getPoolFactory(poolName)
    val time = calculateRunningTime()
    
    printf("Fuzz-testing pool for %s millis: %s%n", time, poolName)
    
    val plan = Planner.plan(time, poolFactory)
    println(plan)
    plan.execute()
    
    println("\nDone")
  }

  def getPoolFactory(name: String): PoolFactory = name match {
    case "blaze" => (config => new BlazePool(config))
    case "queue" => (config => new QueuePool(config))
    case _ => throw new IllegalArgumentException(
        "Don't know this pool type: " + name) 
  }
  
  def calculateRunningTime(): Long = {
      val time = getAsMillis("hours", 0) +
          getAsMillis("minutes", 0) +
          getAsMillis("seconds", 0);
      if (time == 0) 30000 else time
  }
  
  def getAsMillis(name: String, default: Long): Long = {
    val unit = TimeUnit.valueOf(name.toUpperCase())
    val time = java.lang.Long.getLong(name, default)
    unit.toMillis(time)
  }
}
