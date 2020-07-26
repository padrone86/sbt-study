object CubeCalculator extends App {
  println(s"Volume=${cube(10)}")

  def cube(x: Int) = {
    x * x * x
  }
}
