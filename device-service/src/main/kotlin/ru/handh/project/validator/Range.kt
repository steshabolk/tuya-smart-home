package ru.handh.project.validator

import jakarta.validation.Constraint
import jakarta.validation.ConstraintValidator
import jakarta.validation.ConstraintValidatorContext
import jakarta.validation.Payload
import kotlin.reflect.KClass

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FIELD, AnnotationTarget.VALUE_PARAMETER, AnnotationTarget.PROPERTY_GETTER, AnnotationTarget.PROPERTY_SETTER)
@Constraint(validatedBy = [RangeValidator::class])
annotation class Range(
    val message: String = "{api.validation.range}",
    val groups: Array<KClass<*>> = [],
    val payload: Array<KClass<out Payload>> = [],
    val min: Int,
    val max: Int,
    val field: String
)

class RangeValidator : ConstraintValidator<Range, Int> {

    private var range: Range? = null

    override fun initialize(range: Range) {
        this.range = range
    }

    override fun isValid(value: Int?, context: ConstraintValidatorContext): Boolean {
        if (value == null) return true
        return range
            ?.run {
                (this.min..this.max).contains(value)
                    .also {
                        if (!it) {
                            context.disableDefaultConstraintViolation()
                            context
                                .buildConstraintViolationWithTemplate(context.defaultConstraintMessageTemplate)
                                .addPropertyNode(this.field)
                                .addConstraintViolation()
                        }
                    }
            } ?: true
    }
}
