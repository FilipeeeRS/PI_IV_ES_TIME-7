package org.example;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.result.DeleteResult;
import io.github.cdimascio.dotenv.Dotenv;
import org.bson.Document;
import org.bson.types.ObjectId;
import static com.mongodb.client.model.Filters.and;
import static com.mongodb.client.model.Filters.eq;

public class PedidoDeDeletarMedicamento extends ComunicadoJson {

    private String id;
    private String idUsuario;
    public PedidoDeDeletarMedicamento() {
        super("PedidoDeDeletarMedicamento");
    }

    public boolean executar() {
        try {
            Dotenv dotenv = Dotenv.load();
            String uri = dotenv.get("MONGO_URI");
            String dbName = dotenv.get("MONGO_DATABASE", "sample_horacerta");

            try (MongoClient client = MongoClients.create(uri)) {
                MongoDatabase db = client.getDatabase(dbName);
                MongoCollection<Document> collection = db.getCollection("medicamentos");

                ObjectId objectId = new ObjectId(this.id);

                // FILTRO DE SEGURANÇA (Garante que o usuário só deleta o que é dele)
                DeleteResult result = collection.deleteOne(
                        and(
                                eq("_id", objectId),         // Filtro 1: ID do Remédio
                                eq("idUsuario", this.idUsuario) // Filtro 2: ID do Usuário Logado
                        )
                );

                long count = result.getDeletedCount();

                System.out.println("[MEDICAMENTO] Tentativa de delete. ID: " + this.id + ". Deletados: " + count);

                // Retorna true se exatamente um documento foi deletado (sucesso)
                return count == 1;

            } catch (com.mongodb.MongoException e) {
                System.err.println("Erro ao interagir com o MongoDB durante deleção: " + e.getMessage());
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}