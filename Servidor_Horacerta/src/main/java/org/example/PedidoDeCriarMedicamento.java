package org.example;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import io.github.cdimascio.dotenv.Dotenv;
import org.bson.Document;

public class PedidoDeCriarMedicamento extends Comunicado {
    private String nome;
    private String dia;
    private String horario;
    private String descricao;
    private String idUsuario; // de quem é o remédio (?)

    public PedidoDeCriarMedicamento(String nome, String dia, String horario, String descricao, String idUsuario) {
        this.nome = nome;
        this.dia = dia;
        this.horario = horario;
        this.descricao = descricao;
        this.idUsuario = idUsuario;
    }

    // Métod0 para executar
    public boolean executar() {
        Dotenv dotenv = Dotenv.load();
        String uri = dotenv.get("MONGO_URI");
        String dbName = dotenv.get("MONGO_DATABASE", "sample_horacerta");

        try (MongoClient client = MongoClients.create(uri)) {
            MongoDatabase db = client.getDatabase(dbName);
            MongoCollection<Document> col = db.getCollection("medicamentos");

            Document doc = new Document("nome", this.nome)
                    .append("dia", this.dia)
                    .append("horario", this.horario)
                    .append("descricao", this.descricao)
                    .append("idUsuario", this.idUsuario)
                    .append("tomou", false); // False padrão

            col.insertOne(doc);
            System.out.println("[MEDICAMENTO] Criado com sucesso para usuario: " + this.idUsuario);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}