package com.example.pekuscalc

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class CalcViewModel : ViewModel() {
    private val apiKey = "MNM202609"
    
    var valorA by mutableStateOf("")
    var valorB by mutableStateOf("")
    var statusMessage by mutableStateOf("")
    var calculos by mutableStateOf<List<Calculadora>>(emptyList())
    var isLoading by mutableStateOf(false)

    fun calcularESalvar(operacao: String) {
        val a = valorA.toDoubleOrNull()
        val b = valorB.toDoubleOrNull()

        if (a == null || b == null) {
            statusMessage = "Por favor, insira valores válidos."
            return
        }

        if (operacao == "/" && b == 0.0) {
            statusMessage = "Divisão por zero não permitida."
            return
        }

        val resultado = when (operacao) {
            "+" -> a + b
            "-" -> a - b
            "*" -> a * b
            "/" -> a / b
            else -> 0.0
        }

        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US)
        sdf.timeZone = TimeZone.getTimeZone("UTC")
        val dataCalculo = sdf.format(Date())

        val novoCalculo = Calculadora(
            valorA = a,
            valorB = b,
            operacao = operacao,
            resultado = resultado,
            dataCalculo = dataCalculo
        )

        viewModelScope.launch {
            try {
                isLoading = true
                val response = RetrofitClient.instance.salvarCalculo(apiKey, novoCalculo)
                if (response.isSuccessful) {
                    val id = response.body()
                    statusMessage = "Dados armazenados com sucesso, ID de Armazenamento $id"
                    valorA = ""
                    valorB = ""
                    listarCalculos()
                } else {
                    statusMessage = "Erro ao salvar: ${response.code()}"
                }
            } catch (e: Exception) {
                statusMessage = "Erro de conexão: ${e.message}"
            } finally {
                isLoading = false
            }
        }
    }

    fun listarCalculos() {
        viewModelScope.launch {
            try {
                isLoading = true
                val response = RetrofitClient.instance.listarCalculos(apiKey)
                if (response.isSuccessful) {
                    calculos = response.body() ?: emptyList()
                }
            } catch (e: Exception) {
                // Silently fail or log
            } finally {
                isLoading = false
            }
        }
    }

    fun deletarCalculo(id: Int) {
        viewModelScope.launch {
            try {
                isLoading = true
                val response = RetrofitClient.instance.deletarCalculo(apiKey, id)
                if (response.isSuccessful) {
                    listarCalculos()
                }
            } catch (e: Exception) {
                // Silently fail
            } finally {
                isLoading = false
            }
        }
    }
}
