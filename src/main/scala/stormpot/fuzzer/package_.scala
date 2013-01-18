package stormpot

import stormpot._

package object fuzzer {
  type PoolType = LifecycledPool[Poolable] with ResizablePool[Poolable]
  type PoolFactory = (Config[Poolable] => PoolType)
  
  def error(msg: String) {
    System.out.println(msg)
    System.out.flush()
    System.exit(-1)
  }
  
  def errorT(e: Throwable) {
    e.printStackTrace(System.out)
    System.out.flush()
    System.exit(-1)
  }
}
