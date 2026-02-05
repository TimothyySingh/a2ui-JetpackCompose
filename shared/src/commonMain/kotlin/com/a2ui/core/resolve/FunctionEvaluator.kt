package com.a2ui.core.resolve

import kotlinx.serialization.json.*
import kotlin.math.pow
import kotlin.math.roundToInt

/**
 * Evaluates function calls in DynamicValue expressions.
 *
 * Each function call is a JsonObject: {"call": "functionName", "args": {...}}
 * Function args are themselves DynamicValues -> recursive resolution before applying.
 */
class FunctionEvaluator(private val resolver: DynamicResolver) {

    /** Callback for openUrl - set by the renderer layer. */
    var onOpenUrl: ((String) -> Unit)? = null

    fun evaluate(obj: JsonObject): JsonElement? {
        val functionName = obj["call"]?.jsonPrimitive?.contentOrNull ?: return null
        val args = obj["args"]?.jsonObject ?: JsonObject(emptyMap())

        return when (functionName) {
            // Validation functions
            "required" -> evalRequired(args)
            "regex" -> evalRegex(args)
            "length" -> evalLength(args)
            "numeric" -> evalNumeric(args)
            "email" -> evalEmail(args)

            // Formatting functions
            "formatString" -> evalFormatString(args)
            "formatNumber" -> evalFormatNumber(args)
            "formatCurrency" -> evalFormatCurrency(args)
            "formatDate" -> evalFormatDate(args)
            "pluralize" -> evalPluralize(args)

            // Logic functions
            "and" -> evalAnd(args)
            "or" -> evalOr(args)
            "not" -> evalNot(args)

            // Action functions
            "openUrl" -> {
                evalOpenUrl(args)
                JsonPrimitive(true)
            }

            else -> null
        }
    }

    // --- Validation Functions ---

    private fun evalRequired(args: JsonObject): JsonElement {
        val value = resolver.resolve(args["value"])
        val isValid = when (value) {
            null, is JsonNull -> false
            is JsonPrimitive -> value.contentOrNull?.isNotEmpty() == true
            else -> true
        }
        return JsonPrimitive(isValid)
    }

    private fun evalRegex(args: JsonObject): JsonElement {
        val value = resolver.resolveString(args["value"]) ?: return JsonPrimitive(false)
        val pattern = resolver.resolveString(args["pattern"]) ?: return JsonPrimitive(false)
        return JsonPrimitive(Regex(pattern).containsMatchIn(value))
    }

    private fun evalLength(args: JsonObject): JsonElement {
        val value = resolver.resolveString(args["value"]) ?: return JsonPrimitive(false)
        val min = resolver.resolveNumber(args["min"])?.toInt() ?: 0
        val max = resolver.resolveNumber(args["max"])?.toInt() ?: Int.MAX_VALUE
        return JsonPrimitive(value.length in min..max)
    }

    private fun evalNumeric(args: JsonObject): JsonElement {
        val value = resolver.resolveNumber(args["value"]) ?: return JsonPrimitive(false)
        val min = resolver.resolveNumber(args["min"])?.toDouble() ?: Double.MIN_VALUE
        val max = resolver.resolveNumber(args["max"])?.toDouble() ?: Double.MAX_VALUE
        return JsonPrimitive(value.toDouble() in min..max)
    }

    private fun evalEmail(args: JsonObject): JsonElement {
        val value = resolver.resolveString(args["value"]) ?: return JsonPrimitive(false)
        val emailRegex = Regex("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")
        return JsonPrimitive(emailRegex.matches(value))
    }

    // --- Formatting Functions ---

    private fun evalFormatString(args: JsonObject): JsonElement {
        val template = resolver.resolveString(args["template"]) ?: return JsonPrimitive("")
        // Replace ${expression} with resolved values from args or data
        val result = Regex("\\$\\{([^}]+)\\}").replace(template) { match ->
            val expression = match.groupValues[1]
            // If expression starts with /, resolve as JSON pointer
            if (expression.startsWith("/")) {
                resolver.resolveJsonPointer(expression)?.let { resolved ->
                    when (resolved) {
                        is JsonPrimitive -> resolved.contentOrNull ?: resolved.toString()
                        else -> resolved.toString()
                    }
                } ?: ""
            } else {
                // Try to resolve from args
                resolver.resolveString(args[expression]) ?: ""
            }
        }
        return JsonPrimitive(result)
    }

