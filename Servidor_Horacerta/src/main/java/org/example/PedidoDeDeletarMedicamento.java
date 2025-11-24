package org.example;

import org.bson.types.ObjectId;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import static com.mongodb.client.model.Filters.eq;

public class PedidoDeDeletarMedicamento extends Comunicado {

    private String id; // O ID que vem do Android

    public boolean executar() {
        try {
            // --- ATENÇÃO AQUI ---
            // Use a mesma conexão do "PedidoDeCriarMedicamento"
            MongoDatabase database = new Conexao().getBanco(); // AJUSTE ESSA LINHA
            MongoCollection<Document> collection = database.getCollection("medicamentos");
            // --------------------

            ObjectId objectId = new ObjectId(this.id);

            collection.deleteOne(eq("_id", objectId));
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}