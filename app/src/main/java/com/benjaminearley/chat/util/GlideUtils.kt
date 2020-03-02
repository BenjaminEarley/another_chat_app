package com.benjaminearley.chat.util

import android.graphics.drawable.Drawable
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target

fun loadListener(block: (loaded: Boolean) -> Unit) = GlideDrawableLoadListener(block)

/**
 * A [RequestListener] which executes an action when a [Drawable] loads or fails to load.
 */
class GlideDrawableLoadListener(private val block: (loaded: Boolean) -> Unit) :
    RequestListener<Drawable> {

    override fun onResourceReady(
        resource: Drawable?,
        model: Any?,
        target: Target<Drawable>?,
        dataSource: DataSource?,
        isFirstResource: Boolean
    ): Boolean {
        block(true)
        return false
    }

    override fun onLoadFailed(
        e: GlideException?,
        model: Any?,
        target: Target<Drawable>?,
        isFirstResource: Boolean
    ): Boolean {
        block(false)
        return true
    }
}
