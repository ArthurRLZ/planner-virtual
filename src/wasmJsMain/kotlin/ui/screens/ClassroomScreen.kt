package ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import domain.usecase.classroom.SincronizarClassroomUseCase
import integracao.classroom.ClassroomConfig
import integracao.classroom.fetchComToken
import integracao.classroom.iniciarLoginGoogleClassroom

@Composable
fun ClassroomScreen(sincronizarClassroomUseCase: SincronizarClassroomUseCase) {
    var status by remember { mutableStateOf("Desconectado") }
    var sincronizando by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            "Google Classroom",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )

        OutlinedCard(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f))
        ) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text("Status: $status", style = MaterialTheme.typography.bodyMedium)

                Button(
                    enabled = !sincronizando,
                    onClick = {
                        sincronizando = true
                        status = "Conectando..."

                        iniciarLoginGoogleClassroom(
                            clientId = ClassroomConfig.CLIENT_ID,
                            scopes = ClassroomConfig.SCOPES,
                            onToken = { token ->
                                status = "Buscando turmas..."

                                fetchComToken(
                                    url = "https://classroom.googleapis.com/v1/courses?courseStates=ACTIVE",
                                    token = token,
                                    onSucesso = { jsonCursos ->
                                        val cursos = sincronizarClassroomUseCase.processarCursos(jsonCursos)
                                        status = "Sincronizando ${cursos.size} turma(s)..."

                                        cursos.forEach { curso ->
                                            fetchComToken(
                                                url = "https://classroom.googleapis.com/v1/courses/${curso.id}/courseWork",
                                                token = token,
                                                onSucesso = { jsonTarefas ->
                                                    try {
                                                        sincronizarClassroomUseCase.processarESalvarTarefas(jsonTarefas)
                                                        status = "Sincronizado com sucesso!"
                                                    } catch (e: Exception) {
                                                        println("ERRO REAL: ${e.message}")
                                                        println("JSON recebido: $jsonTarefas")
                                                        status = "Erro ao processar: ${e.message}"
                                                    }
                                                    sincronizando = false
                                                },
                                                onErro = { erro ->
                                                    status = "Erro ao buscar tarefas: $erro"
                                                    sincronizando = false
                                                }
                                            )
                                        }
                                    },
                                    onErro = { erro ->
                                        status = "Erro ao buscar turmas: $erro"
                                        sincronizando = false
                                    }
                                )
                            },
                            onErro = { erro ->
                                status = "Erro no login: $erro"
                                sincronizando = false
                            }
                        )
                    }
                ) {
                    if (sincronizando) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                        Spacer(Modifier.width(8.dp))
                    }
                    Text(if (sincronizando) "Sincronizando..." else "Conectar com Google Classroom")
                }
            }
        }
    }
}