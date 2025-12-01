package org.example.domain;


import com.google.gson.annotations.SerializedName;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import io.github.cdimascio.dotenv.Dotenv;
import org.bson.Document;
import org.example.protocol.ComunicadoJson;
import java.util.Objects;


public class PedidoDeLogin extends ComunicadoJson {

    @SerializedName("login")
    private String email;

    @SerializedName("firebaseUid")
    private String firebaseUid;

    public PedidoDeLogin() {
        super("Login");
    }

    public PedidoDeLogin(String email, String firebaseUid) {
        super("Login");
        this.email = email;
        this.firebaseUid = firebaseUid;
    }

    public Usuario getUserData() {
        // Normaliza entrada
        this.email = (this.email == null) ? null : this.email.trim().toLowerCase();

        if (isBlank(this.email) || isBlank(this.firebaseUid)) {
            System.out.println("[LOGIN] Email ou UID em branco!");
            return null;
        }

        Dotenv dotenv = Dotenv.load();
        String uri    = dotenv.get("MONGO_URI");
        String dbName = dotenv.get("MONGO_DATABASE", "sample_horacerta");

        System.out.println("[LOGIN] db=" + dbName + " email=" + this.email);

        try (MongoClient client = MongoClients.create(uri)) {
            MongoDatabase db = client.getDatabase(dbName);
            MongoCollection<Document> col = db.getCollection("usuario");

            System.out.println("[LOGIN] Procurando usuário com email=" + this.email);


            Document doc = col.find(Filters.eq("email", this.email)).first();

            System.out.println("[LOGIN] Documento encontrado? " + (doc != null));

            if (doc == null) return null; // e-mail não encontrado

            String uidBanco = doc.getString("firebase_uid");

            if (uidBanco == null || !uidBanco.equals(this.firebaseUid)) {
                System.out.println("[LOGIN] UID do banco (" + uidBanco + ") não corresponde ao UID enviado (" + this.firebaseUid + ")");
                return null;
            }

            System.out.println("[LOGIN] Usuário autenticado com sucesso!");

            return new Usuario(
                    doc.getObjectId("_id").toHexString(),
                    doc.getString("firebase_uid"),
                    doc.getString("nome"),
                    doc.getString("email"),
                    doc.getString("profileType")
            );
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

    }

    @Override
    public String toString() {
        // Útil para logs: mostra o email e o UID (o ID do Firebase)
        return "PedidoDeLogin [Tipo: " + getTipo() +
                ", Email: " + this.email +
                ", UID: " + this.firebaseUid + "]";
    }

    private static boolean isBlank(String s) {
        return s == null || s.isBlank();
    }

    @Override
    public int hashCode() {
        // Hash baseado nos campos que definem o pedido: email e firebaseUid.
        return Objects.hash(email, firebaseUid);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;

        PedidoDeLogin other = (PedidoDeLogin) obj;

        // Compara os campos de dados email e firebaseUid
        return Objects.equals(email, other.email) &&
                Objects.equals(firebaseUid, other.firebaseUid);
    }
}
