package br.com.fiap.mobile.lumenapp

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import br.com.fiap.mobile.lumenapp.models.Painel
import br.com.fiap.mobile.lumenapp.ui.theme.LoginAppTheme
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.delay
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import java.io.IOException

class MainActivity : ComponentActivity() {
    val client = OkHttpClient()
    val gson = Gson();

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            LoginAppTheme {
                val navController = rememberNavController()
                NavHost(navController, startDestination = "login") {
                    composable("login") { LoginScreen(navController) }
                    composable("home") { HomeScreen(navController) }
                    composable("cadastro_painel") { CadastroPainelScreen(navController) }
                    composable("listagem_paineis") { ListagemPaineisScreen(navController) }
                    composable(
                        route = "editar/{id}",
                        arguments = listOf(navArgument("id") { type = NavType.IntType })
                    ) { backStackEntry ->
                        val painelId = backStackEntry.arguments?.getInt("id") ?: return@composable
                        EditarPainelScreen(painelId, navController)
                    }
                }
            }
        }
    }

    @Composable
    fun LoginScreen(navController: NavController) {
        var username by remember { mutableStateOf(TextFieldValue()) }
        var password by remember { mutableStateOf(TextFieldValue()) }
        var errorMessage by remember { mutableStateOf("") }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(text = "Login", style = MaterialTheme.typography.headlineMedium)

            Spacer(modifier = Modifier.height(16.dp))

            BasicTextField(
                value = username,
                onValueChange = { username = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .border(1.dp, Color.Gray),
                decorationBox = { innerTextField ->
                    if (username.text.isEmpty()) {
                        Text(text = "Username", color = Color.Gray)
                    }
                    innerTextField()
                }
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Campo de Senha
            BasicTextField(
                value = password,
                onValueChange = { password = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .border(1.dp, Color.Gray),
                decorationBox = { innerTextField ->
                    if (password.text.isEmpty()) {
                        Text(text = "Password", color = Color.Gray)
                    }
                    innerTextField()
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (errorMessage.isNotEmpty()) {
                Text(text = errorMessage, color = Color.Red)
                Spacer(modifier = Modifier.height(8.dp))
            }

            Button(
                onClick = {
                    if (username.text == "admin" && password.text == "1234") {
                        navController.navigate("home")
                    } else {
                        errorMessage = "Invalid credentials"
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Login")
            }
        }
    }

    @Composable
    fun HomeScreen(navController: NavController) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Título
            Text(
                text = "Bem vindo ao Lumen!",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold
                ),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 32.dp)
            )
            Text(
                text = "Seu aplicativo de monitoramento e controle de sua produção de energia solar!",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontSize = 24.sp
                ), modifier = Modifier.padding(bottom = 32.dp)
            )

            Button(
                onClick = { navController.navigate("cadastro_painel") },
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
            ) {
                Text(text = "Cadastrar Painel")
            }

            Button(
                onClick = { navController.navigate("listagem_paineis") },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = "Listar Painéis")
            }
        }

    }

    @Composable
    fun CadastroPainelScreen(navController: NavController) {
        var nome by remember { mutableStateOf("") }
        var producaoMedia by remember { mutableStateOf("") }
        var isSuccess by remember { mutableStateOf(false) }
        var errorMessage by remember { mutableStateOf("") }

        // Função para realizar o cadastro
        fun handleCadastro() {
            if (nome.isNotEmpty() && producaoMedia.isNotEmpty()) {
                val painel = Painel(id = 0, nome = nome, producaoMedia = producaoMedia.toDouble())
                val urlCadastro = "https://ecosynergy-api.azurewebsites.net/api/Painel"

                cadastrarPainel(
                    painel = painel,
                    url = urlCadastro,
                    onSuccess = {
                        isSuccess = true
                        runOnUiThread {
                            Handler(Looper.getMainLooper()).postDelayed({
                                navController.popBackStack()
                            }, 2000)
                        }
                    },
                    onError = { message ->
                        errorMessage = message
                    }
                )
            } else {
                errorMessage = "Todos os campos devem ser preenchidos!"
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text("Cadastrar Painel", style = MaterialTheme.typography.headlineMedium)

            Spacer(modifier = Modifier.height(16.dp))

            TextField(
                value = nome,
                onValueChange = { nome = it },
                label = { Text("Nome do Painel") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            TextField(
                value = producaoMedia,
                onValueChange = { producaoMedia = it },
                label = { Text("Produção Média (kWh)") },
                keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = { handleCadastro() },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Cadastrar")
            }

            if (isSuccess) {
                Text("Cadastro realizado com sucesso!", color = Color.Green, modifier = Modifier.padding(top = 16.dp))
            }

            if (errorMessage.isNotEmpty()) {
                Text("Erro: $errorMessage", color = Color.Red, modifier = Modifier.padding(top = 16.dp))
            }
        }
    }


    @Composable
    fun ListagemPaineisScreen(navController: NavController) {
        var paineis by remember { mutableStateOf<List<Painel>>(emptyList()) }
        var errorMessage by remember { mutableStateOf("") }

        fun carregarPaineis() {
            val urlListagem = "https://ecosynergy-api.azurewebsites.net/api/Painel"
            listarPaineis(urlListagem, onSuccess = { painels ->
                paineis = painels // Atualizando o estado com os dados recebidos
            }, onError = { message ->
                errorMessage = message // Atualizando o estado com a mensagem de erro
            })
        }

        LaunchedEffect(true) {
            carregarPaineis()
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            Text("Listagem de Painéis", style = MaterialTheme.typography.headlineMedium)

            Spacer(modifier = Modifier.height(16.dp))

            // Lista de painéis
            LazyColumn(
                modifier = Modifier.fillMaxSize()
            ) {
                if (paineis.isEmpty()) {
                    item {
                        Text("Nenhum painel encontrado.", style = MaterialTheme.typography.bodyMedium)
                    }
                }else{
                items(paineis) { painel ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("ID: ${painel.id}", style = MaterialTheme.typography.bodyMedium)
                            Text("Nome: ${painel.nome}", style = MaterialTheme.typography.bodyMedium)
                            Text("Produção Média: ${painel.producaoMedia} kWh", style = MaterialTheme.typography.bodyMedium)

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 8.dp),
                                horizontalArrangement = Arrangement.End
                            ) {

                                IconButton(onClick = {
                                    navController.navigate("editar/${painel.id}")
                                }) {
                                    Icon(Icons.Default.Edit, contentDescription = "Editar")
                                }

                                IconButton(onClick = {
                                    excluirPainel(painel.id) { success, message ->
                                        if (success) {
                                            carregarPaineis() // Recarregar os painéis após exclusão
                                        } else {
                                            errorMessage = message
                                        }
                                    }
                                }) {
                                    Icon(Icons.Default.Delete, contentDescription = "Excluir", tint = Color.Red)
                                }
                            }
                            }
                        }
                    }
                }
            }

            if (errorMessage.isNotEmpty()) {
                Text("Erro: $errorMessage", color = Color.Red, modifier = Modifier.padding(top = 16.dp))
            }
        }
    }

    @Composable
    fun EditarPainelScreen(painelId: Int, navController: NavController) {
        var nome by remember { mutableStateOf("") }
        var producaoMedia by remember { mutableStateOf("") }
        var errorMessage by remember { mutableStateOf("") }
        var isLoading by remember { mutableStateOf(true) }

        // Carregar os detalhes do painel a partir da API
        fun carregarPainelDetalhes() {
            isLoading = true
            carregarPainelDetalhes(painelId, onSuccess = { painel ->
                nome = painel.nome
                producaoMedia = painel.producaoMedia.toString()
                isLoading = false
            }, onError = { message ->
                errorMessage = message
                isLoading = false
            })
        }

        // Carregar os detalhes assim que a tela for composta
        LaunchedEffect(painelId) {
            carregarPainelDetalhes() // Chama a função para carregar os dados do painel
        }

        fun salvarPainel() {
            val novaProducaoMedia = producaoMedia.toDoubleOrNull()

            if (novaProducaoMedia != null) {
                atualizarPainel(painelId, nome, novaProducaoMedia,
                    onSuccess = {
                        // Se a atualização for bem-sucedida, retorna à tela de listagem
                        navController.popBackStack()
                    },
                    onError = { message ->
                        errorMessage = message
                    }
                )
            } else {
                errorMessage = "Produção média inválida"
            }
        }

        // Exibição da tela
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            Text("Editar Painel", style = MaterialTheme.typography.headlineMedium)

            Spacer(modifier = Modifier.height(16.dp))

            // Exibindo mensagem de erro caso tenha ocorrido
            if (errorMessage.isNotEmpty()) {
                Text("Erro: $errorMessage", color = Color.Red, modifier = Modifier.padding(top = 16.dp))
            }

            // Se estiver carregando, exibe um indicador de carregamento
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
            } else {
                // Campos de edição (apenas se os dados forem carregados)
                TextField(
                    value = nome,
                    onValueChange = { nome = it },
                    label = { Text("Nome") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                TextField(
                    value = producaoMedia,
                    onValueChange = { producaoMedia = it },
                    label = { Text("Produção Média (kWh)") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        salvarPainel()
                        navController.popBackStack()
                    },
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text("Salvar")
                }
            }
        }
    }

    fun cadastrarPainel(painel: Painel, url: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        val jsonBody = Gson().toJson(painel)
        val body = jsonBody.toRequestBody("application/json".toMediaType())

        val request = Request.Builder()
            .url(url)
            .post(body)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                onError("Erro ao realizar o cadastro: ${e.message}")
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    val responseBody = response.body?.string()
                    println("Cadastro realizado com sucesso: $responseBody")
                    runOnUiThread {
                        onSuccess()
                    }
                } else {
                    onError("Erro no cadastro: ${response.code}")
                }
            }
        })
    }

    fun listarPaineis(url: String, onSuccess: (List<Painel>) -> Unit, onError: (String) -> Unit) {

        val request = Request.Builder()
            .url(url)
            .build()

        client.newCall(request).enqueue(object : okhttp3.Callback {
            override fun onFailure(call: okhttp3.Call, e: java.io.IOException) {
                // Chamando o onError caso haja falha na requisição
                onError("Falha na requisição: ${e.message}")
            }

            override fun onResponse(call: okhttp3.Call, response: Response) {
                if (response.isSuccessful) {
                    // Caso a requisição seja bem-sucedida, parse a resposta JSON
                    val responseBody = response.body?.string() ?: ""
                    try {
                        // Usando Gson para deserializar a lista de Painéis
                        val painelListType = object : TypeToken<List<Painel>>() {}.type
                        val paineis: List<Painel> = gson.fromJson(responseBody, painelListType)
                        onSuccess(paineis)
                    } catch (e: Exception) {
                        onError("Erro ao processar os dados: ${e.message}")
                    }
                } else {
                    onError("Erro na requisição: ${response.message}")
                }
            }
        })
    }

    fun excluirPainel(painelId: Int, callback: (Boolean, String) -> Unit) {
        val urlExclusao = "https://ecosynergy-api.azurewebsites.net/api/Painel/$painelId"

        val request = Request.Builder().url(urlExclusao).delete().build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                callback(false, "Erro na rede: ${e.message}")
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    callback(true, "Painel excluído com sucesso!")
                } else {
                    callback(false, "Erro ao excluir painel: ${response.code}")
                }
            }
        })
    }

    fun carregarPainelDetalhes(id: Int, onSuccess: (Painel) -> Unit, onError: (String) -> Unit) {
        val client = OkHttpClient()
        val url = "https://ecosynergy-api.azurewebsites.net/api/Painel/$id" // URL da API com o ID como parâmetro

        val request = Request.Builder()
            .url(url)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: java.io.IOException) {
                onError("Falha na requisição: ${e.message}")
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    val responseBody = response.body?.string()
                    if (responseBody != null) {
                        try {
                            val painel = Gson().fromJson(responseBody, Painel::class.java)
                            onSuccess(painel) // Retorna o painel com os dados da API
                        } catch (e: Exception) {
                            onError("Erro ao processar os dados: ${e.message}")
                        }
                    } else {
                        onError("Resposta vazia da API")
                    }
                } else {
                    onError("Erro na requisição: ${response.message}")
                }
            }
        })
    }

    fun atualizarPainel(id: Int, nome: String, producaoMedia: Double, onSuccess: () -> Unit, onError: (String) -> Unit) {
        val client = OkHttpClient()

        val url = "https://ecosynergy-api.azurewebsites.net/api/Painel/$id" // A URL com o id na rota

        val painelAtualizado = Painel(id, nome, producaoMedia)

        val json = Gson().toJson(painelAtualizado)

        val requestBody = json.toRequestBody("application/json".toMediaType())

        val request = Request.Builder()
            .url(url)
            .put(requestBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                onError("Erro na requisição: ${e.message}")
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    onSuccess()
                } else {
                    onError("Erro ao atualizar painel: ${response.message}")
                }
            }
        })
    }
}

