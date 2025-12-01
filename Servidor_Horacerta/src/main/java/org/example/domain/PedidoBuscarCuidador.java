package org.example.domain;


import com.google.gson.annotations.SerializedName;
import com.mongodb.client.*;
import com.mongodb.client.model.Filters;
import org.bson.Document;
import org.example.protocol.ComunicadoJson;
import java.util.Objects;
public class PedidoBuscarCuidador extends ComunicadoJson {

    @SerializedName("emailIdoso")
    private String emailIdoso;

    public PedidoBuscarCuidador() {
        super("BuscarCuidador");
    }

    public String processarBuscaNoBanco() {
        if (emailIdoso == null || emailIdoso.isBlank()) return null;

        // Link direto do seu banco
        String uri = "mongodb+srv://bestTeam:bestforever@cluster0.s3qlsah.mongodb.net/?retryWrites=true&w=majority&tls=true&appName=Cluster0";
        String dbName = "sample_horacerta";

        try (MongoClient client = MongoClients.create(uri)) {
            MongoDatabase db = client.getDatabase(dbName);
            MongoCollection<Document> col = db.getCollection("usuario");

            System.out.println("[BUSCA CUIDADOR] Procurando cuidador do idoso: " + emailIdoso);

            // Acha o Idoso pelo email
            Document docIdoso = col.find(Filters.eq("email", this.emailIdoso)).first();

            if (docIdoso != null) {
                // Pegar o ID do cuidador que está salvo nele
                String uidCuidador = docIdoso.getString("cuidador_responsavel_uid");

                if (uidCuidador != null && !uidCuidador.isEmpty()) {
                    System.out.println("[BUSCA CUIDADOR] UID encontrado: " + uidCuidador);

                    // Buscar o Cuidador pelo UID (firebase_uid)
                    Document docCuidador = col.find(Filters.eq("firebase_uid", uidCuidador)).first();

                    if (docCuidador != null) {
                        String nome = docCuidador.getString("nome");
                        return nome + ":::" + uidCuidador;
                    }
                }
            }
            System.out.println("[BUSCA CUIDADOR] Nenhum cuidador vinculado.");
            return null;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    @Override
    public int hashCode() {
        // Combina o hash do pai com o hash do campo de dados.
        return Objects.hash(super.hashCode(), emailIdoso);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;

        // Compara o pai primeiro
        if (!super.equals(obj)) return false;

        if (getClass() != obj.getClass()) return false;

        PedidoBuscarCuidador other = (PedidoBuscarCuidador) obj;

        // Compara o campo de dados.
        return Objects.equals(emailIdoso, other.emailIdoso);
    }

    @Override
    public String toString() {
        // Representação clara para logs.
        return "PedidoBuscarCuidador [Email Idoso: " + emailIdoso + "]";
    }
}