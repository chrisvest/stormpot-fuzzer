package stormpot.fuzzer;

import stormpot.Config;
import stormpot.Pool;
import stormpot.Poolable;
import stormpot.qpool.QueuePool;

public class QueuePoolFactory implements PoolFactory {

  @Override
  public <T extends Poolable> Pool<T> createPool(Config<T> config) {
    return new QueuePool<T>(config);
  }
}
