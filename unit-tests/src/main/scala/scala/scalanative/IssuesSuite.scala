package scala.scalanative
import native.{CFunctionPtr1, CFunctionPtr0}
import native.{CInt, CFloat, CDouble}

object IssuesSuite extends tests.Suite {

  def foo(arg: Int): Unit                        = ()
  def crash(arg: CFunctionPtr1[Int, Unit]): Unit = ()
  def lifted208Test(): Unit                      = crash(foo _)

  test("#208") {
    // If we put the test directly, behind the scenes, this will
    // create a nested closure with a pointer to the outer one
    // and the latter is not supported in scala-native
    lifted208Test()
  }

  test("#253") {
    class Cell(val value: Int)

    val arr     = Array(new Cell(1), new Cell(2))
    val reverse = arr.reverse

    assert(reverse(0).value == 2)
    assert(reverse(1).value == 1)
  }

  test("#260") {
    def getStr(): String = {
      val bytes = Array('h'.toByte, 'o'.toByte, 'l'.toByte, 'a'.toByte)
      new String(bytes)
    }

    val sz = getStr()

    assert("hola" == sz)
    assert("hola".equals(sz))
  }

  test("#314") {
    // Division by zero is undefined behavior in production mode.
    // Optimizer can assume it never happens and remove unused result.
    assert {
      try {
        5 / 0
        true
      } catch {
        case _: Throwable =>
          false
      }
    }
  }

  test("#326") {
    abstract class A
    case class S[T](a: T) extends A

    def check(a: A) = a match {
      case S(d: Double) => "double"
      case S(d: Int)    => "int"
      case _            => "neither"
    }

    def main(args: Array[String]): Unit = {
      val (dbl, int, obj) = (S(2.3), S(2), S(S(2)))
      assert(check(dbl) == "double")
      assert(check(int) == "int")
      assert(check(obj) == "neither")
    }
  }

  test("#327") {
    val a = BigInt(1)
    assert(a.toInt == 1)
  }

  test("#337") {
    case class TestObj(value: Int)
    val obj = TestObj(10)
    assert(obj.value == 10)
  }

  test("#350") {
    val div = java.lang.Long.divideUnsigned(42L, 2L)
    assert(div == 21L)
  }

  test("#374") {
    assert("42" == bar(42))
    assert("bar" == bar_i32())
  }

  def bar(i: Int): String = i.toString
  def bar_i32(): String   = "bar"

  test("#376") {
    val m     = scala.collection.mutable.Map.empty[String, String]
    val hello = "hello"
    val world = "world"
    m(hello) = world
    val h = m.getOrElse(hello, "Failed !")
    assert(h equals world)
  }

  val fptrBoxed: CFunctionPtr0[Integer]  = () => new Integer(1)
  val fptr: CFunctionPtr0[CInt]          = () => 1
  val fptrFloat: CFunctionPtr0[CFloat]   = () => 1.0.toFloat
  val fptrDouble: CFunctionPtr0[CDouble] = () => 1.0
  def intIdent(x: Int): Int              = x
  test("#382") {
    /// that gave NPE

    import scala.scalanative.native._
    intIdent(fptr())
    assert(fptr() == 1)

    // Reported issue
    println(fptr())
    println(fptrFloat())
    println(fptrBoxed())

    // Other variations which must work as well
    val x1 = fptr()
    println(x1)
    val x2 = fptrFloat()
    println(x2)

    // Should be possible
    val conv1: Int = (1: Float).cast[Int]
    // Should fail
    //val conv2: Int = (1: Double).cast[Int]
  }

  test("#404") {
    // this must not throw an exception
    this.getClass.##
  }

  test("#424") {
    // this used not to link
    val cls = classOf[Array[Unit]]
  }

  test("#449") {
    import scalanative.native.Ptr
    import scala.scalanative.runtime.ByteArray
    val bytes = new Array[Byte](2)
    bytes(0) = 'b'.toByte
    bytes(1) = 'a'.toByte
    val p: Ptr[Byte] = bytes.asInstanceOf[ByteArray].at(0)
    assert(!p == 'b'.toByte)
    assert(!(p + 1) == 'a'.toByte)
  }
}
