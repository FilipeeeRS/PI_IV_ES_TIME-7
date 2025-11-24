package org.example;
// ... imports ...
import static com.mongodb.client.model.Filters.and; // Necessário para o filtro de segurança
// ...

public class PedidoDeEditarMedicamento extends ComunicadoJson {

    private String id;
    private String idUsuario;
    private String nome;
    private String dia;
    private String horario;
    private String descricao;
    private boolean tomou;


    public PedidoDeEditarMedicamento() {
        super("PedidoDeEditarMedicamento");
    }

    public boolean executar() {
        try {
            // ... (código de conexão com o Mongo) ...

            try (MongoClient client = MongoClients.create(uri)) {
                MongoDatabase db = client.getDatabase(dbName);
                MongoCollection<Document> collection = db.getCollection("medicamentos");

                ObjectId objectId = new ObjectId(this.id);

                // edita no banco de dados
                collection.updateOne(
                        and(
                                eq("_id", objectId),
                                eq("idUsuario", this.idUsuario)
                        ),
                        combine(
                                set("nome", this.nome),
                                set("dia", this.dia),
                                set("horario", this.horario),
                                set("descricao", this.descricao),
                                set("tomou", this.tomou)
                        )
                );

                System.out.println("[MEDICAMENTO] Editado com sucesso. ID: " + this.id);
                return true;
            } catch (com.mongodb.MongoException e) {
                // ...
            }
        } catch (Exception e) {
            // ...
        }
        return false;
    }
}