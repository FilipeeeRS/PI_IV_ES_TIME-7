package org.example;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import io.github.cdimascio.dotenv.Dotenv; // Novo import
import org.bson.Document;
import org.bson.types.ObjectId;
// Os imports de static que você já tem...
import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Updates.combine;
import static com.mongodb.client.model.Updates.set;

public class PedidoDeEditarMedicamento extends ComunicadoJson {

    public PedidoDeEditarMedicamento() {
        super("PedidoDeEditarMedicamento");
    }

    public boolean executar() {
        try {
            Dotenv dotenv = Dotenv.load();
            String uri = dotenv.get("MONGO_URI");
            String dbName = dotenv.get("MONGO_DATABASE", "sample_horacerta");

            // Abre a conexão
            try (MongoClient client = MongoClients.create(uri)) {
                MongoDatabase db = client.getDatabase(dbName);
                MongoCollection<Document> collection = db.getCollection("medicamentos");
                // ----------------------------------------------------------------

                ObjectId objectId = new ObjectId(this.id);

                // Atualiza o documento no Mongo
                collection.updateOne(
                        eq("_id", objectId), // Filtro
                        combine(
                                set("nome", this.nome),
                                set("dosagem", this.dosagem),
                                set("tomou", this.tomou) // Atualização do status
                        )
                );

                System.out.println("[MEDICAMENTO] Editado com sucesso. ID: " + this.id);
                return true;
            } catch (com.mongodb.MongoException e) {
                System.err.println("Erro ao interagir com o MongoDB durante edição: " + e.getMessage());
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}