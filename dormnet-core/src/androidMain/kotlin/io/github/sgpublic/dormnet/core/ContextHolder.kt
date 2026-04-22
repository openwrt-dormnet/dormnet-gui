package io.github.sgpublic.dormnet.core

import android.content.Context
import java.lang.ref.WeakReference

private var contextRef: WeakReference<Context>? = null

fun registerContext(context: Context) {
    contextRef = WeakReference(context.applicationContext)
}

fun <T> useContext(block: Context.() -> T): T {
    val context = contextRef?.get()
        ?: throw IllegalStateException("Context is not registered")
    return context.block()
}
