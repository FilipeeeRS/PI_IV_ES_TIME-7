package org.example;

import com.google.gson.annotations.SerializedName;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import io.github.cdimascio.dotenv.Dotenv;
import org.bson.Document;

public class PedidoDeLogin extends ComunicadoJson {
    // O cliente envia "login" (email) e "senha"
    @SerializedName("login")
    private String email;

    public PedidoDeLogin() {
        super("Login");
    }

    public PedidoDeLogin(String email ) {
        super("Login");
        this.email = email;
    }

    public Usuario getUserData() {
        // Normaliza entrada
        this.email = (this.email == null) ? null : this.email.trim().toLowerCase();
        if (isBlank(this.email) ) {
            System.out.println("[LOGIN] Email ou senha em branco!");
            return null;
        }

        Dotenv dotenv = Dotenv.load();
        String uri    = dotenv.get("MONGO_URI");
        String dbName = dotenv.get("MONGO_DATABASE", "sample_horacerta");

        System.out.println("[LOGIN] db=" + dbName + " email=" + this.email);

        try (MongoClient client = MongoClients.create(uri)) {
            MongoDatabase db = client.getDatabase(dbName);
            MongoCollection<Document> col = db.getCollection("usuario");

            // Log antes da busca
            System.out.println("[LOGIN] Procurando usuário com email=" + this.email);

            Document doc = col.find(Filters.eq("email", this.email)).first();

            // Log do resultado da busca
            System.out.println("[LOGIN] Documento encontrado? " + (doc != null));

            if (doc == null) return null; // e-mail não encontrado


            System.out.println("[LOGIN] Usuário autenticado com sucesso!");
            return new Usuario(
                    doc.getObjectId("_id").toHexString(),
                    doc.getString("uid"),
                    doc.getString("nome"),
                    doc.getString("email")
            );
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public String toString() {
        return "Login de: " + this.email;
    }

    private static boolean isBlank(String s) {
        return s == null || s.isBlank();
    }
}