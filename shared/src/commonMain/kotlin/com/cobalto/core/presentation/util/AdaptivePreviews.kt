package com.cobalto.core.presentation.util

import androidx.compose.ui.tooling.preview.Preview

@Preview(name = "Phone", device = "spec:width=411dp,height=891dp")
@Preview(name = "Tablet", device = "spec:width=1280dp,height=800dp,orientation=landscape")
@Preview(name = "Desktop", device = "spec:width=1920dp,height=1080dp")
annotation class AdaptivePreviews
