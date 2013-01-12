package stormpot.fuzzer

object DefaultedMain {
  def main(args: Array[String]): Unit = {
    System.setProperty("minutes", "1")
    Main.main(null)
  }
}
