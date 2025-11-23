package org.example;

import com.google.gson.annotations.SerializedName;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.model.Filters;
import io.github.cdimascio.dotenv.Dotenv;
import org.bson.Document;
import org.bson.types.ObjectId;

public class PedidoDeDeletarMedicamento extends ComunicadoJson {
    private static final long serialVersionUID = 1L;

    // O ID do medicamento é o campo crucial para a exclusão
    @SerializedName("id")
    private String idMedicamento;

    // O ID do usuário garante que ele só possa excluir seus próprios medicamentos
    @SerializedName("idUsuario")
    private String idUsuario;

    // Construtor padrão
    public PedidoDeDeletarMedicamento() {
        super("PedidoDeExcluirMedicamento");
    }

    // Construtor para casos em que o JSON precisa ser passado
    public PedidoDeDeletarMedicamento(String json) {
        super(json);
    }

    // Métod0 de processamento de exclusão
    public boolean executarExclusao() {
        if (idMedicamento == null || idMedicamento.isBlank() || idUsuario == null || idUsuario.isBlank()) {
            System.err.println("[EXCLUSÃO] ID do medicamento ou ID do usuário não fornecidos.");
            return false;
        }

        // Configuração do MongoDB
        Dotenv dotenv = Dotenv.load();
        String uri = dotenv.get("MONGO_URI");
        String dbName = dotenv.get("MONGO_DATABASE", "sample_horacerta");


        try (MongoClient client = MongoClients.create(uri)) {
            MongoDatabase db = client.getDatabase(dbName);
            MongoCollection<Document> col = db.getCollection("medicamentos");

            // Cria um filtro:
            //    - O _id do documento deve ser o idMedicamento (convertido para ObjectId)
            //    - O campo idUsuario deve ser o idUsuario fornecido
            Document filtro = new Document("_id", new ObjectId(this.idMedicamento))
                    .append("idUsuario", this.idUsuario);

            System.out.println("[EXCLUSÃO] Tentando excluir o medicamento ID: " + this.idMedicamento +
                    " para o usuário: " + this.idUsuario);

            // Executa a exclusão no MongoDB
            DeleteResult resultado = col.deleteOne(filtro);

            // Verifica o resultado
            if (resultado.getDeletedCount() > 0) {
                System.out.println("[EXCLUSÃO] Sucesso. Documentos excluídos: " + resultado.getDeletedCount());
                return true; // Exclusão bem-sucedida
            } else {
                System.out.println("[EXCLUSÃO] Falha. Nenhum documento correspondente encontrado ou excluído.");
                return false; // Falha na exclusão
            }

        } catch (IllegalArgumentException e) {
            System.err.println("[EXCLUSÃO] Erro de ID: O ID fornecido não é um ObjectId válido. " + e.getMessage());
            return false;
        } catch (Exception e) {
            System.err.println("[EXCLUSÃO] Erro inesperado ao interagir com o MongoDB: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public String toString() {
        return "PedidoDeDeletarMedicamento{" + "idMedicamento='" + idMedicamento + '\'' + '}';
    }
}