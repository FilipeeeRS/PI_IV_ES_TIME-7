package org.example;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import io.github.cdimascio.dotenv.Dotenv;
import org.bson.Document;
import com.google.gson.annotations.SerializedName;
import java.util.ArrayList;
import java.util.List;

// Assumindo que 'ComunicadoJson' é sua superclasse de comunicação
// e que a resposta ao pedido será uma lista de documentos (medicamentos)
public class PedidoDeListarMedicamentos extends ComunicadoJson {

    @SerializedName("idUsuario")
    private String idUsuario;

    public PedidoDeListarMedicamentos() {
        super("PedidoDeListarMedicamentos");
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

            // 1. Carrega as variáveis de ambiente
            Dotenv dotenv = Dotenv.load();
            String uri = dotenv.get("MONGO_URI");
            String dbName = dotenv.get("MONGO_DATABASE", "sample_horacerta");

            // 2. Conexão e Busca no MongoDB
            try (MongoClient client = MongoClients.create(uri)) {
                MongoDatabase db = client.getDatabase(dbName);
                // Assume que os documentos serão lidos como 'Document' do BSON
                MongoCollection<Document> col = db.getCollection("medicamentos");

                // 3. Executa a consulta filtrando pelo campo 'idUsuario'
                col.find(Filters.eq("idUsuario", this.idUsuario))
                        .into(medicamentosEncontrados); // Coloca todos os documentos na lista

                System.out.println("[LISTAGEM] Encontrados " + medicamentosEncontrados.size() +
                        " medicamentos para o usuário: " + this.idUsuario);

            } catch (com.mongodb.MongoException e) {
                System.err.println("Erro ao interagir com o MongoDB (Conexão/Consulta): " + e.getMessage());
                // Em caso de erro, retorna lista vazia
            }

        } catch (IllegalArgumentException e) {
            System.err.println("Erro de Validação: " + e.getMessage());
            // Em caso de erro, retorna lista vazia
        } catch (Exception e) {
            System.err.println("Erro inesperado ao executar a busca: " + e.getMessage());
            // Em caso de erro, retorna lista vazia
        }

        return medicamentosEncontrados;
    }

    private static boolean isBlank(String s) {
        return s == null || s.isBlank();
    }
}