package com.example.pekuscalc

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.pekuscalc.ui.theme.PekusCalcTheme
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PekusCalcTheme {
                val viewModel: CalcViewModel = viewModel()
                MainScreen(viewModel)
            }
        }
    }
}

@Composable
fun MainScreen(viewModel: CalcViewModel) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Calculadora", "Histórico")

    Scaffold(
        topBar = {
            TabRow(selectedTabIndex = selectedTab) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { 
                            selectedTab = index 
                            if (index == 1) viewModel.listarCalculos()
                        },
                        text = { Text(title) }
                    )
                }
            }
        },
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding)) {
            if (selectedTab == 0) {
                CalculadoraScreen(viewModel)
            } else {
                HistoricoScreen(viewModel)
            }
        }
    }
}

@Composable
fun CalculadoraScreen(viewModel: CalcViewModel) {
    CalculadoraContent(
        valorA = viewModel.valorA,
        valorB = viewModel.valorB,
        statusMessage = viewModel.statusMessage,
        isLoading = viewModel.isLoading,
        onValorAChange = { viewModel.valorA = it },
        onValorBChange = { viewModel.valorB = it },
        onCalcular = { viewModel.calcularESalvar(it) }
    )
}

@Composable
fun CalculadoraContent(
    valorA: String,
    valorB: String,
    statusMessage: String,
    isLoading: Boolean,
    onValorAChange: (String) -> Unit,
    onValorBChange: (String) -> Unit,
    onCalcular: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        OutlinedTextField(
            value = valorA,
            onValueChange = onValorAChange,
            label = { Text("Valor A") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = valorB,
            onValueChange = onValorBChange,
            label = { Text("Valor B") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            modifier = Modifier.fillMaxWidth()
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            listOf("+", "-", "*", "/").forEach { op ->
                Button(onClick = { onCalcular(op) }) {
                    Text(op, fontSize = 20.sp)
                }
            }
        }

        if (statusMessage.isNotEmpty()) {
            Text(
                text = statusMessage,
                color = if (statusMessage.contains("sucesso")) Color(0xFF388E3C) else MaterialTheme.colorScheme.error,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 16.dp)
            )
        }
        
        if (isLoading) {
            CircularProgressIndicator()
        }
    }
}

@Composable
fun HistoricoScreen(viewModel: CalcViewModel) {
    LaunchedEffect(Unit) {
        viewModel.listarCalculos()
    }

    HistoricoContent(
        calculos = viewModel.calculos,
        isLoading = viewModel.isLoading,
        onDelete = { viewModel.deletarCalculo(it) }
    )
}

@Composable
fun HistoricoContent(
    calculos: List<Calculadora>,
    isLoading: Boolean,
    onDelete: (Int) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        if (isLoading && calculos.isEmpty()) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.primaryContainer)
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("ID", Modifier.weight(0.5f), fontWeight = FontWeight.Bold)
                    Text("Cálculo", Modifier.weight(2f), fontWeight = FontWeight.Bold)
                    Text("Data", Modifier.weight(1.5f), fontWeight = FontWeight.Bold)
                    Spacer(Modifier.width(48.dp))
                }
            }

            items(calculos) { calc ->
                CalculoItem(calc, onDelete = { calc.id?.let { onDelete(it) } })
            }
        }
    }
}

@Composable
fun CalculoItem(calc: Calculadora, onDelete: () -> Unit) {
    val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US)
    val outputFormat = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault())
    
    val formattedDate = try {
        val cleanDate = calc.dataCalculo.substringBefore(".")
        val date = inputFormat.parse(cleanDate)
        outputFormat.format(date ?: Date())
    } catch (e: Exception) {
        calc.dataCalculo
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(8.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = calc.id.toString(), modifier = Modifier.weight(0.5f))
            
            Column(modifier = Modifier.weight(2f)) {
                Text(
                    text = "${"%.2f".format(calc.valorA)} ${calc.operacao} ${"%.2f".format(calc.valorB)}",
                    fontSize = 12.sp
                )
                Text(
                    text = "= ${"%.2f".format(calc.resultado)}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }
            
            Text(text = formattedDate, modifier = Modifier.weight(1.5f), fontSize = 10.sp)
            
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Excluir", tint = Color.Red)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun CalculadoraPreview() {
    PekusCalcTheme {
        CalculadoraContent(
            valorA = "10",
            valorB = "5",
            statusMessage = "Preview status",
            isLoading = false,
            onValorAChange = {},
            onValorBChange = {},
            onCalcular = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun HistoricoPreview() {
    PekusCalcTheme {
        HistoricoContent(
            calculos = listOf(
                Calculadora(id = 1, valorA = 10.0, valorB = 2.0, operacao = "/", resultado = 5.0, dataCalculo = "2023-10-27T10:00:00Z"),
                Calculadora(id = 2, valorA = 100.0, valorB = 50.0, operacao = "-", resultado = 50.0, dataCalculo = "2023-10-27T11:30:00Z")
            ),
            isLoading = false,
            onDelete = {}
        )
    }
}
