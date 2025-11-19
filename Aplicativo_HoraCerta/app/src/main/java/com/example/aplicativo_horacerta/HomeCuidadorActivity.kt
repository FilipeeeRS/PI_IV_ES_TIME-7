package com.example.aplicativo_horacerta

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.ExperimentalFoundationApi
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import kotlinx.coroutines.launch
import androidx.compose.ui.platform.LocalContext
import com.example.aplicativo_horacerta.network.PedidoDeDeletarMedicamento
import com.example.aplicativo_horacerta.network.Resultado
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.net.Socket



import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.outlined.AddCircleOutline


class HomeCuidadorActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 1. LER OS DADOS DA INTENT
        val userUid = intent.getStringExtra(FazerLoginActivity.KEY_USER_UID)
        val profileType = intent.getStringExtra(FazerLoginActivity.KEY_PROFILE_TYPE)

        if (userUid == null || profileType == null) {
            // Caso de falha: Redirecionar para o login
            Toast.makeText(this, "Erro: Falha na passagem de dados de sessão.", Toast.LENGTH_LONG).show()
            // Opcional: Redirecionar para FazerLoginActivity ou fechar
            finish()
            return
        }

        // 2. USAR OS DADOS
        Toast.makeText(this, "Home do $profileType. UID: $userUid", Toast.LENGTH_SHORT).show()

        setContent {
            Surface(color = Color.Black) {
                HomeCuidador(
                    onAccessibilityClick = {
                        val intent = Intent(this, AcessibilidadeActivity::class.java)
                        startActivity(intent)
                    },
                    // PASSA A FUNÇÃO DE LOGOUT
                    onLogoutClick = { performLogout() }
                )
            }
        }
    }

    // Opcional: Adicionar um botão de Logout que remove os dados
    fun performLogout() {
        // Remove a sessão do Firebase
        FirebaseAuth.getInstance().signOut()

        // Limpa os dados do SharedPreferences
        val prefs = getSharedPreferences(FazerLoginActivity.PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().clear().apply()

        // Redireciona para a tela de Login
        val intent = Intent(this, FazerLoginActivity::class.java)
        // Adiciona flags para limpar o histórico de atividades
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish() // Fecha a Home
    }
}

data class Medicamento(
    val id: Int,
    val nome: String,
    val tomou: Boolean,
    val data: String,
    val horario: String
)

//listOf = exemplo para o preview
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

//listOf = exemplo para o preview
val sampleHistorico = listOf(
    HistoricoMedicamento(1, "Buscopan", true, "25/01 Sexta-Feir", "18:30"),
    HistoricoMedicamento(2, "Buscopan", false, "25/01 Sexta-Feira", "18:30"),
    HistoricoMedicamento(3, "Salonpas", false, "25/01 Sexta-Feira", "18:30"),
    HistoricoMedicamento(4, "Salonpas", true, "25/01 Sexta-Feira", "18:30"),
    HistoricoMedicamento(5, "Salonpas", true, "25/01 Sexta-Feira", "18:30")
)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun HomeCuidador(
    initialTabIndex: Int = 0,
    onAccessibilityClick: () -> Unit = {},
    // NOVO PARÂMETRO PARA LOGOUT
    onLogoutClick: () -> Unit
) {

    val pagerState = rememberPagerState(initialPage = initialTabIndex) { 2 }
    var selectedTabIndex by remember { mutableIntStateOf(initialTabIndex) }
    val scope = rememberCoroutineScope()
    val tabBarColor = Color(0xFFEEEEEE)
    val contentColor = Color.White

    LaunchedEffect(pagerState.currentPage) {
        selectedTabIndex = pagerState.currentPage
    }

    val context = LocalContext.current
    val medicamentosList = remember { mutableStateListOf(*sampleMedicamentos.toTypedArray()) }

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
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.weight(1f) // Adiciona peso para empurrar o botão para a direita
                        )


                        IconButton(
                            onClick = onLogoutClick,
                            modifier = Modifier.align(Alignment.CenterVertically)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.ExitToApp,
                                contentDescription = "Sair e Fazer Login",
                                tint = Color.White,
                                modifier = Modifier.size(32.dp)
                            )
                        }
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
                        onClick = { scope.launch { pagerState.animateScrollToPage(1) } },
                        text = { Text("HISTÓRICO", fontSize = 18.sp) },
                        selectedContentColor = Color.Black,
                        unselectedContentColor = Color.Gray
                    )
                }
            }
        },
        floatingActionButton = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                // Botão Acessibilidade
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

                IconButton(
                    onClick = {
                        val intent = Intent(context, CuidadorConectarIdosoActivity::class.java)


                        context.startActivity(intent)
                    },
                    modifier = Modifier.size(56.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.AddCircleOutline,
                        contentDescription = "Conectar a Idoso",
                        modifier = Modifier.size(100.dp),
                        tint = Color.Black
                    )
                }
            }
        },
        floatingActionButtonPosition = FabPosition.Center

    ) { paddingValues ->
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(contentColor)
        ) { page ->
            when (page) {
                0 -> MedicamentosTab(
                    medicamentosList = medicamentosList,
                    onRemoveMedicamento = { medicamento ->
                        scope.launch {
                            //  Deletar do servidor
                            val resultado = performDeleteMedicamento(medicamento.id.toString())

                            if (resultado?.isSucesso == true) {
                                // Se deletou, remove da lista
                                medicamentosList.remove(medicamento)
                                Toast.makeText(context, "Medicamento deletado!", Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(context, "Erro: ${resultado?.mensagem}", Toast.LENGTH_LONG).show()
                            }
                        }
                    }
                )
                1 -> HistoricoTab()
            }
        }
    }
}

