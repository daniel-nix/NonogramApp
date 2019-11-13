package com.example.nonogramapp.exceptions

import java.lang.RuntimeException

class UnreadablePatternException(message: String? = null): RuntimeException(message) {
}