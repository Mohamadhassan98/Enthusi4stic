package com.enthusi4stic.api.recyclerview

class UnexpectedViewTypeException(expected: String?, found: String?) :
    Exception("""Unexpected view type. expected "$expected" found $found"!""")

class UnexpectedFieldTypeException(expected: String?, found: String?) :
    Exception("""Unexpected field type. expected "$expected" found $found"!""")

class ViewNotFoundException(id: String) : Exception("View with id $id was not found!")

class OperationNotImplementedException(operation: Operation = Operation.Default) :
    Exception(operation.message) {
    enum class Operation(val message: String) {
        Default("Declared view type has not corresponding field or it's not implemented yet!"),
        VisibilityBind("Used Visibility View binding, but VisibilityBind interface Not implemented by data class!")
    }
}