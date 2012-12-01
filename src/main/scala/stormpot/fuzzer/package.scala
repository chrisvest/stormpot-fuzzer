package stormpot

import stormpot._

package object fuzzer {
  type PoolType = LifecycledPool[Poolable] with ResizablePool[Poolable]
  type PoolFactory = (Config[Poolable] => PoolType)
}
