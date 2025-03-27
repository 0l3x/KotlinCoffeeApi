package edu.olexandergalaktionov.apirestcoffee.data

import android.util.Log
import edu.olexandergalaktionov.apirestcoffee.model.LoginRequest
import edu.olexandergalaktionov.apirestcoffee.model.LoginResponse

/**
 * Class RemoteDataSource.kt
 * Handles remote API calls for login and coffee retrieval.
 *
 * This object acts as a static data source that communicates directly with the Retrofit API.
 *
 * @author Olexandr Galaktionov Tsisar
 */
class RemoteDataSource {
    companion object {
        val TAG = RemoteDataSource::class.java.simpleName

        private val api = Retrofit2Api.getRetrofit2Api()

        /**
         * Retrieves the list of all coffees from the API.
         *
         * @param token Bearer token for authentication.
         * @return List of coffee objects.
         */
        suspend fun getCoffee(token: String) = api.getAll("Bearer $token")

        /**
         * Performs a login operation using the provided credentials.
         *
         * @param request LoginRequest containing user and password.
         * @return LoginResponse containing the token if successful.
         * @throws Exception when the response is not successful or body is null.
         */
        suspend fun login(request: LoginRequest): LoginResponse {
            val response = api.login(request)
            if (response.isSuccessful) {
                return response.body() ?: throw Exception("Respuesta vacía del servidor")
            } else {
                val errorBody = response.errorBody()?.string() // Error detallado
                Log.e(TAG, "Error: ${response.message()} | $errorBody")
                throw Exception("Error en login: ${response.message()}")
            }
        }
    }
}