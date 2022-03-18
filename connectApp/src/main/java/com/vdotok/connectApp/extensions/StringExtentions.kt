package com.vdotok.connectApp.extensions

import java.util.regex.Pattern


fun String.containsNonAlphaNumeric() : Boolean {
    val p = Pattern.compile("[!@#$%&*()_+=|<>?{}\\[\\]~-¥¢£ø]")
    return p.matcher(this).find()
}



