package stormpot.fuzzer.situation;

public abstract class Situation implements Runnable {

  private int weight;
  private Category category;
  private Mode mode;

  public void init(int weight, Category category, Mode mode) {
    this.weight = weight;
    this.category = category;
    this.mode = mode;
  }

  public void begin() {
  }
  
  public void run() {
  }
  
  public void end() {
  }

  public boolean hasCategory(Category category) {
    return this.category == category;
  }
}
