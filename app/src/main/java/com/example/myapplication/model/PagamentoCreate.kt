package com.example.myapplication.model

import com.google.gson.annotations.SerializedName

/**
 * Representa o objeto de dados enviado para a API
 * ao criar um novo pagamento.
 */
data class PagamentoCreate(
    @SerializedName("horario_entrada")
    val horarioEntrada: String, // Usar String para datas ISO (ex: "2025-11-02T18:30:00")

    @SerializedName("horario_saida")
    val horarioSaida: String,

    @SerializedName("valor_pago")
    val valorPago: Double,

    @SerializedName("numero_cartao")
    val numeroCartao: String
)