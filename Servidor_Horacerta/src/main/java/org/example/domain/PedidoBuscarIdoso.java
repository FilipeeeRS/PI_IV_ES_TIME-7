package org.example.domain;


import com.google.gson.annotations.SerializedName;
import com.mongodb.client.*;
import com.mongodb.client.model.Filters;
import org.bson.Document;
import org.example.protocol.ComunicadoJson;
import java.util.Objects;

public class PedidoBuscarIdoso extends ComunicadoJson {

    @SerializedName("email")
    private String email;

    public PedidoBuscarIdoso() {
        super("BuscarIdoso");
    }

    public String getEmail() {
        return email;
    }

    public String procurarNomeNoBanco() {
        // Ver se o métod0 foi chamado e se o email chegou
        System.out.println("[BUSCA] Iniciando busca...");
        System.out.println("[BUSCA] Email recebido do Android: " + this.email);

        if (email == null || email.isBlank()) {
            System.out.println("[BUSCA] ERRO: Email veio nulo ou vazio!");
            return null;
        }

        String uri = "mongodb+srv://bestTeam:bestforever@cluster0.s3qlsah.mongodb.net/?retryWrites=true&w=majority&tls=true&appName=Cluster0";
        String dbName = "sample_horacerta";

        try (MongoClient client = MongoClients.create(uri)) {
            System.out.println("[BUSCA] Conectado ao MongoDB Atlas.");
            MongoDatabase db = client.getDatabase(dbName);
            MongoCollection<Document> col = db.getCollection("usuario");

            // Confirma o filtro
            String regexPattern = "^" + java.util.regex.Pattern.quote(this.email) + "$";
            System.out.println("[BUSCA] Usando filtro Regex: " + regexPattern);

            Document doc = col.find(Filters.regex("email", regexPattern, "i")).first();

            // Ver o que o banco devolveu
            if (doc == null) {
                System.out.println("[BUSCA] NENHUM documento encontrado para este email.");
                return null;
            } else {
                System.out.println("[BUSCA] Documento encontrado! ID: " + doc.getObjectId("_id"));
                System.out.println("[BUSCA] ProfileType no banco: " + doc.getString("profileType"));
            }

            String tipo = doc.getString("profileType");

            if (tipo != null && tipo.equalsIgnoreCase("Idoso")) {
                String nome = doc.getString("nome");
                String uidCuidadorAtual = doc.getString("cuidador_responsavel_uid");

                System.out.println("[BUSCA] É Idoso. Nome: " + nome);

                if (uidCuidadorAtual != null && !uidCuidadorAtual.isEmpty()) {
                    System.out.println("[BUSCA] Status: Já tem cuidador.");
                    return nome + " (JÁ POSSUI CUIDADOR)";
                } else {
                    System.out.println("[BUSCA] Status: Livre.");
                    return nome;
                }
            } else {
                System.out.println("[BUSCA] ERRO: Usuário encontrado, mas NÃO é Idoso.");
            }
            return null;
        } catch (Exception e) {
            System.err.println("[BUSCA] EXCEÇÃO/ERRO: ");
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public int hashCode() {
        // Combina o hash do pai com o hash do campo de dados.
        return Objects.hash(super.hashCode(), email);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;

        // Compara o pai primeiro
        if (!super.equals(obj)) return false;

        if (getClass() != obj.getClass()) return false;

        PedidoBuscarIdoso other = (PedidoBuscarIdoso) obj;

        // Compara o campo de dados.
        return Objects.equals(email, other.email);
    }

    @Override
    public String toString() {
        // Representação clara para logs.
        return "PedidoBuscarIdoso [Email Alvo: " + email + "]";
    }
}