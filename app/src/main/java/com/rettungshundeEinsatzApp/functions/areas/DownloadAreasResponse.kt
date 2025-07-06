package com.rettungshundeEinsatzApp.functions.areas

data class DownloadAreasResponse(
    val status: String,
    val message: String,
    val data: List<DownloadArea>?
)