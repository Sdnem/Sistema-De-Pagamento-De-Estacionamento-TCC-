import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.myapplication.model.Cartao
import com.example.myapplication.remote.RetrofitClient

@Composable
fun TelaCartoes(navController: NavController, userId: Int) {
    var cartoes by remember { mutableStateOf<List<Cartao>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        RetrofitClient.instance.getCartoes(userId).enqueue(object : retrofit2.Callback<List<Cartao>> {
            override fun onResponse(call: retrofit2.Call<List<Cartao>>, response: retrofit2.Response<List<Cartao>>) {
                if (response.isSuccessful) {
                    cartoes = response.body() ?: emptyList()
                }
                isLoading = false
            }

            override fun onFailure(call: retrofit2.Call<List<Cartao>>, t: Throwable) {
                isLoading = false
            }
        })
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text("Meus Cartões", style = MaterialTheme.typography.headlineMedium)

        Spacer(modifier = Modifier.height(16.dp))

        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(modifier = Modifier.weight(1f)) {
                itemsIndexed(cartoes) { index, cartao ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        elevation = CardDefaults.cardElevation(4.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Nome: ${cartao.nome}")
                            Text("Número: ${cartao.numero}")
                            Text("Banco: ${cartao.banco}")

                            Button(
                                onClick = {
                                    // Chama a API para remover o cartão
                                    RetrofitClient.instance.deleteCartao(1).enqueue(object : retrofit2.Callback<Void> {
                                        override fun onResponse(call: retrofit2.Call<Void>, response: retrofit2.Response<Void>) {
                                            if (response.isSuccessful) {
                                                cartoes = cartoes.toMutableList().apply { removeAt(index) }
                                            }
                                        }
                                        override fun onFailure(call: retrofit2.Call<Void>, t: Throwable) {}
                                    })
                                },
                                modifier = Modifier.align(Alignment.End)
                            ) {
                                Text("Remover")
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    // Aqui você poderia navegar para uma tela de cadastro de cartão
                    navController.navigate("addCartao")
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Adicionar Cartão")
            }
        }
    }
}
@Preview(showBackground = true)
@Composable
fun TelaCartoesPreview() {
    TelaCartoes(navController = rememberNavController(), userId = 1)
}