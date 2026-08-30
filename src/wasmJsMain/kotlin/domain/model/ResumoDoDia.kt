package br.edu.ufapetro.planner.domain.model

import domain.model.Tarefa
import domain.model.Meta
import domain.model.Lembrete
import kotlinx.serialization.Serializable

@Serializable
data class ResumoDoDia(
    val tarefasPendentes: List<Tarefa>,
    val tarefasConcluidas: List<Tarefa>,
    val metasEmAndamento: List<Meta>,
    val proximosLembretes: List<Lembrete>,
    val indicadorProdutividade: Float, // 0.0 a 1.0
    val metasCumpridas: List<Meta>
)