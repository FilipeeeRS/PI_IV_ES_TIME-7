package com.example.aplicativo_horacerta.network;

import com.google.gson.annotations.SerializedName;

public class PedidoDeLogin extends ComunicadoJson {
    private static final long serialVersionUID = 1L;

    // O cliente manda "login" (email). Mapeamos para o campo email.
    @SerializedName("login")
    private String login;

    private String senha;

    // Construtor sem args: necessário para o Gson
    public PedidoDeLogin() {
        super("Login");
    }

    public PedidoDeLogin(String login, String senha) {
        super("Login");
        if (login == null || login.isBlank()) throw new IllegalArgumentException("Login (email) inválido");
        if (senha == null || senha.isBlank()) throw new IllegalArgumentException("Senha inválida");
        this.login = login.trim().toLowerCase(); // normaliza
        this.senha = senha;
    }

    public String getEmail() { return login; }
    public String getSenha() { return senha; }
}