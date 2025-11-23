package org.example;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import com.mongodb.client.result.UpdateResult;
import io.github.cdimascio.dotenv.Dotenv;
import org.bson.Document;
import org.bson.types.ObjectId;

public class PedidoDeEditarMedicamento extends Medicamento {

    public PedidoDeEditarMedicamento() {
        super("PedidoDeEditarMedicamento");
    }

    public PedidoDeEditarMedicamento(String json) {
        super(json);
    }

    /*
      Executa a atualização do medicamento no banco de dados.
      @return true se a atualização foi bem-sucedida, false caso contrário.
    */
    public boolean executarAtualizacao() {
        // Validação básica: ID do medicamento e do usuário.
        if (this.getId() == null || this.getId().isBlank() || this.getIdUsuario() == null || this.getIdUsuario().isBlank()) {
            System.err.println("[UPDATE] ID do medicamento ou ID do usuário não fornecidos.");
            return false;
        }

        // Configuração do MongoDB
        Dotenv dotenv = Dotenv.load();
        String uri = dotenv.get("MONGO_URI");
        String dbName = dotenv.get("MONGO_DATABASE", "sample_horacerta");


        try (MongoClient client = MongoClients.create(uri)) {
            MongoDatabase db = client.getDatabase(dbName);
            MongoCollection<Document> col = db.getCollection("medicamentos");

            // O Filtro: Encontra o documento pelo ID e garante que pertence ao usuário correto
            Document filtro = new Document("_id", new ObjectId(this.getId()))
                    .append("idUsuario", this.getIdUsuario());

            // O Update Set: Cria o objeto de atualização
            // Usamos os getters da superclasse Medicamento para obter os novos valores.
            Document updateFields = new Document();

            // Adicionamos todos os campos que podem ter sido atualizados.
            // O MongoDB só modificará os campos que são diferentes.
            updateFields.append("nome", this.getNome());
            updateFields.append("dia", this.getDia());
            updateFields.append("horario", this.getHorario());
            updateFields.append("descricao", this.getDescricao());
            updateFields.append("tomou", this.isTomou());
            // Não incluímos o 'idUsuario' e o '_id' no set, pois eles não devem mudar.

            Document updateOp = new Document("$set", updateFields);

            System.out.println("[UPDATE] Tentando atualizar o medicamento ID: " + this.getId());

            // Executa a atualização no MongoDB
            UpdateResult resultado = col.updateOne(filtro, updateOp);

            // Verifica o resultado
            if (resultado.getModifiedCount() > 0) {
                System.out.println("[UPDATE] Sucesso. Documentos modificados: " + resultado.getModifiedCount());
                return true; // Atualização bem-sucedida
            } else {
                // Pode ser 0 se o medicamento for encontrado, mas os dados não mudaram.
                // Consideramos sucesso se a contagem for 0, mas pelo menos um documento foi encontrado.
                if (resultado.getMatchedCount() > 0) {
                    System.out.println("[UPDATE] Documento encontrado, mas sem alterações nos dados.");
                    return true;
                }
                System.out.println("[UPDATE] Falha. Nenhum documento correspondente encontrado.");
                return false;
            }

        } catch (IllegalArgumentException e) {
            System.err.println("[UPDATE] Erro de ID: O ID fornecido não é um ObjectId válido. " + e.getMessage());
            return false;
        } catch (Exception e) {
            System.err.println("[UPDATE] Erro inesperado ao interagir com o MongoDB: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
}