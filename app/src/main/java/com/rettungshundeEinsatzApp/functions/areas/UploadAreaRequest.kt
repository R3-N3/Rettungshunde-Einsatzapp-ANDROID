package com.rettungshundeEinsatzApp.functions.areas

data class UploadAreasRequest(
    val token: String,
    val areas: List<UploadArea>
)