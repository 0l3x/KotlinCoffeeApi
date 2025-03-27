package edu.olexandergalaktionov.apirestcoffee.data

import edu.olexandergalaktionov.apirestcoffee.model.CoffeeComments
import edu.olexandergalaktionov.apirestcoffee.model.CoffeeId
import edu.olexandergalaktionov.apirestcoffee.model.CoffeeList
import edu.olexandergalaktionov.apirestcoffee.model.LoginRequest
import edu.olexandergalaktionov.apirestcoffee.model.LoginResponse
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.Path

class Retrofit2Api {
    companion object {
        /**
         * Base URL of the API.
         */
        const val BASE_URL = "https://www.javiercarrasco.es/api/coffee/"

        /**
         * Creates and returns an instance of Retrofit2ApiInterface to interact with the API.
         *
         * @return Retrofit2ApiInterface instance for making API calls.
         */
        fun getRetrofit2Api(): Retrofit2ApiInterface {
            return Retrofit.Builder().baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create()).build()
                .create(Retrofit2ApiInterface::class.java)
        }
    }
}

interface Retrofit2ApiInterface {
    @POST("login")
    @Headers("Content-Type: application/json")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>

    @GET("coffee")
    suspend fun getAll(@Header("Authorization") token: String): List<CoffeeList>

    @GET("coffee/{id}")
    suspend fun getCoffeeById(@Header("Authorization") token: String, @Path("id") id: Int): CoffeeId

    @GET("comments/{idCoffee}")
    suspend fun getComments(@Header("Authorization") token: String, @Path("idCoffee")idCoffee: Int): List<CoffeeComments>

    @POST("comments")
    @Headers("Content-Type: application/json")
    suspend fun postComment(@Header("Authorization") token: String, @Body comment: CoffeeComments): Response<CoffeeComments>
}