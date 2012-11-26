package stormpot

import stormpot._

package object fuzzer {
  type PoolFactory = (Config[ThePoolable] => LifecycledPool[ThePoolable])
}
