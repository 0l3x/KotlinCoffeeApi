package edu.olexandergalaktionov.apirestcoffee.data

import android.util.Log
import edu.olexandergalaktionov.apirestcoffee.model.CoffeeList
import edu.olexandergalaktionov.apirestcoffee.model.LoginRequest
import edu.olexandergalaktionov.apirestcoffee.model.LoginResponse
import edu.olexandergalaktionov.apirestcoffee.utils.SessionManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

/**
 * Class CoffeeRepository.kt
 * Repository to handle authentication and coffee data retrieval operations.
 *
 * @author Olexandr Galaktionov Tsisar
 *
 * @param sessionManager Manager to handle session storage and retrieval.
 */
class CoffeeRepository(private val sessionManager: SessionManager){
    val TAG = CoffeeRepository::class.java.simpleName
    private val remoteDataSource = RemoteDataSource

    /**
     * Authenticates a user by sending login data to the API.
     *
     * @param request Login request data (username and password).
     * @return LoginResponse containing the token.
     */
    suspend fun login(request: LoginRequest): LoginResponse {
        val response = RemoteDataSource.login(request)
        sessionManager.saveSession(response.token!!, request.user) // Se guarda la sesión.
        return response
    }

    /**
     * Returns a Flow that emits the current session token and username.
     *
     * @return Flow<Pair<String?, String?>> session data.
     */
    fun getSessionFlow(): Flow<Pair<String?, String?>> = sessionManager.sessionFlow

    /**
     * Clears the current user session.
     */
    suspend fun logout() {
        sessionManager.clearSession()
    }

    /**
     * Retrieves the list of coffees from the remote API using the stored token.
     *
     * @throws Exception if token is not available.
     * @return List of CoffeeList objects.
     */
    suspend fun getAllCoffees(): List<CoffeeList> {
        val token = sessionManager.sessionFlow.first().first
            ?: throw Exception("Token no disponible")
        Log.d(TAG, "Token usado: $token")
        return remoteDataSource.getCoffee(token)
    }
}