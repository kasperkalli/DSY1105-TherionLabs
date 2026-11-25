package cl.duoc.therionlabs.network

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {

    // Backend Spring Boot desde el emulador Android
    private const val BASE_URL = "http://192.168.1.5:8081/"

    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    // API para sincronizar prospectos
    val prospectoApi: ProspectoApi by lazy {
        retrofit.create(ProspectoApi::class.java)
    }

    // API para consultar y crear eventos
    val eventoApi: EventoApi by lazy {
        retrofit.create(EventoApi::class.java)
    }


    // API para unir captadores a eventos
    val participacionApi: ParticipacionApi by lazy {
        retrofit.create(ParticipacionApi::class.java)
    }


    // API para verificar la identidad del dispositivo
    val dispositivoApi: DispositivoApi by lazy {
        retrofit.create(DispositivoApi::class.java)
    }

    // API para descargar reportes Excel administrativos
    val excelApi: ExcelApi by lazy {
        retrofit.create(ExcelApi::class.java)
    }

    // API para registrar captadores y generar sus tokens
    val captadorAdminApi: CaptadorAdminApi by lazy {
        retrofit.create(CaptadorAdminApi::class.java)
    }

    // Estadísticas administrativas por evento
    val estadisticasAdminApi: EstadisticasAdminApi by lazy {
        retrofit.create(EstadisticasAdminApi::class.java)
    }
}
