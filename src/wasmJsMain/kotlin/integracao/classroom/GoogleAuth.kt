package integracao.classroom

external fun iniciarLoginGoogleClassroom(
    clientId: String,
    scopes: String,
    onToken: (String) -> Unit,
    onErro: (String) -> Unit
)

external fun fetchComToken(
    url: String,
    token: String,
    onSucesso: (String) -> Unit,
    onErro: (String) -> Unit
)