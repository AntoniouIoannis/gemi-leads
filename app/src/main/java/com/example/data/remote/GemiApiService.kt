package com.example.data.remote

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

@JsonClass(generateAdapter = true)
data class GemiApiResponse(
    @Json(name = "success") val success: Boolean? = true,
    @Json(name = "data") val data: List<GemiRemoteCompany>? = null,
    @Json(name = "items") val items: List<GemiRemoteCompany>? = null,
    @Json(name = "total") val total: Int? = 0,
    @Json(name = "message") val message: String? = null
)

@JsonClass(generateAdapter = true)
data class GemiRemoteCompany(
    @Json(name = "gemi_number") val gemiNumber: String?,
    @Json(name = "afm") val afm: String?,
    @Json(name = "name") val companyName: String?,
    @Json(name = "trade_name") val tradeName: String? = null,
    @Json(name = "legal_form") val legalForm: String?,
    @Json(name = "kad") val primaryKad: String?,
    @Json(name = "kad_description") val kadDescription: String?,
    @Json(name = "registration_date") val registrationDate: String?,
    @Json(name = "region") val region: String?,
    @Json(name = "prefecture") val prefecture: String? = null,
    @Json(name = "municipality") val municipality: String? = null,
    @Json(name = "address") val address: String? = null,
    @Json(name = "postal_code") val postalCode: String? = null,
    @Json(name = "chamber") val chamber: String? = null,
    @Json(name = "phone") val phone: String? = null,
    @Json(name = "email") val email: String? = null
)

interface GemiApiService {
    @GET("api/v1/companies/new")
    suspend fun getRecentCompanies(
        @Query("date_from") dateFrom: String? = null,
        @Query("date_to") dateTo: String? = null,
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 50
    ): GemiApiResponse

    @GET("api/v1/companies/search")
    suspend fun searchCompanies(
        @Query("q") query: String,
        @Query("kad") kad: String? = null,
        @Query("region") region: String? = null
    ): GemiApiResponse

    companion object {
        const val GEMI_DEFAULT_API_KEY = "VeWz15eTqbFrqaazLUYUfyUVMr1w6Zfa"
        const val GEMI_BASE_URL = "https://opendata.uhc.gr/"

        fun create(apiKey: String = GEMI_DEFAULT_API_KEY): GemiApiService {
            val authInterceptor = Interceptor { chain ->
                val original = chain.request()
                val requestBuilder = original.newBuilder()
                    .header("X-API-KEY", apiKey)
                    .header("Authorization", "Bearer $apiKey")
                    .header("Accept", "application/json")
                    .header("User-Agent", "GEMI-B2B-LeadGenerator-Android/1.0")
                chain.proceed(requestBuilder.build())
            }

            val client = OkHttpClient.Builder()
                .addInterceptor(authInterceptor)
                .connectTimeout(15, TimeUnit.SECONDS)
                .readTimeout(15, TimeUnit.SECONDS)
                .build()

            val moshi = com.squareup.moshi.Moshi.Builder()
                .add(com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory())
                .build()

            val retrofit = Retrofit.Builder()
                .baseUrl(GEMI_BASE_URL)
                .client(client)
                .addConverterFactory(MoshiConverterFactory.create(moshi))
                .build()

            return retrofit.create(GemiApiService::class.java)
        }
    }
}
