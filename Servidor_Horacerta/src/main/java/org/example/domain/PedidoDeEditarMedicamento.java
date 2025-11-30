package org.example.domain;


import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.result.UpdateResult;
import com.mongodb.MongoException;
import io.github.cdimascio.dotenv.Dotenv;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;
import org.example.protocol.ComunicadoJson;

// Importações estáticas necessárias para filtros e updates do MongoDB
import static com.mongodb.client.model.Filters.and;
import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Updates.combine;
import static com.mongodb.client.model.Updates.set;

public class PedidoDeEditarMedicamento extends ComunicadoJson {

    private String idMedicamento;
    private String idUsuario;
    private String nome;
    private String dia;
    private String horario;
    private String descricao;
    private Boolean tomou;

    public PedidoDeEditarMedicamento() {
        super("PedidoDeEditarMedicamento");
    }

    //Getters para que o GSON
    public String getIdMedicamento() { return idMedicamento; }
    public String getIdUsuario() { return idUsuario; }
    public String getNome() { return nome; }
    public String getDia() { return dia; }
    public String getHorario() { return horario; }
    public String getDescricao() { return descricao; }
    public Boolean isTomou() { return tomou; }

    public boolean executar() {
        // Obter variáveis de ambiente
        Dotenv dotenv = Dotenv.load();
        String uri = dotenv.get("MONGO_URI");
        String dbName = dotenv.get("MONGO_DATABASE", "sample_horacerta");

        // Validação inicial
        if (this.idMedicamento == null || this.idMedicamento.isBlank()) {
            System.err.println("[ERRO] Pedido de Edição sem ID do Medicamento. Recebido: " + this.idMedicamento);
            return false;
        }

        // Validação de segurança
        if (this.idUsuario == null || this.idUsuario.isBlank()) {
            System.err.println("[ERRO] Pedido de Edição sem ID do Usuário. Recebido: " + this.idUsuario);
            return false;
        }

        try (MongoClient client = MongoClients.create(uri)) {
            MongoDatabase db = client.getDatabase(dbName);
            MongoCollection<Document> collection = db.getCollection("medicamentos");

            // Converte o ID do medicamento para ObjectId
            ObjectId objectId = new ObjectId(this.idMedicamento);

            // Define o FILTRO de segurança: Id do medicamento E Id do usuário
            Bson filtro = and(
                    eq("_id", objectId),
                    eq("idUsuario", this.idUsuario)
            );

            // Constrói UPDATE, ignorando campos vazios
            java.util.List<Bson> updates = new java.util.ArrayList<>();

            if (nome != null && !nome.isBlank()) {
                updates.add(set("nome", this.nome));
            }
            if (dia != null && !dia.isBlank()) {
                updates.add(set("dia", this.dia));
            }
            if (horario != null && !horario.isBlank()) {
                updates.add(set("horario", this.horario));
            }
            if (descricao != null && !descricao.isBlank()) {
                updates.add(set("descricao", this.descricao));
            }

            // O campo tomou é tratado separado
            if (this.tomou != null) {
                updates.add(set("tomou", this.tomou));
            }

            // Se não houver nenhum campo para atualizar, falha
            if (updates.isEmpty()) {
                System.err.println("[MEDICAMENTO] Falha ao editar. Nenhum campo válido fornecido para atualização.");
                return false;
            }

            // Combina os updates válidos
            Bson update = combine(updates);

            // Executa a edição
            UpdateResult result = collection.updateOne(filtro, update);

            if (result.getMatchedCount() > 0) {
                if (result.getModifiedCount() > 0) {
                    System.out.println("[MEDICAMENTO] Editado com sucesso. ID: " + this.idMedicamento);
                    return true;
                } else {
                    System.out.println("[MEDICAMENTO] Editado, mas nenhum campo foi modificado (dados iguais). ID: " + this.idMedicamento);
                    return true;
                }
            } else {
                System.err.println("[MEDICAMENTO] Falha ao editar. Documento não encontrado ou ID do Usuário não corresponde. ID: " + this.idMedicamento);
                return false;
            }

        } catch (IllegalArgumentException e) {
            System.err.println("[ERRO] ID do medicamento em formato inválido: " + e.getMessage() + ". ID recebido: " + this.idMedicamento);
            e.printStackTrace();
        } catch (MongoException e) {
            System.err.println("[ERRO MONGO] Falha na edição do medicamento: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("[ERRO GERAL] Falha na execução da edição: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }
}
