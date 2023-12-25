package ru.handh.project.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import jakarta.validation.constraints.NotBlank

@Entity
@Table(name = "users")
data class UserEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    val id: Int,

    @field:NotBlank
    @Column(name = "name")
    val name: String,

    @field:NotBlank
    @Column(name = "username")
    val username: String,

    @field:NotBlank
    @Column(name = "password")
    val password: String
)
