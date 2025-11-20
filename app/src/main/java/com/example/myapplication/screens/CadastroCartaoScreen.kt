package com.example.myapplication.screens

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.myapplication.model.SessionManager // <-- Garanta que está importado
import com.example.myapplication.remote.RetrofitClient
import com.google.gson.JsonObject
import kotlinx.coroutines.launch

// Transformação para o Cartão de Crédito (XXX XXXX XXXX XXXX)
class CreditCardVisualTransformation : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val trimmed = if (text.text.length >= 16) text.text.substring(0..15) else text.text
        var out = ""
        for (i in trimmed.indices) {
            out += trimmed[i]
            if (i % 4 == 3 && i != 15) out += " "
        }

        val creditCardOffsetTranslator = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int {
                if (offset <= 3) return offset
                if (offset <= 7) return offset + 1
                if (offset <= 11) return offset + 2
                if (offset <= 16) return offset + 3
                return 19
            }

            override fun transformedToOriginal(offset: Int): Int {
                if (offset <= 4) return offset
                if (offset <= 9) return offset - 1
                if (offset <= 14) return offset - 2
                if (offset <= 19) return offset - 3
                return 16
            }
        }

        return TransformedText(AnnotatedString(out), creditCardOffsetTranslator)
    }
}

// Transformação para Data (MM/AA)
class DateVisualTransformation : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val trimmed = if (text.text.length >= 4) text.text.substring(0..3) else text.text
        var out = ""
        for (i in trimmed.indices) {
            out += trimmed[i]
            if (i == 1) out += "/"
        }

        val dateOffsetTranslator = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int {
                if (offset <= 1) return offset
                if (offset <= 4) return offset + 1
                return 5
            }

            override fun transformedToOriginal(offset: Int): Int {
                if (offset <= 2) return offset
                if (offset <= 5) return offset - 1
                return 4
            }
        }

        return TransformedText(AnnotatedString(out), dateOffsetTranslator)
    }
}

