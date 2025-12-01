package org.example.domain;


import com.google.gson.annotations.SerializedName;
import com.mongodb.client.*;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import io.github.cdimascio.dotenv.Dotenv;
import org.bson.Document;
import org.example.protocol.ComunicadoJson;
import java.util.Objects;

public class PedidoDeConexao extends ComunicadoJson {

    @SerializedName("emailCuidador")
    private String emailCuidador;

    @SerializedName("emailIdoso")
    private String emailIdoso;

    // Construtor padrão para o GSON
    public PedidoDeConexao() {
        super("ConectarIdoso");
    }

    // Construtor para criar o objeto
    public PedidoDeConexao(String emailCuidador, String emailIdoso) {
        super("ConectarIdoso");
        this.emailCuidador = emailCuidador;
        this.emailIdoso = emailIdoso;
    }

    // Construtor de copia
    public PedidoDeConexao(PedidoDeConexao outro) {
        // 1. Chama o construtor do pai (ComunicadoJson)
        super(outro.getTipo());

        // 2. Copia os campos chaves
        this.emailCuidador = outro.emailCuidador;
        this.emailIdoso = outro.emailIdoso;
    }

    // Métod0 que faz a mágica no Banco (Seguindo o padrão do seu grupo)
    public boolean realizarVinculo() {
        // Validações básicas
        if (emailCuidador == null || emailCuidador.isBlank() || emailIdoso == null || emailIdoso.isBlank()) {
            System.err.println("[CONEXAO] Emails inválidos.");
            return false;
        }

        // Conexão com o Banco (Cópia do padrão do seu Login)
        Dotenv dotenv = Dotenv.load();
        String uri = dotenv.get("MONGO_URI");
        String dbName = dotenv.get("MONGO_DATABASE", "sample_horacerta");

        try (MongoClient client = MongoClients.create(uri)) {
            MongoDatabase db = client.getDatabase(dbName);
            MongoCollection<Document> col = db.getCollection("usuario");

            // Buscar o Idoso
            System.out.println("[CONEXAO] Buscando idoso: " + emailIdoso);
            Document docIdoso = col.find(Filters.eq("email", emailIdoso)).first();

            if (docIdoso == null) {
                System.err.println("[CONEXAO] Idoso não encontrado.");
                return false;
            }

            // Verifica se é mesmo um idoso
            String tipoPerfil = docIdoso.getString("profileType");
            if (tipoPerfil == null || !tipoPerfil.equalsIgnoreCase("Idoso")) {
                System.err.println("[CONEXAO] O email informado não pertence a um perfil de Idoso.");
                return false;
            }

            // Buscar o Cuidador (para pegar o ID dele)
            System.out.println("[CONEXAO] Buscando cuidador: " + emailCuidador);
            Document docCuidador = col.find(Filters.eq("email", emailCuidador)).first();

            if (docCuidador == null) {
                System.err.println("[CONEXAO] Cuidador solicitante não encontrado.");
                return false;
            }

            String uidCuidador = docCuidador.getString("firebase_uid");
            String uidIdoso = docIdoso.getString("firebase_uid");

            // Fazer vínculo (Atualizar o documento do Idoso) ID do cuidador dentro do Idoso
            System.out.println("[CONEXAO] Vinculando Idoso " + uidIdoso + " ao Cuidador " + uidCuidador);

            col.updateOne(
                    Filters.eq("email", emailIdoso),
                    Updates.set("cuidador_responsavel_uid", uidCuidador)
            );

            // Atualizar o Cuidador também
            col.updateOne(
                    Filters.eq("email", emailCuidador),
                    Updates.set("idoso_monitorado_uid", uidIdoso)
            );

            return true;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    @Override
    public int hashCode() {
        // Combina o hash do pai com os hash dos dois campos chaves.
        return Objects.hash(super.hashCode(), emailCuidador, emailIdoso);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;

        if (!super.equals(obj)) return false;

        if (getClass() != obj.getClass()) return false;

        PedidoDeConexao other = (PedidoDeConexao) obj;

        // Compara os dois e-mails.
        return Objects.equals(emailCuidador, other.emailCuidador) &&
                Objects.equals(emailIdoso, other.emailIdoso);
    }
    @Override
    public String toString() {
        // Representação clara para logs.
        return "PedidoDeConexao [Cuidador: " + emailCuidador + ", Vinculando Idoso: " + emailIdoso + "]";
    }
}