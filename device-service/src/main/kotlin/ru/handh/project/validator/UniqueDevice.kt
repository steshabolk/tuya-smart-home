package ru.handh.project.validator

import jakarta.validation.Constraint
import jakarta.validation.ConstraintValidator
import jakarta.validation.ConstraintValidatorContext
import jakarta.validation.Payload
import ru.handh.project.dto.request.CreateDeviceRequest
import ru.handh.project.repository.DeviceRepository
import kotlin.reflect.KClass

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.CLASS)
@Constraint(validatedBy = [UniqueDeviceValidator::class])
annotation class UniqueDevice(
    val message: String = "{api.validation.deviceNotUnique}",
    val groups: Array<KClass<*>> = [],
    val payload: Array<KClass<out Payload>> = []
)

class UniqueDeviceValidator(private val deviceRepository: DeviceRepository)
    : ConstraintValidator<UniqueDevice, CreateDeviceRequest> {

    override fun isValid(value: CreateDeviceRequest, context: ConstraintValidatorContext) =
        value.tuyaDeviceId?.let {
            !deviceRepository.existsByTuyaDeviceId(value.tuyaDeviceId)
                .also {
                    context.disableDefaultConstraintViolation()
                    context
                        .buildConstraintViolationWithTemplate(context.defaultConstraintMessageTemplate)
                        .addPropertyNode("tuyaDeviceId")
                        .addConstraintViolation()
                }
        }?: true
}
