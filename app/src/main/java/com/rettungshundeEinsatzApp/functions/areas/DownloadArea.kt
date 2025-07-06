package com.rettungshundeEinsatzApp.functions.areas

data class DownloadArea(
    val id: Int,
    val title: String,
    val description: String,
    val color: String,
    val points: List<DownloadAreaPoint>
)