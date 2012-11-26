package stormpot.fuzzer.situation;

public class SetPoolSizeRelativeToThreads extends Situation {

  private final int relativePoolSize;

  public SetPoolSizeRelativeToThreads(int poolSizeRelativeToThreadCount) {
    this.relativePoolSize = poolSizeRelativeToThreadCount;
  }

  @Override
  public String toString() {
    return "SetPoolSizeRelativeToThreads(" + relativePoolSize + ")";
  }
}
