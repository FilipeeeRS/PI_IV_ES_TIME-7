package org.example;

import com.google.gson.annotations.SerializedName;
import io.github.cdimascio.dotenv.Dotenv;
import com.mongodb.client.*;
import org.bson.Document;

import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

public class PedidoDeCadastro extends ComunicadoJson {
    // Campos recebidos do cliente
    private String nome;

    @SerializedName("login")  // "login" do JSON vira "email" aqui
    private String email;


    // Gerados automaticamente no servidor
    private String uid;
    private String codigo;

    public PedidoDeCadastro() { super("Cadastro"); }

    private void gerarIds() {
        if (uid == null || uid.isBlank())
            uid = UUID.randomUUID().toString();

        if (codigo == null || codigo.isBlank())
            codigo = String.format("%06d", ThreadLocalRandom.current().nextInt(0, 1_000_000));
    }

    public boolean criarDocumento() {
        try {
            if (isBlank(nome) || isBlank(email) )
                throw new IllegalArgumentException("Campos obrigatórios ausentes");

            gerarIds();

            Dotenv dotenv = Dotenv.load();
            String uri = dotenv.get("MONGO_URI");
            String dbName = dotenv.get("MONGO_DATABASE", "sample_horacerta");

            try (MongoClient client = MongoClients.create(uri)) {
                MongoDatabase db = client.getDatabase(dbName);
                MongoCollection<Document> col = db.getCollection("usuario");

                Document doc = new Document("uid", uid)
                        .append("nome", nome)
                        .append("email", email)
                        .append("codigo", codigo);

                col.insertOne(doc);
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private static boolean isBlank(String s) {
        return s == null || s.isBlank();
    }
}