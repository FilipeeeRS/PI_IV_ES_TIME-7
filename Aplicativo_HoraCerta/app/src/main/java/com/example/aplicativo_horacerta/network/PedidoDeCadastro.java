package com.example.aplicativo_horacerta.network;

import com.google.gson.annotations.SerializedName; // <--- NOVA IMPORTAÇÃO

public class PedidoDeCadastro extends ComunicadoJson {
    private static final long serialVersionUID = 1L;


    @SerializedName("nome")
    private final String name;


    @SerializedName("email")
    private final String login;


    @SerializedName("firebaseUid")
    private final String firebaseUid;


    @SerializedName("profileType")
    private final String profileType;


    public PedidoDeCadastro(String name, String login, String firebaseUid, String profileType) {
        super("Cadastro");


        if (name == null || name.isBlank())  throw new IllegalArgumentException("Nome inválido");
        if (login == null || login.isBlank())throw new IllegalArgumentException("Email/Login inválido");
        if ( firebaseUid == null || firebaseUid.isBlank())throw new IllegalArgumentException("FirebaseUId inválida");
        if (profileType == null || profileType.isBlank())throw new IllegalArgumentException("Perfil inválido");


        this.name = name;
        this.login = login;
        this.firebaseUid = firebaseUid;
        this.profileType = profileType;

    }

    public String getName()   { return name; }
    public String getLogin()  { return login; }
    public String getFirebaseUId()  { return firebaseUid; }
    public String getProfileType() {return profileType; }
}