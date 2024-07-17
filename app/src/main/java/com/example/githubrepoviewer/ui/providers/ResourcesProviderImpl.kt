package com.example.githubrepoviewer.ui.providers

import android.content.Context
import android.graphics.Color
import android.util.Log
import androidx.annotation.StringRes
import androidx.core.content.ContextCompat
import com.example.githubrepoviewer.R
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ResourcesProviderImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : ResourcesProvider {

    private val languageColors: Map<String, Int> by lazy { loadLanguageColors() }

    override fun getString(@StringRes resId: Int): String {
        return context.getString(resId)
    }

    override fun getString(@StringRes resId: Int, vararg formatArgs: Any): String {
        return context.getString(resId, *formatArgs)
    }

    override fun getGithubColorForLanguage(language: String?): Int {
        return languageColors[language] ?: ContextCompat.getColor(context, R.color.error)
    }

    private fun loadLanguageColors(): Map<String, Int> {
        return runCatching {
            val jsonString =
                context.assets.open("github-lang-colors.json").bufferedReader().readText()
            val json = Json { ignoreUnknownKeys = true }
            val jsonObject = json.parseToJsonElement(jsonString).jsonObject
            jsonObject.entries.associateBy({ it.key },
                { Color.parseColor(it.value.jsonPrimitive.content) })
        }.getOrElse { e ->
            Log.e("ResourcesProvider", "Error reading language colors", e)
            emptyMap()
        }
    }
}
