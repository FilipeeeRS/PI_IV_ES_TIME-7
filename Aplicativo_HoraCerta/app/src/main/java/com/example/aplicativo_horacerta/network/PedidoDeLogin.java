package com.example.aplicativo_horacerta.network;

import com.google.gson.annotations.SerializedName;

public class PedidoDeLogin extends ComunicadoJson {
    private static final long serialVersionUID = 1L;


    @SerializedName("login")
    private String email;

    @SerializedName("firebaseUid")
    private String firebaseUid;


    public PedidoDeLogin() {
        super("Login");
    }

    public PedidoDeLogin(String email, String firebaseUid) {
        super("Login");
        if (email == null || email.isBlank()) throw new IllegalArgumentException("Login (email) inválido");
        // Ajuste a mensagem para refletir que é o UID, não a Senha
        if (firebaseUid == null || firebaseUid.isBlank()) throw new IllegalArgumentException("UID inválido");
        this.email = email.trim().toLowerCase(); // normaliza
        this.firebaseUid = firebaseUid;
    }

    public String getEmail() { return email; }
    public String getFirebaseUid() { return firebaseUid; }
}