package stormpot.fuzzer;

import stormpot.Config;
import stormpot.Pool;
import stormpot.Poolable;
import stormpot.bpool.BlazePool;

public class BlazePoolFactory implements PoolFactory {

  @Override
  public <T extends Poolable> Pool<T> createPool(Config<T> config) {
    return new BlazePool<T>(config);
  }
}
