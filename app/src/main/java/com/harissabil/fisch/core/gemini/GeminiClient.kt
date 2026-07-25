package com.harissabil.fisch.core.gemini

import com.google.ai.client.generativeai.GenerativeModel
import com.harissabil.fisch.BuildConfig
import com.harissabil.fisch.core.common.util.Constant.GEMINI_FLASH

class GeminiClient {

    val geneminiProVisionModel by lazy {
        GenerativeModel(
            modelName = GEMINI_FLASH,
            apiKey = BuildConfig.GEMINI_API_KEY,
        )
    }
}