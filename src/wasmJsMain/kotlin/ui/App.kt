package ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import br.edu.ufapetro.planner.domain.usecase.painel.GerarResumoDoDiaUseCase
import data.repository.LembreteRepositoryLocalStorage
import data.repository.MetaRepositoryLocalStorage
import data.repository.TarefaRepositoryLocalStorage
import domain.usecase.classroom.SincronizarClassroomUseCase
import domain.usecase.lembrete.CriarLembreteUseCase
import domain.usecase.lembrete.ListarLembretesUseCase
import domain.usecase.lembrete.RemoverLembreteUseCase
import domain.usecase.meta.AtualizarStatusMetaUseCase
import domain.usecase.meta.CriarMetaUseCase
import domain.usecase.meta.ListarMetasUseCase
import domain.usecase.meta.RemoverMetaUseCase
import domain.usecase.painel.ListarAtividadesDoMesUseCase
import domain.usecase.relatorio.GerarRelatorioAnualUseCase
import domain.usecase.relatorio.GerarRelatorioMensalUseCase
import domain.usecase.relatorio.GerarRelatorioSemanalUseCase
import domain.usecase.tarefa.AtualizarStatusTarefaUseCase
import domain.usecase.tarefa.CriarTarefaUseCase
import domain.usecase.tarefa.ListarTarefasPorDataUseCase
import domain.usecase.tarefa.RemoverTarefaUseCase
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import ui.screens.ClassroomScreen
import ui.screens.LembretesScreen
import ui.screens.MetasScreen
import ui.screens.PainelScreen
import ui.screens.RelatoriosScreen
import ui.screens.TarefasScreen
import ui.theme.PlannerTheme

enum class Tela(val rotulo: String) {
    PAINEL("Painel"), TAREFAS("Tarefas"), METAS("Metas"), LEMBRETES("Lembretes"),
    CLASSROOM("Classroom"), RELATORIOS("Relatórios")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun App() {
    val tarefaRepository = remember { TarefaRepositoryLocalStorage() }
    val metaRepository = remember { MetaRepositoryLocalStorage() }
    val lembreteRepository = remember { LembreteRepositoryLocalStorage() }

    val criarTarefaUseCase = remember { CriarTarefaUseCase(tarefaRepository) }
    val listarTarefasPorDataUseCase = remember { ListarTarefasPorDataUseCase(tarefaRepository) }
    val atualizarStatusTarefaUseCase = remember { AtualizarStatusTarefaUseCase(tarefaRepository) }
    val removerTarefaUseCase = remember { RemoverTarefaUseCase(tarefaRepository) }

    val criarMetaUseCase = remember { CriarMetaUseCase(metaRepository) }
    val listarMetasUseCase = remember { ListarMetasUseCase(metaRepository) }
    val atualizarStatusMetaUseCase = remember { AtualizarStatusMetaUseCase(metaRepository) }
    val removerMetaUseCase = remember { RemoverMetaUseCase(metaRepository) }

    val criarLembreteUseCase = remember { CriarLembreteUseCase(lembreteRepository) }
    val listarLembretesUseCase = remember { ListarLembretesUseCase(lembreteRepository) }
    val removerLembreteUseCase = remember { RemoverLembreteUseCase(lembreteRepository) }

    val gerarResumoDoDiaUseCase = remember {
        GerarResumoDoDiaUseCase(tarefaRepository, metaRepository, lembreteRepository)
    }
    val listarAtividadesDoMesUseCase = remember {
        ListarAtividadesDoMesUseCase(tarefaRepository, metaRepository, lembreteRepository)
    }

    val sincronizarClassroomUseCase = remember { SincronizarClassroomUseCase(tarefaRepository) }
    val gerarRelatorioSemanalUseCase = remember { GerarRelatorioSemanalUseCase(tarefaRepository, metaRepository) }
    val gerarRelatorioMensalUseCase = remember { GerarRelatorioMensalUseCase(tarefaRepository, metaRepository) }
    val gerarRelatorioAnualUseCase = remember { GerarRelatorioAnualUseCase(tarefaRepository, metaRepository) }

    val sistemaEmModoEscuro = isSystemInDarkTheme()
    var modoEscuroAtivo by remember { mutableStateOf(sistemaEmModoEscuro) }

    PlannerTheme(isModoEscuro = modoEscuroAtivo) {
        var telaAtual by remember { mutableStateOf(Tela.PAINEL) }
        val drawerState = rememberDrawerState(DrawerValue.Closed)
        val scope = rememberCoroutineScope()

        fun navegarPara(tela: Tela) {
            telaAtual = tela
            scope.launch { drawerState.close() }
        }

        ModalNavigationDrawer(
            drawerState = drawerState,
            drawerContent = {
                ModalDrawerSheet {
                    Spacer(Modifier.height(20.dp))
                    Text(
                        "Planner Virtual",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 24.dp)
                    )
                    Spacer(Modifier.height(20.dp))

                    Tela.entries.forEach { tela ->
                        val iconColor = if (telaAtual == tela) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        }

                        NavigationDrawerItem(
                            label = { Text(tela.rotulo) },
                            selected = telaAtual == tela,
                            onClick = { navegarPara(tela) },
                            icon = {
                                when (tela) {
                                    Tela.PAINEL -> IconePainel(color = iconColor)
                                    Tela.TAREFAS -> IconeTarefas(color = iconColor)
                                    Tela.METAS -> IconeMetas(color = iconColor)
                                    Tela.LEMBRETES -> IconeLembretes(color = iconColor)
                                    Tela.CLASSROOM -> IconeClassroom(color = iconColor)
                                    Tela.RELATORIOS -> IconeRelatorios(color = iconColor)
                                }
                            },
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 2.dp)
                        )
                    }

                    Spacer(Modifier.weight(1f))
                    HorizontalDivider()

                    // Alternador de tema
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { modoEscuroAtivo = !modoEscuroAtivo }
                            .padding(horizontal = 24.dp, vertical = 16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (modoEscuroAtivo) "Modo Noturno" else "Modo Claro",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Switch(
                            checked = modoEscuroAtivo,
                            onCheckedChange = { modoEscuroAtivo = it },
                            colors = SwitchDefaults.colors(
                                checkedIconColor = Color(0xFFE2E8F0),
                                uncheckedIconColor = Color(0xFF0F172A)
                            ),
                            thumbContent = {
                                ThemeToggleIcon(
                                    isDark = modoEscuroAtivo,
                                    color = if (modoEscuroAtivo) Color(0xFFE2E8F0) else Color(0xFF0F172A)
                                )
                            }
                        )
                    }
                }
            }
        ) {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.background
            ) {
                Column(Modifier.fillMaxSize()) {
                    TopAppBar(
                        title = { Text(telaAtual.rotulo, fontWeight = FontWeight.Bold) },
                        navigationIcon = {
                            IconButton(onClick = { scope.launch { drawerState.open() } }) {
                                MenuIcon(color = MaterialTheme.colorScheme.onSurface)
                            }
                        }
                    )

                    Box(Modifier.weight(1f)) {
                        when (telaAtual) {
                            Tela.PAINEL -> PainelScreen(gerarResumoDoDiaUseCase, listarAtividadesDoMesUseCase)
                            Tela.METAS -> MetasScreen(
                                criarMetaUseCase, listarMetasUseCase, atualizarStatusMetaUseCase, removerMetaUseCase
                            )
                            Tela.TAREFAS -> TarefasScreen(
                                criarTarefaUseCase,
                                listarTarefasPorDataUseCase,
                                atualizarStatusTarefaUseCase,
                                removerTarefaUseCase
                            )
                            Tela.LEMBRETES -> LembretesScreen(
                                criarLembreteUseCase, listarLembretesUseCase, removerLembreteUseCase
                            )
                            Tela.CLASSROOM -> ClassroomScreen(sincronizarClassroomUseCase)
                            Tela.RELATORIOS -> RelatoriosScreen(
                                gerarRelatorioSemanalUseCase, gerarRelatorioMensalUseCase, gerarRelatorioAnualUseCase
                            )
                        }
                    }
                }
            }
        }
    }
}

