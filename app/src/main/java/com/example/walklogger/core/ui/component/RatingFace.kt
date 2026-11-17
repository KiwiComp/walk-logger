package com.example.walklogger.core.ui.component

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.walklogger.R
import com.example.walklogger.core.ui.theme.AppSize
import com.example.walklogger.core.ui.theme.WalkLoggerTheme

private val DefaultSize = AppSize.chipHeight

/** Coloured face for a 1..7 walk rating, from very angry to very happy. */
@Composable
fun RatingFace(rating: Int, modifier: Modifier = Modifier, size: Dp = DefaultSize) {
    Image(
        painter = painterResource(ratingFaceDrawable(rating)),
        contentDescription = stringResource(R.string.rating_face_description, rating),
        modifier = modifier.size(size),
    )
}

@DrawableRes
private fun ratingFaceDrawable(rating: Int): Int = when (rating) {
    1 -> R.drawable.rating_1_very_angry
    2 -> R.drawable.rating_2_angry
    3 -> R.drawable.rating_3_sad
    4 -> R.drawable.rating_4_neutral
    5 -> R.drawable.rating_5_slight_smile
    6 -> R.drawable.rating_6_happy
    7 -> R.drawable.rating_7_very_happy
    else -> throw IllegalArgumentException("Rating must be 1..7, was $rating")
}

@Preview(showBackground = true)
@Composable
private fun RatingFacePreview() {
    WalkLoggerTheme {
        Row { (1..7).forEach { RatingFace(rating = it) } }
    }
}
