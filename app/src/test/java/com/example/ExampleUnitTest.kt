package com.example

import com.example.util.MathEvaluator
import org.junit.Assert.*
import org.junit.Test

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
  @Test
  fun addition_isCorrect() {
    assertEquals(4, 2 + 2)
  }

  @Test
  fun testPercentageLogic() {
    val evaluator = MathEvaluator()
    // Test 1000 % 3 equals 30 (3% of 1000)
    assertEquals(30.0, evaluator.evaluate("1000%3"), 0.0001)

    // Test mod expression: 10 mod 3 equals 1
    assertEquals(1.0, evaluator.evaluate("10 mod 3"), 0.0001)

    // Test trailing percent: 50% equals 0.5
    assertEquals(0.5, evaluator.evaluate("50%"), 0.0001)

    // Test trailing percent in operation: 50% / 2 equals 0.25
    assertEquals(0.25, evaluator.evaluate("50% / 2"), 0.0001)

    // Test binary percent in complex expression
    assertEquals(40.0, evaluator.evaluate("1000 % 3 + 10"), 0.0001)
  }
}