// Função de rede para deletar medicamento
suspend fun performDeleteMedicamento(idMedicamento: String): Resultado? {

    val SERVER_IP = "192.168.0.10"
    val SERVER_PORT = 12345

    val pedido = PedidoDeDeletarMedicamento(idMedicamento)

    // Roda a rede em background
    return withContext(Dispatchers.IO) {
        try {
            val socket = Socket(SERVER_IP, SERVER_PORT)
            val oos = ObjectOutputStream(socket.outputStream)

            oos.writeObject(pedido) // Envia o pedido
            oos.flush()

            val ois = ObjectInputStream(socket.inputStream)
            val resposta = ois.readObject() as? Resultado

            ois.close()
            oos.close()
            socket.close()
            //recebe a resposta
            resposta

        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}

////////////////////////////////////////////////////////////////////////////////
@Composable
fun MedicamentosTab(
    medicamentosList: List<Medicamento>,
    onRemoveMedicamento: (Medicamento) -> Unit
) {
    val context = LocalContext.current

    var showDeleteDialog by remember { mutableStateOf(false) }
    var medicamentoParaDeletar by remember { mutableStateOf<Medicamento?>(null) }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        TextButton(
            onClick = {
                val intent = Intent(context, RemédioCriarActivity::class.java)
                context.startActivity(intent)
            },
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
            items(medicamentosList) { medicamento ->
                MedicamentoItem(
                    medicamento = medicamento,
                    onDeleteClick = {
                        medicamentoParaDeletar = medicamento
                        showDeleteDialog = true },
                    onEditClick = {
                        val intent = Intent(context, RemédioEditarActivity::class.java)
                        intent.putExtra("MEDICAMENTO_ID", medicamento.id.toString())
                        context.startActivity(intent)
                    }
                )
            }
        }
    }

    if (showDeleteDialog && medicamentoParaDeletar != null) {
        DeleteConfirmationDialog(
            medicamentoNome = medicamentoParaDeletar!!.nome,
            onConfirmDelete = {
                onRemoveMedicamento(medicamentoParaDeletar!!)
                showDeleteDialog = false
                medicamentoParaDeletar = null
            },
            onDismiss = {
                showDeleteDialog = false
                medicamentoParaDeletar = null
            }
        )
    }
}

////////////////////////////////////////////////////////////////////////////////
@Composable
fun DeleteConfirmationDialog(
    medicamentoNome: String,
    onConfirmDelete: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(text = "Confirmar Exclusão")
        },
        text = {
            Text(text = "Você tem certeza que deseja deletar o medicamento \"$medicamentoNome\"?")
        },
        confirmButton = {
            Button(
                onClick = onConfirmDelete,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Red
                )
            ) {
                Text("Deletar")
            }
        },
        dismissButton = {
            Button(
                onClick = onDismiss
            ) {
                Text("Cancelar")
            }
        }
    )
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
    // se a lista estiver vazia, não mostra nada
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
        // Passa uma função vazia para o Logout no Preview
        HomeCuidador(initialTabIndex = 0, onLogoutClick = {})
    }
}
@Preview(showSystemUi = true, showBackground = true, name = "Aba Histórico")
@Composable
fun PreviewHomeCuidadorHistorico() {
    Surface(color = Color.White) {
        // Passa uma função vazia para o Logout no Preview
        HomeCuidador(initialTabIndex = 1, onLogoutClick = {})
    }
}