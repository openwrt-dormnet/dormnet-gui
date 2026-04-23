package io.github.sgpublic.dormnet.targets.core

import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.lifecycle.ViewModel
import org.jetbrains.compose.resources.StringResource

interface LoginParams

val LocalDormnetViewModel = staticCompositionLocalOf<DormnetViewModel<out LoginParams>> {
    error("CompositionLocal LocalDormnetViewModel not present")
}

abstract class DormnetViewModel<T: LoginParams>: ViewModel() {
    abstract fun createLoginParams(): T
}

abstract class DormnetTarget<T: LoginParams> {
    abstract val title: StringResource

    open val HttpClient = io.github.sgpublic.dormnet.core.HttpClient

    @Composable
    abstract operator fun invoke(loading: Boolean, onLoadingChanged: (Boolean) -> Unit)

    @Composable
    protected fun <VM: DormnetViewModel<T>> viewModel(): VM {
        return LocalDormnetViewModel.current as VM
    }

    abstract fun createViewModel(): DormnetViewModel<T>

    suspend fun login(params: LoginParams): Result<String> {
        return doLogin(params as T)
    }

    protected abstract suspend fun doLogin(params: T): Result<String>
}
