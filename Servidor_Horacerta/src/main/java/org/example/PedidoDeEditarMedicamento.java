package org.example;

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

// Importações estáticas necessárias para filtros e updates do MongoDB
import static com.mongodb.client.model.Filters.and;
import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Updates.combine;
import static com.mongodb.client.model.Updates.set;

// Assumindo que ComunicadoJson é uma classe base
public class PedidoDeEditarMedicamento extends ComunicadoJson {

    // 🚨 CORREÇÃO 1: Renomeado o campo para idMedicamento para bater com o cliente.
    private String idMedicamento; // ID do MongoDB (o que o cliente envia)
    private String idUsuario; // ID do usuário para segurança
    private String nome;
    private String dia;
    private String horario;
    private String descricao;
    private Boolean tomou; // Usar Boolean (wrapper) para permitir null se não for enviado

    public PedidoDeEditarMedicamento() {
        super("PedidoDeEditarMedicamento");
    }

    /**
     * Getters necessários para que o GSON (ou outro serializador)
     * possa preencher os campos privados.
     */
    public String getIdMedicamento() { return idMedicamento; }
    public String getIdUsuario() { return idUsuario; }
    public String getNome() { return nome; }
    public String getDia() { return dia; }
    public String getHorario() { return horario; }
    public String getDescricao() { return descricao; }
    public Boolean isTomou() { return tomou; }


    // 🚨 O método executar deve retornar um Comunicado. Assumindo que o retorno booleano
    // é um simplificação, aqui ele retorna um boolean para seguir seu código.
    public boolean executar() {
        // Obter variáveis de ambiente (MELHORIA: Mover esta lógica para um Singleton de conexão)
        Dotenv dotenv = Dotenv.load();
        String uri = dotenv.get("MONGO_URI");
        String dbName = dotenv.get("MONGO_DATABASE", "sample_horacerta");

        // 🚨 Validação inicial para evitar a exceção 'hexString can not be null'
        if (this.idMedicamento == null || this.idMedicamento.isBlank()) {
            System.err.println("[ERRO] Pedido de Edição sem ID do Medicamento. Recebido: " + this.idMedicamento);
            return false;
        }

        // 🚨 Validação de segurança: o usuário deve ser fornecido
        if (this.idUsuario == null || this.idUsuario.isBlank()) {
            System.err.println("[ERRO] Pedido de Edição sem ID do Usuário. Recebido: " + this.idUsuario);
            return false;
        }

        try (MongoClient client = MongoClients.create(uri)) {
            MongoDatabase db = client.getDatabase(dbName);
            MongoCollection<Document> collection = db.getCollection("medicamentos");

            // Tenta converter o ID do medicamento para ObjectId
            ObjectId objectId = new ObjectId(this.idMedicamento);

            // 1. Define o FILTRO de segurança: Id do medicamento E Id do usuário
            Bson filtro = and(
                    eq("_id", objectId),
                    eq("idUsuario", this.idUsuario)
            );

            // 2. 🚨 CORREÇÃO 2: Constrói o UPDATE dinamicamente, ignorando campos nulos ou vazios
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

            // O campo 'tomou' (boolean) deve ser tratado separadamente se for enviado
            if (this.tomou != null) {
                updates.add(set("tomou", this.tomou));
            }

            // Se não houver nenhum campo para atualizar, falha a edição.
            if (updates.isEmpty()) {
                System.err.println("[MEDICAMENTO] Falha ao editar. Nenhum campo válido fornecido para atualização.");
                return false;
            }

            // Combina os updates válidos
            Bson update = combine(updates);

            // 3. Executa a edição
            UpdateResult result = collection.updateOne(filtro, update);

            if (result.getMatchedCount() > 0) {
                if (result.getModifiedCount() > 0) {
                    System.out.println("[MEDICAMENTO] Editado com sucesso. ID: " + this.idMedicamento);
                    return true; // Sucesso na modificação
                } else {
                    System.out.println("[MEDICAMENTO] Editado, mas nenhum campo foi modificado (dados iguais). ID: " + this.idMedicamento);
                    return true; // Consideramos sucesso (o estado desejado foi alcançado)
                }
            } else {
                System.err.println("[MEDICAMENTO] Falha ao editar. Documento não encontrado ou ID do Usuário não corresponde. ID: " + this.idMedicamento);
                return false;
            }

        } catch (IllegalArgumentException e) {
            // Captura erros de formato de ObjectId (ex: "abc" não é um ID válido)
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