package org.example;

import com.google.gson.annotations.SerializedName;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.result.UpdateResult;
import io.github.cdimascio.dotenv.Dotenv;
import org.bson.Document;
import org.bson.conversions.Bson;

import static com.mongodb.client.model.Filters.and;
import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Updates.set;

public class PedidoDeConfirmarAlarme extends ComunicadoJson {

    // O Android vai enviar esses dados quando o idoso clicar em "Já Tomei"
    @SerializedName("idUsuario")
    private String idUsuario;

    @SerializedName("nomeRemedio")
    private String nomeRemedio;

    @SerializedName("dia")
    private String dia;

    @SerializedName("horario")
    private String horario;

    public PedidoDeConfirmarAlarme() {
        super("ConfirmarAlarme");
    }

    // Getters
    public String getIdUsuario() { return idUsuario; }
    public String getNomeRemedio() { return nomeRemedio; }
    public String getDia() { return dia; }
    public String getHorario() { return horario; }

    public boolean executar() {
        // Validação básica
        if (idUsuario == null || nomeRemedio == null) {
            System.err.println("[ERRO] Pedido de confirmação incompleto.");
            return false;
        }

        Dotenv dotenv = Dotenv.load();
        String uri = dotenv.get("MONGO_URI");
        String dbName = dotenv.get("MONGO_DATABASE", "sample_horacerta");

        try (MongoClient client = MongoClients.create(uri)) {
            MongoDatabase db = client.getDatabase(dbName);
            MongoCollection<Document> collection = db.getCollection("medicamentos");

            // Procura o remédio específico deste usuário
            Bson filtro = and(
                    eq("idUsuario", this.idUsuario),
                    eq("nome", this.nomeRemedio),
                    eq("dia", this.dia),
                    eq("horario", this.horario)
            );

            // Atualiza o status para TOMOU = TRUE
            Bson update = set("tomou", true);

            UpdateResult result = collection.updateOne(filtro, update);

            if (result.getMatchedCount() > 0) {
                System.out.println("[ALARME] Medicamento confirmado como tomado: " + nomeRemedio);
                return true;
            } else {
                System.out.println("[ALARME] Medicamento não encontrado para confirmar: " + nomeRemedio);
                return false;
            }

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}