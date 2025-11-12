

package com.example.aplicativo_horacerta.network;

// Esta classe será a resposta que o GSON desserializa do servidor.
public class ResultadoLogin extends ComunicadoJson {
    private static final long serialVersionUID = 1L;

    private final boolean isSuccessful;
    private final String mensagem;
    private final String firebaseUid; // O servidor deve preencher
    private final String profileType; // O servidor deve preencher

    // O GSON precisa deste construtor para desserializar
    public ResultadoLogin(boolean isSuccessful, String mensagem, String firebaseUid, String profileType) {
        super("ResultadoLogin"); // Ou o nome da operação
        this.isSuccessful = isSuccessful;
        this.mensagem = mensagem;
        this.firebaseUid = firebaseUid;
        this.profileType = profileType;
    }

    // Getters
    public boolean isSuccessful() { return isSuccessful; }
    public String getMensagem() { return mensagem; }
    public String getFirebaseUid() { return firebaseUid; }
    public String getProfileType() { return profileType; }
}