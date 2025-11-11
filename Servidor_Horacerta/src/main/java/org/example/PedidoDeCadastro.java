package org.example;

import com.google.gson.annotations.SerializedName;
import io.github.cdimascio.dotenv.Dotenv;
import com.mongodb.client.*;
import org.bson.Document;

public class PedidoDeCadastro extends ComunicadoJson {


    @SerializedName("nome")
    private String nome;


    @SerializedName("email")
    private String email;


    @SerializedName("firebaseUid")
    private String firebaseUid;


    @SerializedName("profileType")
    private String profileType;

    public PedidoDeCadastro() {
        super("Cadastro"); // Garante que a operação seja identificada
    }


    public String getNome() { return this.nome; }


    public String getEmail() { return this.email; }


    public String getFirebaseUid() { return this.firebaseUid; }


    public String getProfileType() { return this.profileType; }



    public boolean criarDocumento() {
        try {
            // Validação consolidada: Se algum campo for nulo/vazio, lança exceção.
            if (isBlank(nome) || isBlank(email) || isBlank(firebaseUid) || isBlank(profileType))
                throw new IllegalArgumentException("Campos obrigatórios ausentes");


            Dotenv dotenv = Dotenv.load();
            String uri = dotenv.get("MONGO_URI");
            String dbName = dotenv.get("MONGO_DATABASE", "sample_horacerta");

            try (MongoClient client = MongoClients.create(uri)) {
                MongoDatabase db = client.getDatabase(dbName);
                MongoCollection<Document> col = db.getCollection("usuario");


                Document doc = new Document("firebase_uid", this.firebaseUid)
                        .append("nome", nome)
                        .append("email", email)
                        .append("profileType", profileType);

                col.insertOne(doc);
                return true;
            }
        } catch (IllegalArgumentException e) {

            System.err.println("Erro de Validação: " + e.getMessage());
            return false;
        } catch (Exception e) {
            // Trata erros de MongoDB ou conexão
            e.printStackTrace();
            return false;
        }
    }



    private static boolean isBlank(String s) {
        return s == null || s.isBlank();
    }
}