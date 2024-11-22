package br.com.fiap.mobile.lumenapp

import android.os.Bundle
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
import androidx.navigation.compose.*
import br.com.fiap.mobile.lumenapp.models.Painel
import br.com.fiap.mobile.lumenapp.ui.theme.LoginAppTheme
import com.google.gson.Gson
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import java.io.IOException

class MainActivity : ComponentActivity() {
    val client = OkHttpClient()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            LoginAppTheme {
                val navController = rememberNavController()
                NavHost(navController, startDestination = "login") {
                    composable("login") { LoginScreen(navController) }
                    composable("home") { HomeScreen(navController) }
                    composable("cadastro_painel") { CadastroPainelScreen() }
                    composable("listagem_paineis") { ListagemPaineisScreen(navController) }
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

            // Campo de Nome de Usuário
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

            // Exibindo mensagem de erro
            if (errorMessage.isNotEmpty()) {
                Text(text = errorMessage, color = Color.Red)
                Spacer(modifier = Modifier.height(8.dp))
            }

            // Botão de Login
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
    fun CadastroPainelScreen() {
        var nome by remember { mutableStateOf("") }
        var producaoMedia by remember { mutableStateOf("") }
        var isSuccess by remember { mutableStateOf(false) }
        var errorMessage by remember { mutableStateOf("") }

        fun handleCadastro() {
            if (nome.isNotEmpty() && producaoMedia.isNotEmpty()) {
                val painel = Painel(id = 0, nome = nome, producaoMedia = producaoMedia.toDouble())
                val urlCadastro = "https://ecosynergy-api.azurewebsites.net/api/Painel"

                cadastrarPainel(painel, urlCadastro)
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

        fun carregarPaineis(){
            val urlListagem = "https://ecosynergy-api.azurewebsites.net/api/Painel"
            listarPaineis(urlListagem)
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

            // Mensagem de erro
            if (errorMessage.isNotEmpty()) {
                Text("Erro: $errorMessage", color = Color.Red, modifier = Modifier.padding(top = 16.dp))
            }
        }
    }

    fun cadastrarPainel(painel: Painel, url: String) {
        val jsonBody = Gson().toJson(painel)

        val body = jsonBody
            .toRequestBody("application/json".toMediaType())
        val request = Request.Builder()
            .url(url)
            .post(body)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    val responseBody = response.body?.string()
                    println("Cadastro realizado com sucesso: $responseBody")
                } else {
                    println("Erro no cadastro: ${response.code}")
                }
            }
        })
    }

    fun listarPaineis(url: String) {
        val request = Request.Builder()
            .url(url)
            .get()
            .build()

        // Envia a requisição de forma assíncrona
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace() // Tratar erro aqui
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    // Sucesso, processa a resposta aqui
                    val responseBody = response.body?.string()

                    // Converte a resposta JSON em uma lista de objetos Painel
                    val paineis = Gson().fromJson(responseBody, Array<Painel>::class.java).toList()
                    paineis.forEach {
                        println("Painel: ${it.id}, ${it.nome}, ${it.producaoMedia}")
                    }
                } else {
                    // Tratar erro HTTP
                    println("Erro na listagem: ${response.code}")
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

}

