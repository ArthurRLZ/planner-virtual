window.iniciarLoginGoogleClassroom = function (clientId, scopes, onToken, onErro) {
    try {
        const client = google.accounts.oauth2.initTokenClient({
            client_id: clientId,
            scope: scopes,
            callback: (tokenResponse) => {
                if (tokenResponse.access_token) {
                    onToken(tokenResponse.access_token);
                } else {
                    onErro("Não foi possível obter o token de acesso.");
                }
            }
        });
        client.requestAccessToken();
    } catch (e) {
        onErro(String(e));
    }
};

window.fetchComToken = function (url, token, onSucesso, onErro) {
    fetch(url, { headers: { "Authorization": "Bearer " + token } })
        .then(res => res.json())
        .then(json => onSucesso(JSON.stringify(json)))
        .catch(err => onErro(String(err)));
};