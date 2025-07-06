package com.rettungshundeEinsatzApp.functions.areas

data class UploadArea(
    val title: String,
    val description: String,
    val color: String,
    val points: List<UploadAreaPoint>
)