// Ícones Customizados em Canvas para a Gaveta
@Composable
fun IconePainel(color: Color, modifier: Modifier = Modifier.size(20.dp)) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val stroke = 1.8.dp.toPx()
        drawRoundRect(color = color, topLeft = Offset(0f, 0f), size = Size(w * 0.42f, h * 0.42f), style = Stroke(stroke))
        drawRoundRect(color = color, topLeft = Offset(w * 0.58f, 0f), size = Size(w * 0.42f, h * 0.42f), style = Stroke(stroke))
        drawRoundRect(color = color, topLeft = Offset(0f, h * 0.58f), size = Size(w * 0.42f, h * 0.42f), style = Stroke(stroke))
        drawRoundRect(color = color, topLeft = Offset(w * 0.58f, h * 0.58f), size = Size(w * 0.42f, h * 0.42f), style = Stroke(stroke))
    }
}

@Composable
fun IconeTarefas(color: Color, modifier: Modifier = Modifier.size(20.dp)) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val stroke = 1.8.dp.toPx()

        // Desenho da Prancheta (Retângulo Externo)
        drawRoundRect(
            color = color,
            topLeft = Offset(w * 0.1f, h * 0.15f),
            size = Size(w * 0.8f, h * 0.75f),
            style = Stroke(stroke)
        )

        // Suporte superior da prancheta (Clip)
        drawRoundRect(
            color = color,
            topLeft = Offset(w * 0.35f, h * 0.08f),
            size = Size(w * 0.3f, h * 0.12f),
            style = Stroke(stroke)
        )

        // Checkmark perfeitamente centralizado no interior da prancheta
        drawLine(
            color = color,
            start = Offset(w * 0.30f, h * 0.52f),
            end = Offset(w * 0.44f, h * 0.66f),
            strokeWidth = stroke,
            cap = StrokeCap.Round
        )
        drawLine(
            color = color,
            start = Offset(w * 0.44f, h * 0.66f),
            end = Offset(w * 0.70f, h * 0.40f),
            strokeWidth = stroke,
            cap = StrokeCap.Round
        )
    }
}

