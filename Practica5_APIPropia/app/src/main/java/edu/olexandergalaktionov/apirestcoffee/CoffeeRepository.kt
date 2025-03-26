package edu.olexandergalaktionov.apirestcoffee

import edu.olexandergalaktionov.apirestcoffee.data.RemoteDataSource
import edu.olexandergalaktionov.apirestcoffee.data.Retrofit2Api
import edu.olexandergalaktionov.apirestcoffee.model.CoffeeList
import edu.olexandergalaktionov.apirestcoffee.model.LoginRequest
import edu.olexandergalaktionov.apirestcoffee.model.LoginResponse
import edu.olexandergalaktionov.apirestcoffee.utils.SessionManager
import kotlinx.coroutines.flow.Flow

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
        return remoteDataSource.getCoffee("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c3VhcmlvIjoib2dhbGFrdGlvbm92IiwiaWF0IjoxNzQyNDg0Mjc2LCJleHAiOjE3NDI0OTE0NzZ9.uTCJou3JyB5rlgfiQE4v9VbBdaLNGwnXbZe0spGlvU0")
    }
}