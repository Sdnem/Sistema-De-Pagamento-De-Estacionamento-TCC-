package com.example.myapplication

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.model.Carro
import com.example.myapplication.model.SessionManager
import com.example.myapplication.remote.RetrofitClient
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException

// 1. Adicionamos o SessionManager como dependência, igual ao CartaoViewModel
class CarManagementViewModel(private val sessionManager: SessionManager) : ViewModel() {

    private val _carros = MutableStateFlow<List<Carro>>(emptyList())
    val carros: StateFlow<List<Carro>> = _carros

    // 2. Estados para controlar o carregamento e mensagens de erro
    val isLoading = mutableStateOf(false)
    val errorMessage = mutableStateOf<String?>(null)

    // 3. Flow para eventos de navegação (ex: após cadastrar com sucesso)
    private val _cadastroEvent = MutableSharedFlow<Unit>()
    val cadastroEvent = _cadastroEvent.asSharedFlow()

    init {
        // 4. Busca os carros do usuário assim que o ViewModel é criado
        buscarCarrosDoUsuario()
    }

    fun buscarCarrosDoUsuario() {
        val token = sessionManager.fetchAuthToken()
        if (token == null) {
            errorMessage.value = "Usuário não autenticado."
            return
        }

        isLoading.value = true
        errorMessage.value = null

        RetrofitClient.getInstanceCarro(token).getCarro().enqueue(object : Callback<List<Carro>> {
            override fun onResponse(call: Call<List<Carro>>, response: Response<List<Carro>>) {
                if (response.isSuccessful) {
                    _carros.value = response.body() ?: emptyList()
                } else {
                    errorMessage.value = "Erro ao buscar os carros."
                }
                isLoading.value = false
            }

            override fun onFailure(call: Call<List<Carro>>, t: Throwable) {
                errorMessage.value = "Falha na conexão: ${t.message}"
                isLoading.value = false
            }
        })
    }

    fun addCarro(novoCarro: Carro) {
        val token = sessionManager.fetchAuthToken()
        if (token == null) {
            errorMessage.value = "Sessão expirada. Faça login novamente."
            return
        }

        // 5. Usando viewModelScope.launch para a chamada de rede assíncrona
        viewModelScope.launch {
            // Verifica o limite de 3 carros antes de fazer a chamada de rede
            if (_carros.value.size >= 3) {
                errorMessage.value = "Limite de 3 carros atingido."
                return@launch
            }

            isLoading.value = true
            errorMessage.value = null

            try {
                val carroAdicionado = RetrofitClient.getInstanceCarro(token).addCarro(novoCarro)

                // Atualiza a lista local com o carro retornado pela API
                val listaAtual = _carros.value.toMutableList()
                listaAtual.add(carroAdicionado)
                _carros.value = listaAtual

                // Emite o evento para notificar a UI que o cadastro foi um sucesso
                _cadastroEvent.emit(Unit)

            } catch (e: HttpException) {
                // Trata erros de resposta do servidor (ex: 404, 500)
                errorMessage.value = "Erro ao adicionar o carro."
            } catch (e: IOException) {
                // Trata erros de conexão (sem internet, timeout)
                errorMessage.value = "Falha na conexão: ${e.message}"
            } finally {
                // Garante que o indicador de loading seja desativado ao final
                isLoading.value = false
            }
        }
    }

    // Função de exemplo para adicionar um carro novo
    fun addExampleCar() {
        val maxId = _carros.value.maxByOrNull { it.id ?: 0 }?.id ?: 0
        val newId = maxId + 1
        val newCar = Carro(
            id = newId,
            marca = "Hyundai",
            modelo = "HB20",
            ano = 2024,
            placa = "XYZ-9H87",
            cor = "Branco",
            imageUrl = "https://i.imgur.com/k2Hqs48.png",
            userId = 1
        )
        addCarro(newCar)
    }

    fun deletarCarro(carroId: Int) {
        val token = sessionManager.fetchAuthToken()
        if (token == null) {
            errorMessage.value = "Sessão expirada. Faça login novamente."
            return
        }

        RetrofitClient.getInstanceCarro(token).deleteCarro(carroId).enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.isSuccessful) {
                    // Remove o carro da lista local se a chamada for bem-sucedida
                    val listaAtualizada = _carros.value.toMutableList()
                    listaAtualizada.removeAll { it.id == carroId }
                    _carros.value = listaAtualizada
                } else {
                    errorMessage.value = "Não foi possível remover o carro."
                }
            }

            override fun onFailure(call: Call<Void>, t: Throwable) {
                errorMessage.value = "Falha na conexão: ${t.message}"
            }
        })
    }
}