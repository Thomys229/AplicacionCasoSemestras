package com.example.reservaescuela.data

import com.google.gson.annotations.SerializedName
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*

data class Material(
    @SerializedName("id") val id: Long,
    @SerializedName("nombre") val nombre: String,
    @SerializedName("categoria") val categoria: String
)

data class Entrada(
    @SerializedName("nombre") val nombre: String,
    @SerializedName("categoria") val categoria: String
)

data class Feriado(
    @SerializedName("date") val fecha: String,
    @SerializedName("localName") val nombre: String
)

interface CatalogoApi {
    @GET("categorias") suspend fun listar(): List<String>
}

interface MaterialesApi {
    @GET("materiales") suspend fun listar(): List<Material>
    @POST("materiales") suspend fun agregar(@Body entrada: Entrada)
    @PUT("materiales/{id}") suspend fun editar(@Path("id") id: Long, @Body entrada: Entrada)
    @DELETE("materiales/{id}") suspend fun eliminar(@Path("id") id: Long)
}

interface FeriadosApi {
    @GET("api/v3/PublicHolidays/{anio}/CL") suspend fun listar(@Path("anio") anio: Int): List<Feriado>
}

object RetrofitService {
    private fun cliente(url: String) = Retrofit.Builder().baseUrl(url)
        .addConverterFactory(GsonConverterFactory.create()).build()

    // ADB conecta estos puertos con el equipo de la defensa.
    val catalogo = cliente("http://localhost:8081/").create(CatalogoApi::class.java)
    val materiales = cliente("http://localhost:8082/").create(MaterialesApi::class.java)
    val feriados = cliente("https://date.nager.at/").create(FeriadosApi::class.java)
}
