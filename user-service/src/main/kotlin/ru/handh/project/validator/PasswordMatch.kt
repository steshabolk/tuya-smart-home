package ru.handh.project.validator

import jakarta.validation.Constraint
import jakarta.validation.ConstraintValidator
import jakarta.validation.ConstraintValidatorContext
import jakarta.validation.Payload
import ru.handh.project.dto.request.RegisterRequest
import kotlin.reflect.KClass

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.CLASS)
@Constraint(validatedBy = [PasswordMatchValidator::class])
annotation class PasswordMatch(
    val message: String = "{api.validation.passwordNotMatch}",
    val groups: Array<KClass<*>> = [],
    val payload: Array<KClass<out Payload>> = []
)

class PasswordMatchValidator : ConstraintValidator<PasswordMatch, RegisterRequest> {

    override fun isValid(value: RegisterRequest, context: ConstraintValidatorContext) =
        value.password == value.confirmPassword
            .also {
                context.disableDefaultConstraintViolation()
                context
                    .buildConstraintViolationWithTemplate(context.defaultConstraintMessageTemplate)
                    .addPropertyNode("confirmPassword")
                    .addConstraintViolation()
            }
}
