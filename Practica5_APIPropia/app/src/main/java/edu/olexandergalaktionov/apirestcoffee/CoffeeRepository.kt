package edu.olexandergalaktionov.apirestcoffee

import android.util.Log
import edu.olexandergalaktionov.apirestcoffee.data.RemoteDataSource
import edu.olexandergalaktionov.apirestcoffee.data.Retrofit2Api
import edu.olexandergalaktionov.apirestcoffee.model.CoffeeList
import edu.olexandergalaktionov.apirestcoffee.model.LoginRequest
import edu.olexandergalaktionov.apirestcoffee.model.LoginResponse
import edu.olexandergalaktionov.apirestcoffee.utils.SessionManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

class CoffeeRepository(private val sessionManager: SessionManager){
    private val apiService = Retrofit2Api.getRetrofit2Api()
    val TAG = CoffeeRepository::class.java.simpleName
    private val remoteDataSource = RemoteDataSource

    // Función para obtener el login.
    suspend fun login(request: LoginRequest): LoginResponse {
        val response = RemoteDataSource.login(request)
        sessionManager.saveSession(response.token!!, request.user) // Se guarda la sesión.
        return response
    }

    fun getSessionFlow(): Flow<Pair<String?, String?>> = sessionManager.sessionFlow

     suspend fun logout() {
         sessionManager.clearSession()
        }

    /**
     * Obtiene la lista de cafés desde la API.
     */
    suspend fun getAllCoffees(): List<CoffeeList> {
        val token = sessionManager.sessionFlow.first().first
            ?: throw Exception("Token no disponible")
        Log.d(TAG, "Token usado: $token")
        return remoteDataSource.getCoffee(token)
    }

}