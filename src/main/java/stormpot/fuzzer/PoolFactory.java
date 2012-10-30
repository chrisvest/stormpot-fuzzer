package stormpot.fuzzer;

import stormpot.Config;
import stormpot.Pool;
import stormpot.Poolable;

public interface PoolFactory {
  public <T extends Poolable> Pool<T> createPool(Config<T> config);
}
