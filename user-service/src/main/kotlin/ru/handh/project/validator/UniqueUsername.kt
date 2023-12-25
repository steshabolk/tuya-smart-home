package ru.handh.project.validator

import jakarta.validation.Constraint
import jakarta.validation.ConstraintValidator
import jakarta.validation.ConstraintValidatorContext
import jakarta.validation.Payload
import ru.handh.project.dto.request.RegisterRequest
import ru.handh.project.repository.UserRepository
import kotlin.reflect.KClass

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.CLASS)
@Constraint(validatedBy = [UniqueUsernameValidator::class])
annotation class UniqueUsername(
    val message: String = "{api.validation.usernameNotUnique}",
    val groups: Array<KClass<*>> = [],
    val payload: Array<KClass<out Payload>> = []
)

class UniqueUsernameValidator(private val userRepository: UserRepository) :
    ConstraintValidator<UniqueUsername, RegisterRequest> {

    override fun isValid(value: RegisterRequest, context: ConstraintValidatorContext) =
        value.username
            ?.let {
                !userRepository.existsByUsername(value.username)
                    .also {
                        context.disableDefaultConstraintViolation()
                        context
                            .buildConstraintViolationWithTemplate(context.defaultConstraintMessageTemplate)
                            .addPropertyNode("username")
                            .addConstraintViolation()
                    }
            } ?: true
}
