package integracao.classroom

object ClassroomConfig {
    const val CLIENT_ID = "SEU_CLIENT_ID_AQUI.apps.googleusercontent.com"

    // Escopos corretos da API do Classroom (o que você escreveu no prompt,
    // 'https://googleapis.com', não é um escopo válido)
    const val SCOPES =
        "https://www.googleapis.com/auth/classroom.courses.readonly " +
                "https://www.googleapis.com/auth/classroom.coursework.me.readonly"
}