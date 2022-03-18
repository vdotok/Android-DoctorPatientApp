package com.vdotok.connect.extensions

import java.util.regex.Pattern


fun String.containsNonAlphaNumeric() : Boolean {
    val p = Pattern.compile("[!@#$%&*()_+=|<>?{}\\[\\]~-¥¢£ø]")
    return p.matcher(this).find()
}



