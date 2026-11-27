package com.example.reservaescuela.data

import androidx.annotation.Keep
import okhttp3.Credentials
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*
import java.net.URI
import java.util.concurrent.TimeUnit

@Keep
data class SolicitudNube(val id: String, val espacio: Int, val fecha: String, val inicio: String,
    val fin: String, val personas: Int, val actividad: String)
@Keep
data class TurnoNube(val id: String, val usuario: String, val espacio: Int, val fecha: String,
    val inicio: String, val fin: String, val personas: Int, val actividad: String, val estado: String)
@Keep
data class DecisionNube(val estado: String)

interface AgendaApi {
    @GET("api/reservas") suspend fun listar(): List<TurnoNube>
    @POST("api/reservas") suspend fun crear(@Body solicitud: SolicitudNube)
    @PUT("api/reservas/{id}/decision") suspend fun decidir(@Path("id") id: String, @Body decision: DecisionNube)
}

fun agendaApi(direccion: String, usuario: String, clave: String): AgendaApi {
    val url = URI.create(direccion.trim().trimEnd('/'))
    require(url.scheme == "https" && !url.host.isNullOrBlank() && url.userInfo == null &&
        url.query == null && url.fragment == null && url.path.isNullOrEmpty())
    val autorizacion = Credentials.basic(usuario, clave, Charsets.UTF_8)
    val cliente = OkHttpClient.Builder().readTimeout(90, TimeUnit.SECONDS)
        .followRedirects(false).followSslRedirects(false)
        .addInterceptor { chain ->
            chain.proceed(chain.request().newBuilder().header("Authorization", autorizacion).build())
        }.build()
    return Retrofit.Builder().baseUrl("$url/").client(cliente)
        .addConverterFactory(GsonConverterFactory.create()).build().create(AgendaApi::class.java)
}
