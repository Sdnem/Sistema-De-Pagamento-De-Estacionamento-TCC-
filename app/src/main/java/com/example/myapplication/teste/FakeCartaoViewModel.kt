package com.example.myapplication.teste

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import com.example.myapplication.CartaoViewModel
import com.example.myapplication.teste.CartaoViewModelTeste
import com.example.myapplication.model.Cartao
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow

class FakeCartaoViewModel(
    initialIsLoading: Boolean = false,
    initialErrorMessage: String? = null
) : CartaoViewModelTeste() { // Herda da ViewModel real

    // 1. Sobrescreve o estado de 'isLoading'
    // Usamos mutableStateOf para poder definir um valor inicial para a preview.
    override val isLoading: State<Boolean> = mutableStateOf(initialIsLoading)

    // 2. Sobrescreve o estado de 'errorMessage'
    override val errorMessage: State<String?> = mutableStateOf(initialErrorMessage)

    // 3. Sobrescreve o canal de eventos
    // Fornecemos uma implementação vazia que não fará nada na preview.
    override val cadastroEvent: SharedFlow<Unit> = MutableSharedFlow()

    // 4. Sobrescreve a função de adicionar cartão
    // A implementação é vazia, pois não precisamos salvar nada na preview.
    override fun addCartao(cartao: Cartao) {
        // Nenhuma ação é necessária na preview.
        println("Preview: Tentativa de adicionar cartão: $cartao")
    }
}