package org.example;

import com.google.gson.annotations.SerializedName;
import com.mongodb.client.*;
import com.mongodb.client.model.Filters;
import org.bson.Document;

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

            // 1. Achar o Idoso pelo email
            Document docIdoso = col.find(Filters.eq("email", this.emailIdoso)).first();

            if (docIdoso != null) {
                // 2. Pegar o ID do cuidador que está salvo nele
                String uidCuidador = docIdoso.getString("cuidador_responsavel_uid");

                if (uidCuidador != null && !uidCuidador.isEmpty()) {
                    System.out.println("[BUSCA CUIDADOR] UID encontrado: " + uidCuidador);

                    // 3. Buscar o Cuidador pelo UID (firebase_uid)
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
}