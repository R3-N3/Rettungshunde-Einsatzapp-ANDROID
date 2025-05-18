package com.rettungshundeEinsatzApp.functions

fun hasMinLength(password: String) = password.length >= 8
fun hasLetter(password: String) = password.any { it.isLetter() }
fun hasDigit(password: String) = password.any { it.isDigit() }
fun hasSpecialChar(password: String) = password.any { !it.isLetterOrDigit() }