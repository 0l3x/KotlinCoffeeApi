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

/**
 * Class Retrofit2Api.kt
 *
 * Retrofit builder to interact with the Coffee REST API.
 *
 * @author Olexandr Galaktionov Tsisar
 */
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

/**
 * Interface Retrofit2ApiInterface
 *
 * Retrofit interface to define endpoints of the Coffee API.
 *
 * @author Olexandr Galaktionov Tsisar
 */
interface Retrofit2ApiInterface {
    /**
     * Sends a POST request to login a user.
     *
     * @param request LoginRequest object containing user credentials.
     * @return Response containing LoginResponse data if successful.
     */
    @POST("login")
    @Headers("Content-Type: application/json")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>

    /**
     * Retrieves the full list of coffees.
     *
     * @param token Authentication token in the "Bearer" format.
     * @return List of CoffeeList objects.
     */
    @GET("coffee")
    suspend fun getAll(@Header("Authorization") token: String): List<CoffeeList>

    /**
     * Retrieves the details of a specific coffee by ID.
     *
     * @param token Authentication token in the "Bearer" format.
     * @param id ID of the coffee to retrieve.
     * @return CoffeeId object containing the coffee details.
     */
    @GET("coffee/{id}")
    suspend fun getCoffeeById(@Header("Authorization") token: String, @Path("id") id: Int): CoffeeId

    /**
     * Retrieves the list of comments for a specific coffee.
     *
     * @param token Authentication token in the "Bearer" format.
     * @param idCoffee ID of the coffee to get comments for.
     * @return List of CoffeeComments objects.
     */
    @GET("comments/{idCoffee}")
    suspend fun getComments(@Header("Authorization") token: String, @Path("idCoffee")idCoffee: Int): List<CoffeeComments>

    /**
     * Posts a new comment for a coffee.
     *
     * @param token Authentication token in the "Bearer" format.
     * @param comment CoffeeComments object to be posted.
     * @return Response containing the posted CoffeeComments.
     */
    @POST("comments")
    @Headers("Content-Type: application/json")
    suspend fun postComment(@Header("Authorization") token: String, @Body comment: CoffeeComments): Response<CoffeeComments>
}