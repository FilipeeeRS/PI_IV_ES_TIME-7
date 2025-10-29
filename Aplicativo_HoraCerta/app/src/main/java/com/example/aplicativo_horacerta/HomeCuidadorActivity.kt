package com.example.aplicativo_horacerta

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.layout.Box
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import kotlinx.coroutines.launch


class HomeCuidadorActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Surface(color = Color.Black) {
                HomeCuidador(
                    onAccessibilityClick = {
                        // Criar e registrar a AcessiblidadeActivityval
                        // intent = Intent(this, AcessiblidadeActivity::class.java)
                        // startActivity(intent)
                    }
                )
            }
        }
    }
}

data class Medicamento(
    val id: Int,
    val nome: String,
    val tomou: Boolean,
    val data: String,
    val horario: String
)
//listOf funciona como exemplo
val sampleMedicamentos = listOf(
    Medicamento(1, "Buscopan", true, "02/04 Quarta-Feira", "18:30"),
    Medicamento(2, "Buscopan",true, "02/04 Quarta-Feira", "18:30"),
    Medicamento(3, "Salonpas",false, "02/04 Quarta-Feira", "18:30"),
    Medicamento(4, "Salonpas",false, "02/04 Quarta-Feira", "18:30"),
    Medicamento(5, "Salonpas", true, "02/04 Quarta-Feira", "18:30"),
)

data class HistoricoMedicamento(
    val id: Int,
    val nome: String,
    val tomou: Boolean,
    val data: String,
    val horario: String
)
//listOf funciona como exemplo
val sampleHistorico = listOf(
    HistoricoMedicamento(1, "Buscopan", true, "25/01 Sexta-Feir", "18:30"),
    HistoricoMedicamento(2, "Buscopan", false, "25/01 Sexta-Feira", "18:30"),
    HistoricoMedicamento(3, "Salonpas", false, "25/01 Sexta-Feira", "18:30"),
    HistoricoMedicamento(4, "Salonpas", true, "25/01 Sexta-Feira", "18:30"),
    HistoricoMedicamento(5, "Salonpas", true, "25/01 Sexta-Feira", "18:30")
)

