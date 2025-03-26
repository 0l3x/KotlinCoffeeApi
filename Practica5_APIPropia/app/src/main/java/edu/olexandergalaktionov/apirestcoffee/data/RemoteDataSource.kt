package edu.olexandergalaktionov.apirestcoffee.data

import android.util.Log
import edu.olexandergalaktionov.apirestcoffee.model.LoginRequest
import edu.olexandergalaktionov.apirestcoffee.model.LoginResponse
import kotlinx.coroutines.flow.flow

class RemoteDataSource {
    companion object {
        private const val TAG = "RemoteDataSource"

        private val api = Retrofit2Api.getRetrofit2Api()

        suspend fun getCoffee(token: String) = api.getAll("Bearer $token")

        // Función para obtener el login, se pasa el objeto RequestLogin en el body.
        // Se devuelve un objeto LoginResponse.
        suspend fun login(request: LoginRequest): LoginResponse {
            val response = api.login(request)
            if (response.isSuccessful) {
                return response.body()?: throw Exception("Respuesta vacía del servidor")
            } else {
                val errorBody = response.errorBody()?.string() // Detalles del error.
                Log.e(TAG, "Error: ${response.message()} | $errorBody")
                throw Exception("Error en login: ${response.message()}")
            }
        }
    }
}