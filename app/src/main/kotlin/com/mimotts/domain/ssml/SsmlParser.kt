package com.mimotts.domain.ssml

data class SsmlSegment(
    val text: String,
    val voiceId: String? = null,
    val prosody: ProsodyConfig? = null
)

data class ProsodyConfig(
    val rate: Float? = null,
    val pitch: Float? = null,
    val volume: Float? = null
)

class SsmlParser {

    fun parse(text: String): List<SsmlSegment> {
        if (text.isBlank()) return emptyList()

        // 检测是否包含 SSML 标签
        if (!text.contains("<")) {
            return listOf(SsmlSegment(text = text.trim()))
        }

        val segments = mutableListOf<SsmlSegment>()
        val regex = Regex("""<voice\s+name="([^"]*)"[^>]*>(.*?)</voice>""", RegexOption.DOT_MATCHES_ALL)
        val prosodyRegex = Regex("""<prosody\s+([^>]*)>(.*?)</prosody>""", RegexOption.DOT_MATCHES_ALL)

        var lastIndex = 0
        val matches = regex.findAll(text)

        for (match in matches) {
            // 处理 voice 标签前的普通文本
            if (match.range.first > lastIndex) {
                val plainText = text.substring(lastIndex, match.range.first).trim()
                if (plainText.isNotEmpty()) {
                    segments.add(SsmlSegment(text = plainText))
                }
            }

            val voiceId = match.groupValues[1]
            val content = match.groupValues[2].trim()

            // 检查内容中是否包含 prosody 标签
            val prosodyMatch = prosodyRegex.find(content)
            if (prosodyMatch != null) {
                val prosodyAttrs = prosodyMatch.groupValues[1]
                val prosodyText = prosodyMatch.groupValues[2].trim()
                val prosody = parseProsodyAttributes(prosodyAttrs)
                segments.add(SsmlSegment(text = prosodyText, voiceId = voiceId, prosody = prosody))
            } else if (content.isNotEmpty()) {
                segments.add(SsmlSegment(text = content, voiceId = voiceId))
            }

            lastIndex = match.range.last + 1
        }

        // 处理剩余的普通文本
        if (lastIndex < text.length) {
            val remaining = text.substring(lastIndex).trim()
            if (remaining.isNotEmpty()) {
                segments.add(SsmlSegment(text = remaining))
            }
        }

        return segments.ifEmpty { listOf(SsmlSegment(text = text.trim())) }
    }

    private fun parseProsodyAttributes(attrs: String): ProsodyConfig {
        val rate = extractAttributeValue(attrs, "rate")?.let { parseRate(it) }
        val pitch = extractAttributeValue(attrs, "pitch")?.let { parsePitch(it) }
        val volume = extractAttributeValue(attrs, "volume")?.toFloatOrNull()
        return ProsodyConfig(rate = rate, pitch = pitch, volume = volume)
    }

    private fun extractAttributeValue(attrs: String, name: String): String? {
        val regex = Regex("""$name="([^"]*)"""")
        return regex.find(attrs)?.groupValues?.get(1)
    }

    private fun parseRate(value: String): Float {
        return when (value) {
            "x-slow" -> 0.5f
            "slow" -> 0.75f
            "medium" -> 1.0f
            "fast" -> 1.25f
            "x-fast" -> 1.5f
            else -> {
                // 尝试解析百分比或倍数
                val percent = value.removeSuffix("%").toFloatOrNull()
                if (percent != null) percent / 100f
                else value.toFloatOrNull() ?: 1.0f
            }
        }
    }

    private fun parsePitch(value: String): Float {
        return when (value) {
            "x-low" -> 0.5f
            "low" -> 0.75f
            "medium" -> 1.0f
            "high" -> 1.25f
            "x-high" -> 1.5f
            else -> {
                val percent = value.removeSuffix("%").toFloatOrNull()
                if (percent != null) 1.0f + percent / 100f
                else value.toFloatOrNull() ?: 1.0f
            }
        }
    }
}
