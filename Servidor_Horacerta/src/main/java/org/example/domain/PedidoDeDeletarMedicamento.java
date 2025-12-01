package org.example.domain;


import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.result.DeleteResult;
import io.github.cdimascio.dotenv.Dotenv;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.example.domain.result.ResultadoOperacao;
import org.example.protocol.ComunicadoJson;

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

                // Filtro de segurança
                DeleteResult result = collection.deleteOne(
                        and(
                                eq("_id", objectId),
                                eq("idUsuario", this.idUsuario)
                        )
                );

                long count = result.getDeletedCount();

                System.out.println("[MEDICAMENTO] Tentativa de delete. ID: " + this.id + ". Deletados: " + count);

                if (count == 1) {
                    return new ResultadoOperacao(true, "Medicamento deletado com sucesso.").getSucesso();
                } else {
                    return new ResultadoOperacao(false, "Falha ao deletar: Medicamento não encontrado ou permissão negada.").getSucesso();
                }

            } catch (com.mongodb.MongoException e) {
                System.err.println("Erro ao interagir com o MongoDB durante deleção: " + e.getMessage());
                return new ResultadoOperacao(false, "Erro interno do servidor (MongoDB): " + e.getMessage()).getSucesso();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return new ResultadoOperacao(false, "Erro interno inesperado: " + e.getMessage()).getSucesso();
        }
    }
}