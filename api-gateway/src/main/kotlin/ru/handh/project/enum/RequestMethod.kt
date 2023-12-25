package ru.handh.project.enum

enum class RequestMethod {
    GET, POST, PUT, DELETE;

    companion object {
        fun from(s: String): RequestMethod = values().find { it.name == s }!!

        val allMethods: List<RequestMethod> = RequestMethod.values().toList()
    }
}
