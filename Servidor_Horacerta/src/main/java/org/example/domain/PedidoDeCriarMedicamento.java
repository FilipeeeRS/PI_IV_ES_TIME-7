package org.example.domain;


import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import io.github.cdimascio.dotenv.Dotenv;
import org.bson.Document;
import com.google.gson.annotations.SerializedName;
import org.example.protocol.ComunicadoJson;
import java.util.Objects;

public class PedidoDeCriarMedicamento extends ComunicadoJson {
    @SerializedName("nome")
    private String nome;
    @SerializedName("dia")
    private String dia;
    @SerializedName("horario")
    private String horario;
    @SerializedName("descricao")
    private String descricao;
    @SerializedName("idUsuario")
    private String idUsuario;

    public PedidoDeCriarMedicamento() {
        super("PedidoDeCriarMedicamento");
    }

    public String getNome(){ return this.nome ;}
    public String getDia(){ return this.dia; }
    public String getHorario(){ return  this.horario; }
    public String getDescricao(){ return this.descricao; }
    public String getIdUsuario() {return this.idUsuario; }

    // Métod0 para executar
    public boolean executar() {
        try {
            if (isBlank(nome) || isBlank(dia) || isBlank(horario) || isBlank(descricao) || isBlank(idUsuario)) {
                // Lança a exceção de validação
                throw new IllegalArgumentException("Campos obrigatórios ausentes");
            }

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
            } catch (com.mongodb.MongoException e) {
                System.err.println("Erro ao interagir com o MongoDB: " + e.getMessage());
                return false;
            }
        } catch (IllegalArgumentException e) {
            System.err.println("Erro de Validação: " + e.getMessage());
            return false;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    private static boolean isBlank(String s) {
        return s == null || s.isBlank();
    }

    // Construtor de copia
    public PedidoDeCriarMedicamento(PedidoDeCriarMedicamento outro) {
        // 1. Chama o construtor do pai (ComunicadoJson)
        super(outro.getTipo());

        // 2. Copia todos os campos
        this.nome = outro.nome;
        this.dia = outro.dia;
        this.horario = outro.horario;
        this.descricao = outro.descricao;
        this.idUsuario = outro.idUsuario;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), nome, dia, horario, descricao, idUsuario);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;

        // Compara o pai primeiro
        if (!super.equals(obj)) return false;

        if (getClass() != obj.getClass()) return false;

        PedidoDeCriarMedicamento other = (PedidoDeCriarMedicamento) obj;

        // Compara todos os cinco campos de dados.
        return Objects.equals(nome, other.nome) &&
                Objects.equals(dia, other.dia) &&
                Objects.equals(horario, other.horario) &&
                Objects.equals(descricao, other.descricao) &&
                Objects.equals(idUsuario, other.idUsuario);
    }

    @Override
    public String toString() {
        return "PedidoDeCriarMedicamento [Nome: " + nome + ", Dia/Hora: " + dia + " @ " + horario + ", Usuário ID: " + idUsuario + "]";
    }


}
