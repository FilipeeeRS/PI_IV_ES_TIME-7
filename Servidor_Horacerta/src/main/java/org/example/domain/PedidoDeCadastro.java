package org.example.domain;


import com.google.gson.annotations.SerializedName;
import io.github.cdimascio.dotenv.Dotenv;
import com.mongodb.client.*;
import org.bson.Document;
import org.example.protocol.ComunicadoJson;
import java.util.Objects;

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
            if (isBlank(nome) || isBlank(email) || isBlank(firebaseUid) || isBlank(profileType))
                throw new IllegalArgumentException("Campos obrigatórios ausentes");

            Dotenv dotenv = Dotenv.load();
            String uri = dotenv.get("MONGO_URI");
            String dbName = dotenv.get("MONGO_DATABASE", "sample_horacerta");

            try (MongoClient client = MongoClients.create(uri)) {
                MongoDatabase db = client.getDatabase(dbName);
                MongoCollection<Document> col = db.getCollection("usuario");

                Document filtroEmail = new Document("email", this.email);
                if (col.find(filtroEmail).first() != null) {
                    System.err.println("Erro: Email já cadastrado.");
                    return false;
                }

                Document filtroUid = new Document("firebase_uid", this.firebaseUid);
                if (col.find(filtroUid).first() != null) {
                    System.err.println("Erro: UID do Firebase já cadastrado.");
                    return false;
                }

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
            e.printStackTrace();
            return false;
        }

    }

    private static boolean isBlank(String s) {
        return s == null || s.isBlank();
    }

    @Override
    public int hashCode() {
        // Combina o hash do pai com os hash dos quatro campos.
        return Objects.hash(super.hashCode(), nome, email, firebaseUid, profileType);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;

        // Compara o pai primeiro
        if (!super.equals(obj)) return false;

        if (getClass() != obj.getClass()) return false;

        PedidoDeCadastro other = (PedidoDeCadastro) obj;

        // Compara todos os quatro campos de dados.
        return Objects.equals(nome, other.nome) &&
                Objects.equals(email, other.email) &&
                Objects.equals(firebaseUid, other.firebaseUid) &&
                Objects.equals(profileType, other.profileType);
    }

    @Override
    public String toString() {
        // Representação clara para logs. Evita mostrar o UID completo.
        return "PedidoDeCadastro [Perfil: " + profileType + ", Email: " + email + ", Nome: " + nome + "]";
    }
}