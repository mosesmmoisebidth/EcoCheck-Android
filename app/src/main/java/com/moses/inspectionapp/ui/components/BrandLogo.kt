package com.moses.inspectionapp.ui.components

import androidx.annotation.RawRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import coil.compose.AsyncImage
import coil.decode.SvgDecoder
import coil.request.ImageRequest
import com.moses.inspectionapp.R

@Composable
fun BrandLogo(
    modifier: Modifier = Modifier,
    @RawRes resId: Int = R.raw.ecocheck_logo,
) {
    val context = LocalContext.current
    AsyncImage(
        model = ImageRequest.Builder(context)
            .data(resId)
            .decoderFactory(SvgDecoder.Factory())
            .build(),
        contentDescription = null,
        modifier = modifier,
        contentScale = ContentScale.Fit,
    )
}
