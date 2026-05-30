package com.example.util

import kotlin.math.*

class MathEvaluator(private val isDegreeMode: Boolean = true) {

    fun evaluate(expression: String): Double {
        if (expression.isBlank()) return 0.0
        val sanitized = sanitize(expression)
        return Parser(sanitized, isDegreeMode).parse()
    }

    private fun sanitize(expr: String): String {
        return expr
            .replace("×", "*")
            .replace("÷", "/")
            .replace("π", "(${Math.PI})")
            .replace("e", "(${Math.E})")
            .replace("sin⁻¹", "asin")
            .replace("cos⁻¹", "acos")
            .replace("tan⁻¹", "atan")
            .replace("mod", "%")
            .replace("√", "sqrt")
            .replace("∛", "cbrt")
    }

    private class Parser(private val str: String, private val isDegreeMode: Boolean) {
        private var pos = -1
        private var ch = ' '

        private fun nextChar() {
            pos++
            ch = if (pos < str.length) str[pos] else '\u0000'
        }

        private fun eat(charToEat: Char): Boolean {
            while (ch == ' ') nextChar()
            if (ch == charToEat) {
                nextChar()
                return true
            }
            return false
        }

        fun parse(): Double {
            nextChar()
            val x = parseExpression()
            if (pos < str.length) {
                throw IllegalArgumentException("Unexpected character: $ch")
            }
            return x
        }

        private fun parseExpression(): Double {
            var x = parseTerm()
            while (true) {
                if (eat('+')) x += parseTerm()
                else if (eat('-')) x -= parseTerm()
                else break
            }
            return x
        }

        private fun parseTerm(): Double {
            var x = parseFactor()
            while (true) {
                if (eat('*')) x *= parseFactor()
                else if (eat('/')) {
                    val divisor = parseFactor()
                    if (divisor == 0.0) throw ArithmeticException("Division by zero")
                    x /= divisor
                } else if (eat('%')) {
                    val divisor = parseFactor()
                    if (divisor == 0.0) throw ArithmeticException("Modulo by zero")
                    x %= divisor
                } else break
            }
            return x
        }

        private fun parseFactor(): Double {
            if (eat('+')) return parseFactor()
            if (eat('-')) return -parseFactor()

            var x: Double
            val startPos = this.pos
            if (eat('(')) {
                x = parseExpression()
                eat(')')
            } else if ((ch in '0'..'9') || ch == '.') {
                while ((ch in '0'..'9') || ch == '.') nextChar()
                x = str.substring(startPos, this.pos).toDouble()
            } else if (ch in 'a'..'z' || ch == '√' || ch == '∛') {
                if (ch == '√') {
                    nextChar()
                    x = parseFactor()
                    x = sqrt(x)
                } else if (ch == '∛') {
                    nextChar()
                    x = parseFactor()
                    x = cbrt(x)
                } else {
                    while (ch in 'a'..'z' || ch in '0'..'9') nextChar()
                    val func = str.substring(startPos, this.pos)
                    // Check if open parenthesis is there
                    val hasParen = eat('(')
                    x = parseExpression()
                    if (hasParen) {
                        eat(')')
                    }
                    x = when (func) {
                        "sin" -> if (isDegreeMode) sin(Math.toRadians(x)) else sin(x)
                        "cos" -> if (isDegreeMode) cos(Math.toRadians(x)) else cos(x)
                        "tan" -> if (isDegreeMode) tan(Math.toRadians(x)) else tan(x)
                        "sinh" -> sinh(x)
                        "cosh" -> cosh(x)
                        "tanh" -> tanh(x)
                        "asin" -> {
                            val r = asin(x)
                            if (isDegreeMode) Math.toDegrees(r) else r
                        }
                        "acos" -> {
                            val r = acos(x)
                            if (isDegreeMode) Math.toDegrees(r) else r
                        }
                        "atan" -> {
                            val r = atan(x)
                            if (isDegreeMode) Math.toDegrees(r) else r
                        }
                        "log" -> log10(x)
                        "ln" -> ln(x)
                        "sqrt" -> sqrt(x)
                        "cbrt" -> cbrt(x)
                        "exp" -> exp(x)
                        else -> throw IllegalArgumentException("Unknown function: $func")
                    }
                }
            } else {
                throw IllegalArgumentException("Unexpected character: $ch")
            }

            if (eat('^')) {
                x = x.pow(parseFactor())
            }

            while (eat('!')) {
                x = factorial(x)
            }

            return x
        }

        private fun factorial(n: Double): Double {
            if (n < 0.0) throw IllegalArgumentException("Factorial of negative")
            val intVal = n.toInt()
            if (n != intVal.toDouble()) {
                // Approximate with gamma function
                return gammaApproximation(n + 1)
            }
            var res = 1.0
            for (i in 1..intVal) {
                res *= i
            }
            return res
        }

        private fun gammaApproximation(x: Double): Double {
            val g = 7
            val p = doubleArrayOf(
                0.99999999999980993, 676.5203681218851, -1259.1392167224028,
                771.32342877765313, -176.61502916214059, 12.507343278686905,
                -0.13857109526572012, 9.9843695780195716e-6, 1.5056327351493116e-7
            )
            var z = x
            if (z < 0.5) return Math.PI / (sin(Math.PI * z) * gammaApproximation(1.0 - z))
            z -= 1.0
            var sum = p[0]
            for (i in 1 until g + 2) {
                sum += p[i] / (z + i)
            }
            val t = z + g + 0.5
            return sqrt(2.0 * Math.PI) * t.pow(z + 0.5) * exp(-t) * sum
        }
    }
}
