package com.example.githubrepoviewer.ui.providers

import androidx.annotation.StringRes

interface ResourcesProvider {
    fun getString(@StringRes resId: Int): String
    fun getString(@StringRes resId: Int, vararg formatArgs: Any): String
}
