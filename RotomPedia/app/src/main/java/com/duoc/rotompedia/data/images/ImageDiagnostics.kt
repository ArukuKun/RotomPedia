package com.duoc.rotompedia.data.images

data class ImageFailure(val pokemonId: Int, val shiny: Boolean, val source: PokemonImageSource,
    val url: String, val httpStatus: Int?, val reason: String)

object ImageDiagnostics {
    private val failures = java.util.concurrent.ConcurrentHashMap<String, ImageFailure>()
    fun record(request: PokemonImageRequest, candidate: PokemonImageCandidate, status: Int?, error: Throwable) {
        val record = ImageFailure(request.pokemonId, request.shiny, candidate.source, candidate.url, status,
            "${error.javaClass.simpleName}: ${error.message.orEmpty()}")
        if (failures.put(candidate.url, record) != record) android.util.Log.w("RotomPedia_Sprites", record.toString())
    }
    fun snapshot(): List<ImageFailure> = failures.values.sortedWith(compareBy<ImageFailure> { it.pokemonId }.thenBy { it.url })
}
