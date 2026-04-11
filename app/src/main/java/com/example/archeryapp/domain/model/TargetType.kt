package com.example.archeryapp.domain.model

enum class TargetType(val code: String, val displayName: String) {
    MC("MC", "Standard Multicolor"),
    BF("BF", "Standard Blue Face"),
    MINI_MC("MINI_MC", "Mini Multicolor"),
    TRIPLE("TRIPLE", "Triple");

    companion object {
        fun fromCode(code: String?): TargetType =
            entries.firstOrNull { it.code == code } ?: MINI_MC
    }
}
