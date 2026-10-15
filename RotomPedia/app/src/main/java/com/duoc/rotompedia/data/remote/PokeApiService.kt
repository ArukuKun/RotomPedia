package com.duoc.rotompedia.data.remote

import com.duoc.rotompedia.data.model.EvolutionChainResponse
import com.duoc.rotompedia.data.model.PokemonData
import com.duoc.rotompedia.data.model.PokemonListResponse
import com.duoc.rotompedia.data.model.PokemonSpeciesResponse
import com.duoc.rotompedia.data.model.TypeDetailResponse
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query
import retrofit2.http.Url
//Url de la base POKEAPI
private const val BASE_URL = "https://pokeapi.co/api/v2/"


//Interfaz retrofit
interface PokeApiService {
    @GET("pokemon/{id}")
    suspend fun getPokemon(
        @Path("id") pokemonId: Int
    ): PokemonData

    // Pide una lista de Pokémon. 'limit' es cuántos traer, 'offset' es desde dónde empezar.
    @GET("pokemon-species")
    suspend fun getPokemonList(
        @Query("limit") limit: Int,
        @Query("offset") offset: Int
    ): PokemonListResponse

    // Obtiene los datos de la "especie" del Pokémon
    @GET("pokemon-species/{id}")
    suspend fun getPokemonSpecies(
        @Path("id") pokemonId: Int
    ): PokemonSpeciesResponse

    // Obtiene la cadena de evolución desde una URL dinámica
    @GET
    suspend fun getEvolutionChain(
        @Url url: String
    ): EvolutionChainResponse
    // Obtiene los detalles de un tipo específico por su nombre
    @GET("type/{name}")
    suspend fun getTypeDetails(
        @Path("name") name: String
    ): TypeDetailResponse
}

//Objeto singleton que crea y mantiene una unica instancia
object PokemonApi{
    //1 inicializa el moshi
    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    //2 inicializa la instancia usando la url base y el moshi como translate
    private val retrofit = Retrofit.Builder()
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .baseUrl(BASE_URL)
        .build()

    //3 crea la instancia del servgicio que usara la app
    val service: PokeApiService by lazy {
        retrofit.create(PokeApiService::class.java)
    }
}
