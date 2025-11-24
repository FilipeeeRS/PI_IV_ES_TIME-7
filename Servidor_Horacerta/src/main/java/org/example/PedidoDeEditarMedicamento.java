package org.example;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.result.UpdateResult;
import com.mongodb.MongoException;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;

// Importações estáticas necessárias para filtros e updates do MongoDB
import static com.mongodb.client.model.Filters.and;
import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Updates.combine;
import static com.mongodb.client.model.Updates.set;

// Assumindo que ComunicadoJson é uma classe base
public class PedidoDeEditarMedicamento extends ComunicadoJson {
    // Campos que serão populados pelo desserializador (GSON/Jackson)
    private String id; // ID do MongoDB
    private String idUsuario; // ID do usuário para segurança
    private String nome;
    private String dia;
    private String horario;
    private String descricao;
    private boolean tomou;

    // Constante de configuração (SUBSTITUIR PELOS VALORES REAIS)
    private static final String MONGO_URI = "SUA_URI_DO_MONGO";
    private static final String DB_NAME = "SEU_DB_NAME";

    public PedidoDeEditarMedicamento() {
        super("PedidoDeEditarMedicamento");
    }

    /**
     * Getters necessários para que o GSON (ou outro serializador)
     * possa preencher os campos privados se o construtor default for usado,
     * e para fins de debug/log.
     */
    public String getId() { return id; }
    public String getIdUsuario() { return idUsuario; }
    public String getNome() { return nome; }
    public String getDia() { return dia; }
    public String getHorario() { return horario; }
    public String getDescricao() { return descricao; }
    public boolean isTomou() { return tomou; }


    public boolean executar() {
        try {
            // OBS: Use a URI e o nome do DB corretos
            try (MongoClient client = MongoClients.create(MONGO_URI)) {
                MongoDatabase db = client.getDatabase(DB_NAME);
                MongoCollection<Document> collection = db.getCollection("medicamentos");

                ObjectId objectId = new ObjectId(this.id);

                // 1. Define o filtro de segurança usando Bson (Resolve o erro Incompatible types)
                // Retira a necessidade de new Document().append(...)
                Bson filtro = and(
                        eq("_id", objectId),
                        eq("idUsuario", this.idUsuario)
                );

                // 2. Define o update usando Bson (Resolve o erro Incompatible types)
                Bson update = combine(
                        set("nome", this.nome),
                        set("dia", this.dia),
                        set("horario", this.horario),
                        set("descricao", this.descricao),
                        set("tomou", this.tomou)
                );

                // 3. Executa a edição
                UpdateResult result = ((MongoCollection<Document>) collection).updateOne(filtro, update);

                if (result.getMatchedCount() > 0) {
                    System.out.println("[MEDICAMENTO] Editado com sucesso. ID: " + this.id);
                    return true;
                } else {
                    System.err.println("[MEDICAMENTO] Falha ao editar. Documento não encontrado. ID: " + this.id);
                    return false;
                }

            } catch (MongoException e) {
                System.err.println("[ERRO MONGO] Falha na edição do medicamento: " + e.getMessage());
                e.printStackTrace();
            }
        } catch (IllegalArgumentException e) {
            System.err.println("[ERRO] ID do medicamento inválido: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("[ERRO GERAL] Falha na execução da edição: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }
}