@Composable
fun IconeMetas(color: Color, modifier: Modifier = Modifier.size(20.dp)) {
    Canvas(modifier = modifier) {
        val center = Offset(size.width / 2, size.height / 2)
        val stroke = 1.8.dp.toPx()
        drawCircle(color = color, radius = size.width * 0.45f, center = center, style = Stroke(stroke))
        drawCircle(color = color, radius = size.width * 0.28f, center = center, style = Stroke(stroke))
        drawCircle(color = color, radius = size.width * 0.12f, center = center)
    }
}

@Composable
fun IconeLembretes(color: Color, modifier: Modifier = Modifier.size(20.dp)) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val stroke = 1.8.dp.toPx()
        val path = Path().apply {
            moveTo(w * 0.5f, h * 0.1f)
            cubicTo(w * 0.25f, h * 0.1f, w * 0.2f, h * 0.5f, w * 0.15f, h * 0.7f)
            lineTo(w * 0.85f, h * 0.7f)
            cubicTo(w * 0.8f, h * 0.5f, w * 0.75f, h * 0.1f, w * 0.5f, h * 0.1f)
            close()
        }
        drawPath(path = path, color = color, style = Stroke(stroke))
        drawCircle(color = color, radius = w * 0.08f, center = Offset(w * 0.5f, h * 0.85f))
    }
}

@Composable
fun IconeClassroom(color: Color, modifier: Modifier = Modifier.size(20.dp)) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val stroke = 1.8.dp.toPx()
        drawRoundRect(color = color, topLeft = Offset(w * 0.05f, h * 0.1f), size = Size(w * 0.9f, h * 0.75f), style = Stroke(stroke))
        drawCircle(color = color, radius = w * 0.1f, center = Offset(w * 0.5f, h * 0.35f))
        drawArc(color = color, startAngle = 180f, sweepAngle = 180f, useCenter = false, topLeft = Offset(w * 0.3f, h * 0.5f), size = Size(w * 0.4f, h * 0.25f), style = Stroke(stroke))
    }
}

@Composable
fun IconeRelatorios(color: Color, modifier: Modifier = Modifier.size(20.dp)) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val stroke = 1.8.dp.toPx()
        drawLine(color = color, start = Offset(w * 0.2f, h * 0.85f), end = Offset(w * 0.2f, h * 0.5f), strokeWidth = stroke * 2, cap = StrokeCap.Round)
        drawLine(color = color, start = Offset(w * 0.5f, h * 0.85f), end = Offset(w * 0.5f, h * 0.2f), strokeWidth = stroke * 2, cap = StrokeCap.Round)
        drawLine(color = color, start = Offset(w * 0.8f, h * 0.85f), end = Offset(w * 0.8f, h * 0.35f), strokeWidth = stroke * 2, cap = StrokeCap.Round)
    }
}

@Composable
fun ThemeToggleIcon(isDark: Boolean, color: Color, modifier: Modifier = Modifier.size(12.dp)) {
    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height

        if (isDark) {
            val center = Offset(width / 2, height / 2)
            drawCircle(
                color = color,
                radius = width * 0.45f,
                center = center
            )
            drawCircle(
                color = Color(0xFF1E1E1E),
                radius = width * 0.38f,
                center = Offset(center.x + width * 0.22f, center.y - height * 0.12f)
            )
        } else {
            val center = Offset(width / 2, height / 2)
            val radius = width * 0.25f
            drawCircle(color = color, radius = radius, center = center)

            val strokeWidth = 1.5.dp.toPx()
            val rayLength = width * 0.15f
            val rayOffset = width * 0.32f

            val angles = listOf(0f, 45f, 90f, 135f, 180f, 225f, 270f, 315f)
            angles.forEach { angle ->
                val rad = angle * (PI / 180.0)
                val start = Offset(
                    (center.x + rayOffset * cos(rad)).toFloat(),
                    (center.y + rayOffset * sin(rad)).toFloat()
                )
                val end = Offset(
                    (center.x + (rayOffset + rayLength) * cos(rad)).toFloat(),
                    (center.y + (rayOffset + rayLength) * sin(rad)).toFloat()
                )
                drawLine(
                    color = color,
                    start = start,
                    end = end,
                    strokeWidth = strokeWidth,
                    cap = StrokeCap.Round
                )
            }
        }
    }
}

@Composable
fun MenuIcon(color: Color, modifier: Modifier = Modifier.size(24.dp)) {
    Canvas(modifier = modifier) {
        val strokeWidth = 2.dp.toPx()
        val ys = listOf(size.height * 0.28f, size.height * 0.5f, size.height * 0.72f)
        ys.forEach { y ->
            drawLine(
                color = color,
                start = Offset(size.width * 0.15f, y),
                end = Offset(size.width * 0.85f, y),
                strokeWidth = strokeWidth,
                cap = StrokeCap.Round
            )
        }
    }
}