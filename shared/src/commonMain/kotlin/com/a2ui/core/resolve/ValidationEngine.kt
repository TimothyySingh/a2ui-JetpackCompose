package com.a2ui.core.resolve

import com.a2ui.core.model.A2UICheck

/**
 * Result of validating a component's checks.
 */
data class ValidationResult(
    val isValid: Boolean,
    val errors: List<String>
) {
    companion object {
        val Valid = ValidationResult(isValid = true, errors = emptyList())
    }
}

/**
 * Processes the checks array on Checkable components
 * (TextField, CheckBox, ChoicePicker, Slider, DateTimeInput).
 *
 * Each check has a condition (DynamicBoolean) and a message (DynamicString).
 * If condition resolves to false, the message is added to errors.
 */
class ValidationEngine(private val resolver: DynamicResolver) {

    fun validate(checks: List<A2UICheck>?): ValidationResult {
        if (checks.isNullOrEmpty()) return ValidationResult.Valid

        val errors = mutableListOf<String>()
        for (check in checks) {
            val conditionResult = resolver.resolveBoolean(check.condition)
            if (conditionResult != true) {
                val message = resolver.resolveString(check.message) ?: "Validation failed"
                errors.add(message)
            }
        }

        return ValidationResult(
            isValid = errors.isEmpty(),
            errors = errors
        )
    }
}
