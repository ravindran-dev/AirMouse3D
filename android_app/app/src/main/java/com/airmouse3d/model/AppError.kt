package com.airmouse3d.model

/** Recoverable error conditions the app surfaces to the user, per the error-handling spec. */
sealed class AppError {
    data object NoNetwork : AppError()
    data object NotPaired : AppError()
    data object SensorsUnavailable : AppError()
    data object PermissionDenied : AppError()
    data class Unknown(val message: String) : AppError()
}
