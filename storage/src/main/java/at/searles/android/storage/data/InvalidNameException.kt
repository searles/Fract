package at.searles.android.storage.data

import java.lang.Exception
import java.lang.RuntimeException

class InvalidNameException(message: String, cause: Exception?): RuntimeException(message, cause)