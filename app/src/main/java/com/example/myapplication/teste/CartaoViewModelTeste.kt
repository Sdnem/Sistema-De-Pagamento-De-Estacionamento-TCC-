package com.example.myapplication.teste

import androidx.compose.runtime.State
import androidx.lifecycle.ViewModel
import com.example.myapplication.model.Cartao
import kotlinx.coroutines.flow.SharedFlow

abstract class CartaoViewModelTeste : ViewModel() {
    abstract val isLoading: State<Boolean>
    abstract val errorMessage: State<String?>
    abstract val cadastroEvent: SharedFlow<Unit>
    abstract fun addCartao(cartao: Cartao)
}