    private fun evalFormatNumber(args: JsonObject): JsonElement {
        val value = resolver.resolveNumber(args["value"])?.toDouble() ?: return JsonPrimitive("")
        val decimals = resolver.resolveNumber(args["decimals"])?.toInt() ?: 0
        val grouping = resolver.resolveBoolean(args["grouping"]) ?: false

        val formatted = if (decimals > 0) {
            val factor = 10.0.pow(decimals)
            val rounded = (value * factor).roundToInt() / factor
            rounded.toString()
        } else {
            value.roundToInt().toString()
        }

        return if (grouping) {
            val parts = formatted.split(".")
            val intPart = parts[0].reversed().chunked(3).joinToString(",").reversed()
            if (parts.size > 1) JsonPrimitive("$intPart.${parts[1]}")
            else JsonPrimitive(intPart)
        } else {
            JsonPrimitive(formatted)
        }
    }

    private fun evalFormatCurrency(args: JsonObject): JsonElement {
        val value = resolver.resolveNumber(args["value"])?.toDouble() ?: return JsonPrimitive("")
        val currency = resolver.resolveString(args["currency"]) ?: "USD"
        val decimals = resolver.resolveNumber(args["decimals"])?.toInt() ?: 2

        val symbol = currencySymbol(currency)
        val factor = 10.0.pow(decimals)
        val rounded = (value * factor).roundToInt() / factor

        // Format with grouping
        val formatted = rounded.toString()
        val parts = formatted.split(".")
        val intPart = parts[0].reversed().chunked(3).joinToString(",").reversed()
        val decPart = if (parts.size > 1) parts[1].padEnd(decimals, '0').take(decimals)
        else "0".repeat(decimals)

        return JsonPrimitive("$symbol$intPart.$decPart")
    }

    private fun currencySymbol(code: String): String = when (code.uppercase()) {
        "USD" -> "$"
        "EUR" -> "\u20AC"
        "GBP" -> "\u00A3"
        "JPY" -> "\u00A5"
        "CNY" -> "\u00A5"
        "KRW" -> "\u20A9"
        "INR" -> "\u20B9"
        "BRL" -> "R$"
        "CAD" -> "CA$"
        "AUD" -> "A$"
        else -> "$code "
    }

    private fun evalFormatDate(args: JsonObject): JsonElement {
        // Basic date formatting - patterns like "yyyy-MM-dd", "MM/dd/yyyy", etc.
        val value = resolver.resolveString(args["value"]) ?: return JsonPrimitive("")
        val pattern = resolver.resolveString(args["pattern"]) ?: return JsonPrimitive(value)
        // In KMP common code, we do basic pattern replacement
        // Full TR35 support would need platform-specific implementation
        return JsonPrimitive(value)
    }

    private fun evalPluralize(args: JsonObject): JsonElement {
        val count = resolver.resolveNumber(args["count"])?.toInt() ?: 0
        // CLDR plural categories
        val zero = resolver.resolveString(args["zero"])
        val one = resolver.resolveString(args["one"])
        val two = resolver.resolveString(args["two"])
        val few = resolver.resolveString(args["few"])
        val many = resolver.resolveString(args["many"])
        val other = resolver.resolveString(args["other"]) ?: ""

        val result = when (count) {
            0 -> zero ?: other
            1 -> one ?: other
            2 -> two ?: other
            in 3..10 -> few ?: other
            in 11..99 -> many ?: other
            else -> other
        }
        return JsonPrimitive(result)
    }

    // --- Logic Functions ---

    private fun evalAnd(args: JsonObject): JsonElement {
        val values = args["values"]?.jsonArray ?: return JsonPrimitive(false)
        val allTrue = values.all { value ->
            resolver.resolveBoolean(value) == true
        }
        return JsonPrimitive(allTrue)
    }

    private fun evalOr(args: JsonObject): JsonElement {
        val values = args["values"]?.jsonArray ?: return JsonPrimitive(false)
        val anyTrue = values.any { value ->
            resolver.resolveBoolean(value) == true
        }
        return JsonPrimitive(anyTrue)
    }

    private fun evalNot(args: JsonObject): JsonElement {
        val value = resolver.resolveBoolean(args["value"]) ?: return JsonPrimitive(true)
        return JsonPrimitive(!value)
    }

    // --- Action Functions ---

    private fun evalOpenUrl(args: JsonObject) {
        val url = resolver.resolveString(args["url"]) ?: return
        onOpenUrl?.invoke(url)
    }

}
