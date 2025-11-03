package com.example.myapplication.model

import com.google.gson.annotations.SerializedName
import java.util.Date // Ou String, dependendo do formato que sua API envia

data class Pagamento(
    @SerializedName("id")
    val id: Int,

    @SerializedName("horario_entrada")
    val horarioEntrada: Date,

    @SerializedName("horario_saida")
    val horarioSaida: Date,

    @SerializedName("valor_pago")
    val valorPago: Double,

    @SerializedName("numero_cartao")
    val numeroCartao: String,

    @SerializedName("usuario_id")
    val usuarioId: Int
)