package stormpot.fuzzer.situation;

public class SetThreadCount extends Situation {

  private final int threads;

  public SetThreadCount(int threads) {
    this.threads = threads;
  }

  @Override
  public String toString() {
    return "SetThreadCount(" + threads + ")";
  }
}
