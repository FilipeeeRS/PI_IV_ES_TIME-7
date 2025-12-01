package org.example.domain;


import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
// import io.github.cdimascio.dotenv.Dotenv; // <--- Comentei pra usar direto
import org.bson.Document;
import com.google.gson.annotations.SerializedName;
import org.example.protocol.ComunicadoJson;
import java.util.Objects;
import java.util.ArrayList;
import java.util.List;

public class PedidoDeListarMedicamentos extends ComunicadoJson {

    @SerializedName("idUsuario")
    private String idUsuario;

    public PedidoDeListarMedicamentos() {
        super("PedidoDeListarMedicamentos");
    }

    public PedidoDeListarMedicamentos(PedidoDeListarMedicamentos outro) {
        super(outro.getTipo());

        this.idUsuario = outro.idUsuario;
    }

    public String getIdUsuario() {
        return this.idUsuario;
    }

    public List<Document> executar() {

        List<Document> medicamentosEncontrados = new ArrayList<>();

        try {
            if (isBlank(idUsuario)) {
                throw new IllegalArgumentException("O ID do Usuário é obrigatório para listar os medicamentos.");
            }

            System.out.println("------------------------------------------------");
            System.out.println("[LISTAR] Recebi pedido para o ID: '" + this.idUsuario + "'");
            System.out.println("[LISTAR] Tamanho da string ID: " + this.idUsuario.length());

            // Dotenv dotenv = Dotenv.load();
            // String uri = dotenv.get("MONGO_URI");
            String uri = "mongodb+srv://bestTeam:bestforever@cluster0.s3qlsah.mongodb.net/?retryWrites=true&w=majority&tls=true&appName=Cluster0";
            String dbName = "sample_horacerta"; // Confirme se o nome do banco é esse mesmo no seu Mongo Compass

            try (MongoClient client = MongoClients.create(uri)) {
                MongoDatabase db = client.getDatabase(dbName);
                MongoCollection<Document> col = db.getCollection("medicamentos");

                // Remove espaços do começo e fim do ID antes de buscar
                col.find(Filters.eq("idUsuario", this.idUsuario.trim()))
                        .into(medicamentosEncontrados);

                System.out.println("[LISTAR] Sucesso! Encontrados " + medicamentosEncontrados.size() +
                        " medicamentos para este usuário.");
                System.out.println("------------------------------------------------");

            } catch (com.mongodb.MongoException e) {
                System.err.println("Erro MongoDB: " + e.getMessage());
            }

        } catch (IllegalArgumentException e) {
            System.err.println("Erro de Validação: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Erro inesperado: " + e.getMessage());
            e.printStackTrace();
        }

        return medicamentosEncontrados;
    }

    private static boolean isBlank(String s) {
        return s == null || s.isBlank();
    }

    @Override
    public int hashCode() {
        // Combina o hash do pai com o hash do campo de dados.
        return Objects.hash(super.hashCode(), idUsuario);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;

        // Compara o pai primeiro
        if (!super.equals(obj)) return false;

        if (getClass() != obj.getClass()) return false;

        PedidoDeListarMedicamentos other = (PedidoDeListarMedicamentos) obj;

        // Compara o campo chave.
        return Objects.equals(idUsuario, other.idUsuario);
    }

    @Override
    public String toString() {
        return "PedidoDeListarMedicamentos [ID Usuário: " + idUsuario + "]";
    }
}
