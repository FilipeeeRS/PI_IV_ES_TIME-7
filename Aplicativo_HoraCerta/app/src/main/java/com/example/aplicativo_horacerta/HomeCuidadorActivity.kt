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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.outlined.AddCircleOutline
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.aplicativo_horacerta.FazerLoginActivity.Companion.KEY_USER_UID
import com.example.aplicativo_horacerta.network.Comunicado
import com.example.aplicativo_horacerta.socket.MedicamentoRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.gson.annotations.SerializedName
import kotlinx.coroutines.launch
import java.io.Serializable
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class HomeCuidadorActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val userUid = intent.getStringExtra(FazerLoginActivity.KEY_USER_UID)
        val profileType = intent.getStringExtra(FazerLoginActivity.KEY_PROFILE_TYPE)

        if (userUid == null || profileType == null) {
            Toast.makeText(this, "Erro: Falha na passagem de dados de sessão.", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        setContent {
            Surface(color = Color.Black) {
                HomeCuidador(
                    userId = userUid,
                    onAccessibilityClick = {
                        val intent = Intent(this, AcessibilidadeActivity::class.java)
                        startActivity(intent)
                    },
                    onLogoutClick = { performLogout() }
                )
            }
        }
    }

    fun performLogout() {
        FirebaseAuth.getInstance().signOut()
        val prefs = getSharedPreferences(FazerLoginActivity.PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().clear().apply()

        val intent = Intent(this, InicioTelaActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}

// --- Data Classes ---

data class Medicamento(
    val id: String,
    val nome: String,
    val tomou: Boolean,
    @SerializedName("dia") val data: String,
    val horario: String,
    val descricao: String,
    val idUsuario: String
) : Serializable

data class ResultadoListaMedicamentos(
    @SerializedName("tipo") val tipo: String = "ResultadoListaMedicamentos",
    @SerializedName("medicamentos") val medicamentos: List<Medicamento>?
) : Comunicado()

data class WrapperResposta(
    @SerializedName("operacao") val operacaoJsonString: String
)

data class HistoricoMedicamento(
    val id: String,
    val nome: String,
    val tomou: Boolean,
    val data: String,
    val horario: String
)

data class ResultadoOperacao(
    @SerializedName("tipo") val tipo: String,
    @SerializedName("sucesso") val isSucesso: Boolean,
    @SerializedName("mensagem") val mensagem: String? = null
) : Comunicado()

// --- Composables ---

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun HomeCuidador(
    userId: String?,
    initialTabIndex: Int = 0,
    onAccessibilityClick: () -> Unit = {},
    onLogoutClick: () -> Unit
) {
    // Estados de UI
    val pagerState = rememberPagerState(initialPage = initialTabIndex) { 2 }
    var selectedTabIndex by remember { mutableIntStateOf(initialTabIndex) }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    // Listas de dados
    val medicamentosList = remember { mutableStateListOf<Medicamento>() }
    val historicoList = remember { mutableStateListOf<HistoricoMedicamento>() }
    val lifecycleOwner = LocalLifecycleOwner.current

    val tabBarColor = Color(0xFFEEEEEE)
    val contentColor = Color.White

    // Sincroniza a aba selecionada com o pager
    LaunchedEffect(pagerState.currentPage) {
        selectedTabIndex = pagerState.currentPage
    }

    // Observer: Atualiza a lista sempre que a tela volta a ficar visível (Resume)
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                if (userId != null) {
                    scope.launch {
                        // Busca dados no servidor/repositório
                        val resultado = MedicamentoRepository.performListarMedicamentos(userId)

                        if (resultado?.medicamentos != null) {
                            val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale("pt", "BR"))
                            val agora = Calendar.getInstance().time

                            medicamentosList.clear()
                            historicoList.clear()

                            // Separa remédios futuros (Lista) de passados (Histórico)
                            val (futuros, passados) = resultado.medicamentos.partition { remedio ->
                                try {
                                    val dataHoraRemedio = sdf.parse("${remedio.data} ${remedio.horario}")
                                    dataHoraRemedio != null && dataHoraRemedio.after(agora)
                                } catch (e: Exception) {
                                    false
                                }
                            }

                            // Ordena e preenche a lista principal
                            medicamentosList.addAll(futuros.sortedBy {
                                try {
                                    sdf.parse("${it.data} ${it.horario}")
                                } catch (e: Exception) {
                                    null
                                }
                            })

                            // Preenche o histórico
                            passados.forEach { remedioPassado ->
                                historicoList.add(
                                    HistoricoMedicamento(
                                        id = remedioPassado.id,
                                        nome = remedioPassado.nome,
                                        tomou = remedioPassado.tomou,
                                        data = remedioPassado.data,
                                        horario = remedioPassado.horario
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }

        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    Scaffold(
        topBar = {
            Column {
                // Cabeçalho com Imagem e Logo
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
                            modifier = Modifier.weight(1f)
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

                // Abas de Navegação (Medicamentos / Histórico)
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
        // Conteúdo das Abas
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(contentColor)
        ) { page ->
            when (page) {
                0 -> MedicamentosTab(
                    userId = userId ?: "",
                    medicamentosList = medicamentosList,
                    onRemoveMedicamento = { medicamento ->
                        scope.launch {
                            val usuarioId = userId ?: return@launch

                            // Chamada ao Repositório para Deletar
                            val resultado = MedicamentoRepository.performDeleteMedicamento(
                                medicamento.id,
                                usuarioId
                            )

                            if (resultado.isSucesso) {
                                medicamentosList.remove(medicamento)
                                Toast.makeText(context, "Medicamento deletado!", Toast.LENGTH_SHORT).show()
                            } else {
                                val mensagemErro = resultado.mensagem ?: "Erro desconhecido ao deletar."
                                Toast.makeText(context, mensagemErro, Toast.LENGTH_LONG).show()
                            }
                        }
                    }
                )
                1 -> HistoricoTab(historicoList = historicoList)
            }
        }
    }
}

@Composable
fun MedicamentosTab(
    userId: String,
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
                intent.putExtra(KEY_USER_UID, userId)
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
                        showDeleteDialog = true
                    },
                    onEditClick = {
                        val intent = Intent(context, RemédioEditarActivity::class.java)
                        intent.putExtra("DADOS_MEDICAMENTO", medicamento as Serializable)
                        intent.putExtra(KEY_USER_UID, userId)
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

@Composable
fun DeleteConfirmationDialog(
    medicamentoNome: String,
    onConfirmDelete: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = "Confirmar Exclusão") },
        text = { Text(text = "Você tem certeza que deseja deletar o medicamento \"$medicamentoNome\"?") },
        confirmButton = {
            Button(
                onClick = onConfirmDelete,
                colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
            ) {
                Text("Deletar")
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) { Text("Cancelar") }
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
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF0F0F0))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = medicamento.nome,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Dia: ${medicamento.data}",
                    fontSize = 14.sp,
                    color = Color.Gray
                )
                Text(
                    text = "Horário: ${medicamento.horario}",
                    fontSize = 14.sp,
                    color = Color.Gray
                )
            }
            IconButton(onClick = onEditClick) {
                Icon(
                    imageVector = Icons.Filled.Edit,
                    contentDescription = "Editar",
                    tint = Color.Gray,
                    modifier = Modifier.size(28.dp)
                )
            }
            IconButton(onClick = onDeleteClick) {
                Icon(
                    imageVector = Icons.Filled.Delete,
                    contentDescription = "Deletar",
                    tint = Color.Gray,
                    modifier = Modifier.size(28.dp)
                )
            }
        }
    }
}

@Composable
fun HistoricoTab(historicoList: List<HistoricoMedicamento>) {
    if (historicoList.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text("Nenhum remédio no histórico.", color = Color.Gray)
        }
    }
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(historicoList) { historicoItem ->
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
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF0F0F0))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = historico.nome,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = textColor
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Dia: ${historico.data}",
                    fontSize = 14.sp,
                    color = textColor
                )
                Text(
                    text = "Horário: ${historico.horario}",
                    fontSize = 14.sp,
                    color = textColor
                )
                Text(
                    text = statusText,
                    fontSize = 14.sp,
                    color = textColor
                )
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