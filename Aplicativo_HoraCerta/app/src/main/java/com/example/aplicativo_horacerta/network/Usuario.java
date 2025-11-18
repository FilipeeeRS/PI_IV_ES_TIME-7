package com.example.aplicativo_horacerta.network;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;

// A classe precisa ser Serializable para ser transportada
public class Usuario implements Serializable {
    private static final long serialVersionUID = 1L;

    // Campos que refletem exatamente o que o servidor envia
    @SerializedName("_id")
    private final String idMongoDB;

    @SerializedName("uid")
    private final String firebaseUid; // O UID que você precisa

    @SerializedName("nome")
    private final String nome;

    @SerializedName("email")
    private final String email;

    @SerializedName("tipo")
    private final String profileType; // O Tipo de Perfil (Cuidador/Idoso)


    public Usuario(String idMongoDB, String firebaseUid, String nome, String email, String profileType) {
        this.idMongoDB = idMongoDB;
        this.firebaseUid = firebaseUid;
        this.nome = nome;
        this.email = email;
        this.profileType = profileType;
    }


    public String getUid() { return firebaseUid; }
    public String getTipo() { return profileType; }
    public String getEmail() { return email; }

}