@Composable
fun CadastroCartaoScreen(navController: NavController) {
    var numeroCartao by remember { mutableStateOf("") }
    var nomeTitular by remember { mutableStateOf("") }
    var dataValidade by remember { mutableStateOf("") }
    var cvv by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Cadastre um Cartão",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "É necessário ter um método de pagamento para continuar.",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 8.dp, bottom = 24.dp)
            )

            OutlinedTextField(
                value = numeroCartao,
                onValueChange = { newValue ->
                    // Apenas filtra dígitos e limita o tamanho do DADO (não da visualização)
                    if (newValue.length <= 16) {
                        numeroCartao = newValue.filter { it.isDigit() }
                    }
                },
                label = { Text("Número do Cartão") },
                // AQUI entra a mágica visual
                visualTransformation = CreditCardVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = nomeTitular,
                onValueChange = {
                    if (it.length <= 50) {
                        nomeTitular = it
                    }
                },
                label = { Text("Nome do Titular") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text, imeAction = ImeAction.Next),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))

            Row(Modifier.fillMaxWidth()) {

                // Função auxiliar para checar se está vencido (simulação simples)
                fun isDateExpired(input: String): Boolean {
                    if (input.length != 4) return false

                    val inputMonth = input.substring(0, 2).toIntOrNull() ?: return false
                    val inputYear = input.substring(2).toIntOrNull() ?: return false

                    // Exemplo estático: Considerando que estamos em Novembro de 2025 (11/25)
                    // Em um app real, pegue a data atual do sistema via Calendar ou LocalDate
                    val currentMonth = 11
                    val currentYear = 25

                    if (inputYear < currentYear) return true
                    if (inputYear == currentYear && inputMonth < currentMonth) return true

                    return false
                }

                val hasError = isDateExpired(dataValidade)

                OutlinedTextField(
                    value = dataValidade,
                    onValueChange = { newValue ->
                        // 1. Remove tudo que não for número
                        val newDigits = newValue.filter { it.isDigit() }

                        // Validação progressiva
                        var isValidInput = true

                        // Regra A: Não pode ter mais de 4 dígitos
                        if (newDigits.length > 4) {
                            isValidInput = false
                        }

                        // Regra B: O primeiro dígito só pode ser 0 ou 1
                        // (Isso obriga o usuário a digitar "09" em vez de apenas "9")
                        if (newDigits.isNotEmpty()) {
                            val firstDigit = newDigits[0].digitToInt()
                            if (firstDigit > 1) isValidInput = false
                        }

                        // Regra C: Se já tiver 2 dígitos (o mês), deve ser entre 01 e 12
                        if (newDigits.length >= 2) {
                            val month = newDigits.substring(0, 2).toInt()
                            if (month < 1 || month > 12) isValidInput = false
                        }

                        // Só atualiza o estado se passar em todas as validações
                        if (isValidInput) {
                            dataValidade = newDigits
                        }
                    },

                    // Se tiver erro, fica vermelho
                    isError = hasError,

                    // Opcional: Texto de suporte explicando o erro
                    supportingText = {
                        if (hasError) {
                            Text("Cartão vencido", color = MaterialTheme.colorScheme.error)
                        }
                    },

                    label = { Text("Validade (MM/AA)") },
                    visualTransformation = DateVisualTransformation(), // Usando a classe que criamos antes
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next),
                    modifier = Modifier.weight(1f) // Ou ajuste conforme seu layout
                )
                Spacer(modifier = Modifier.width(16.dp))
                OutlinedTextField(
                    value = cvv,
                    onValueChange = {
                        // Limita o tamanho do CVV a 3 dígitos
                        if (it.length <= 3){
                            cvv = it
                        }
                    },
                    label = { Text("CVV") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
                    modifier = Modifier.weight(1f)
                )
            }
            Spacer(modifier = Modifier.height(32.dp))


            Button(
                onClick = {
                    if (isLoading) return@Button
                    isLoading = true

                    scope.launch {
                        // ================================================
                        // CORREÇÃO PRINCIPAL AQUI
                        // ================================================

                        // 1. Pega o token salvo na sessão
                        val token = SessionManager.getAuthToken(context)
                        if (token == null) {
                            Toast.makeText(context, "Sessão inválida. Faça login novamente.", Toast.LENGTH_LONG).show()
                            navController.navigate("login") { popUpTo(0) } // Volta tudo
                            isLoading = false
                            return@launch
                        }

                        // 2. Prepara os dados do cartão
                        val cartaoJson = JsonObject().apply {
                            addProperty("numero", numeroCartao)
                            addProperty("nome", nomeTitular)
                            addProperty("validade", dataValidade)
                            addProperty("cvv", cvv)
                        }

                        // 3. Faz a chamada na API, incluindo o token no Header
                        try {
                            // Adicionamos "Bearer $token" como primeiro argumento
                            val response = RetrofitClient.api.cadastrarCartao("Bearer $token", cartaoJson)

                            if (response.isSuccessful) {
                                Toast.makeText(context, "Cartão cadastrado com sucesso!", Toast.LENGTH_SHORT).show()
                                navController.navigate("home") {
                                    // Limpa a navegação para o usuário não voltar para esta tela
                                    popUpTo(navController.graph.startDestinationId) { inclusive = true }
                                }
                            } else {
                                // O erro "Credenciais inválidas" cairá aqui
                                val errorMsg = response.errorBody()?.string() ?: "Erro ao cadastrar cartão."
                                Log.e("CADASTRO_CARTAO_ERRO", "Código: ${response.code()} | Mensagem: $errorMsg")
                                Toast.makeText(context, "Erro: $errorMsg", Toast.LENGTH_LONG).show()
                            }
                        } catch (e: Exception) {
                            Log.e("CADASTRO_CARTAO_EXCECAO", "Exceção: ${e.message}")
                            Toast.makeText(context, "Falha na conexão com o servidor.", Toast.LENGTH_LONG).show()
                        } finally {
                            isLoading = false
                        }
                        // ================================================
                    }
                },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                enabled = !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
                } else {
                    Text("Salvar Cartão e Continuar")
                }
            }
        }
    }
}