@Composable
fun HomeCuidador(
    initialTabIndex: Int = 0,
    onAccessibilityClick: () -> Unit = {}
) {

    val pagerState = rememberPagerState(initialPage = initialTabIndex) { 2 }
    var selectedTabIndex by remember { mutableIntStateOf(initialTabIndex) }
    val scope = rememberCoroutineScope()
    val tabBarColor = Color(0xFFEEEEEE)
    val contentColor = Color.White

    LaunchedEffect(pagerState.currentPage) {
        selectedTabIndex = pagerState.currentPage
    }

    Scaffold(
        topBar = {
            Column {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(130.dp)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_launcher_background),
                        contentDescription = "Fundo do Cabeçalho",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_launcher_foreground),
                            contentDescription = "Logo",
                            tint = Color.White,
                            modifier = Modifier.size(100.dp)
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(
                            text = "CUIDADOR",
                            color = Color.White,
                            fontSize = 30.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                TabRow(
                    selectedTabIndex = selectedTabIndex,
                    containerColor = tabBarColor,
                    contentColor = Color.Black,
                    indicator = { tabPositions ->
                        val currentTabPosition = tabPositions[selectedTabIndex]
                        Box(
                            Modifier
                                .fillMaxWidth()
                                .wrapContentSize(Alignment.BottomStart)
                                .offset(x = currentTabPosition.left)
                                .width(currentTabPosition.width)
                                .height(3.dp)
                                .background(Color.Black)
                        )
                    }
                ) {
                    Tab(
                        selected = selectedTabIndex == 0,
                        onClick = { scope.launch { pagerState.animateScrollToPage(0) } },
                        text = { Text("MEDICAMENTOS", fontSize = 18.sp) },
                        selectedContentColor = Color.Black,
                        unselectedContentColor = Color.Gray
                    )
                    Tab(
                        selected = selectedTabIndex == 1,
                        onClick = { scope.launch { pagerState.animateScrollToPage(1) } },                        text = { Text("HISTÓRICO", fontSize = 18.sp) },
                        selectedContentColor = Color.Black,
                        unselectedContentColor = Color.Gray
                    )
                }
            }
        },
        floatingActionButton = {
            IconButton(
                onClick = onAccessibilityClick,
                modifier = Modifier.size(56.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.Info,
                    contentDescription = "Acessibilidade",
                    modifier = Modifier.size(100.dp),
                    tint = Color.Black
                )
            }
        },
        floatingActionButtonPosition = FabPosition.Start

    ) { paddingValues ->
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(contentColor)
        ) { page ->
            when (page) {
                0 -> MedicamentosTab()
                1 -> HistoricoTab()
            }
        }
    }
}
////////////////////////////////////////////////////////////////////////////////
@Composable
fun MedicamentosTab() {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        TextButton(
            onClick = { /* TODO: Navegar para Adicionar Medicamento */ },
            modifier = Modifier.padding(vertical = 8.dp)
        ) {
            Text(
                text = "adicionar novo medicamento +",
                color = Color.Blue,
                textDecoration = TextDecoration.Underline,
                fontSize = 18.sp
            )
        }
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(sampleMedicamentos) { medicamento ->
                MedicamentoItem(
                    medicamento = medicamento,
                    onDeleteClick = { /* TODO: Deletar */ },
                    onEditClick = { /* TODO: Editar */ }
                )
            }
        }
    }
}
@Composable
fun MedicamentoItem(
    medicamento: Medicamento,
    onDeleteClick: () -> Unit,
    onEditClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFF0F0F0)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = medicamento.nome, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = "Dia: ${medicamento.data}", fontSize = 14.sp, color = Color.Gray)
                Text(text = "Horário: ${medicamento.horario}", fontSize = 14.sp, color = Color.Gray)
            }
            IconButton(onClick = onDeleteClick) {Icon(Icons.Filled.Delete, contentDescription = "Deletar", tint = Color.Gray,modifier = Modifier.size(28.dp)) }
            IconButton(onClick = onEditClick) { Icon(Icons.Filled.Edit, contentDescription = "Editar", tint = Color.Gray, modifier = Modifier.size(28.dp)) }
        }
    }
}
////////////////////////////////////////////////////////////////////////////////
@Composable
fun HistoricoTab() {
    // Se a lista estiver vazia, LazyColumn não mostra nada
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(sampleHistorico) { historicoItem ->
            HistoricoItem(historico = historicoItem)
        }
    }
}
@Composable
fun HistoricoItem(historico: HistoricoMedicamento) {
    val statusText = if (historico.tomou) "Status: Tomou" else "Status: Não Tomou"
    val icon = if (historico.tomou) Icons.Filled.Check else Icons.Filled.Close
    val iconColor = if (historico.tomou) Color.Gray else Color.Red
    val textColor = if (historico.tomou) Color.Gray else Color.Unspecified

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFF0F0F0))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = historico.nome, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = textColor)
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = "Dia: ${historico.data}", fontSize = 14.sp, color = textColor)
                Text(text = "Horário: ${historico.horario}", fontSize = 14.sp, color = textColor)
                Text(text = statusText, fontSize = 14.sp, color = textColor)
            }
            Icon(
                imageVector = icon,
                contentDescription = statusText,
                tint = iconColor,
                modifier = Modifier.size(35.dp)
            )
        }
    }
}

@Preview(showSystemUi = true, showBackground = true, name = "Aba Medicamentos")
@Composable
fun PreviewHomeCuidador() {
    Surface(color = Color.White) {
        HomeCuidador(initialTabIndex = 0)
    }
}
@Preview(showSystemUi = true, showBackground = true, name = "Aba Histórico")
@Composable
fun PreviewHomeCuidadorHistorico() {
    Surface(color = Color.White) {
        HomeCuidador(initialTabIndex = 1)
    }
}