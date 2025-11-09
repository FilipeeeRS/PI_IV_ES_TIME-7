package org.example;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.*;
import com.mongodb.client.model.Filters;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;

import org.bson.Document;
import io.github.cdimascio.dotenv.Dotenv;

public class SupervisoraDeConexao extends Thread {
    private final Socket conexao;
    private final ArrayList<Parceiro> usuarios;
    private final ObjectOutputStream transmissor;
    private final ObjectInputStream  receptor;
    private Parceiro usuario;

    public SupervisoraDeConexao(
            Socket conexao,
            ArrayList<Parceiro> usuarios,
            ObjectOutputStream transmissor,
            ObjectInputStream receptor) throws Exception
    {
        if (conexao==null)  throw new Exception("Conexao ausente");
        if (usuarios==null) throw new Exception("Usuarios ausentes");
        this.conexao = conexao;
        this.usuarios = usuarios;
        this.transmissor = transmissor;
        this.receptor = receptor;
    }

    @Override
    public void run() {
        try {
            this.usuario = new Parceiro(conexao, receptor, transmissor);

            synchronized (usuarios) { usuarios.add(usuario); }

            for (;;) {
                System.out.println("[SERVIDOR] Aguardando objeto do cliente...");

                Comunicado comunicado;
                try {
                    comunicado = this.usuario.envie();
                } catch (Exception e) {
                    System.out.println("[SERVIDOR] Cliente desconectou: " + e.getMessage());
                    break;
                }

                if (comunicado == null) continue;

                System.out.println("[SERVIDOR] Recebido: " + comunicado.getClass().getSimpleName());

                if (comunicado instanceof PedidoDeCadastro pedido) {
                    String nome = pedido.getNome();
                    String email = pedido.getEmail();
                    String senha = pedido.getSenha();
                    String tipo = pedido.getTipo();

                    boolean sucesso;
                    String mensagem;

                    // Conecta ao MongoDB
                    Dotenv dotenv = Dotenv.load();
                    try (MongoClient client = MongoClients.create(dotenv.get("MONGO_URI"))) {
                        MongoDatabase db = client.getDatabase(dotenv.get("MONGO_DATABASE", "sample_horacerta"));
                        MongoCollection<Document> col = db.getCollection("usuarios");

                        // Verifica se o email já existe
                        Document existente = col.find(Filters.eq("email", email)).first();

                        if (existente != null) {
                            sucesso = false;
                            mensagem = "Este e-mail já está em uso.";
                        } else {
                            Document novoUsuario = new Document()
                                    .append("nome", nome)
                                    .append("email", email)
                                    .append("senha", senha)
                                    .append("tipo", tipo);

                            // Insere usuário no MongoDB
                            col.insertOne(novoUsuario);

                            sucesso = true;
                            mensagem = "Cadastro realizado com sucesso!";
                        }
                    } catch (Exception e) {
                        System.err.println("[ERRO] Falha ao cadastrar: " + e.getMessage());
                        sucesso = false;
                        mensagem = "Erro ao conectar com o banco de dados.";
                    }

                    System.out.println("[SERVIDOR] Enviando resultado para " + email + ": " + mensagem);
                    this.usuario.receba(new Resultado(sucesso, mensagem));
                    System.out.println("[SERVIDOR] Resultado enviado.");
                }

                else if (comunicado instanceof PedidoDeCriarMedicamento pedido) {
                    System.out.println("[SERVIDOR] Recebido pedido de criar medicamento.");
                    boolean sucesso = pedido.executar();

                    String mensagem;
                    if (sucesso) {
                        mensagem = "Medicamento criado com sucesso!";
                    } else {
                        mensagem = "Erro ao criar medicamento no banco de dados.";
                    }

                    this.usuario.receba(new Resultado(sucesso, mensagem)); // envia true ou false para o android
                }

                else if (comunicado instanceof PedidoDeListarMedicamentos pedido) {
                    System.out.println("[SERVIDOR] Listando medicamentos para: " + pedido.getIdUsuario());

                    String idUsuario = pedido.getIdUsuario();
                    ArrayList<Medicamento> lista = new ArrayList<>();

                    Dotenv dotenv = Dotenv.load(); // Conecta no mongo e busca a lista
                    try (MongoClient client = MongoClients.create(dotenv.get("MONGO_URI"))) {
                        MongoDatabase db = client.getDatabase(dotenv.get("MONGO_DATABASE", "sample_horacerta"));
                        MongoCollection<Document> col = db.getCollection("medicamentos");

                        // Busca as informações onde o idUsuario é igual
                        for (Document doc : col.find(Filters.eq("idUsuario", idUsuario))) {
                            Medicamento med = new Medicamento(
                                    doc.getObjectId("_id").toHexString(),
                                    doc.getString("nome"),
                                    doc.getString("dia"),
                                    doc.getString("horario"),
                                    doc.getString("descricao"),
                                    doc.getString("idUsuario")
                            );
                            lista.add(med);
                        }
                    } catch (Exception e) {
                        System.err.println("[ERRO] Falha ao listar medicamentos: " + e.getMessage());
                    }

                    System.out.println("[SERVIDOR] Encontrados " + lista.size() + " medicamentos.");

                    this.usuario.receba(new ResultadoListaMedicamentos(lista)); // Envia de volta para o android
                }

                else if (comunicado instanceof PedidoDeDeletarMedicamento pedido) {
                    System.out.println("[SERVIDOR] Recebido pedido para deletar: " + pedido.getIdMedicamento());

                    String idParaDeletar = pedido.getIdMedicamento();
                    boolean sucesso;
                    String mensagem;

                    Dotenv dotenv = Dotenv.load();
                    try (MongoClient client = MongoClients.create(dotenv.get("MONGO_URI"))) {
                        MongoDatabase db = client.getDatabase(dotenv.get("MONGO_DATABASE", "sample_horacerta"));
                        MongoCollection<Document> col = db.getCollection("medicamentos");

                        ObjectId objectId = new ObjectId(idParaDeletar);

                        var deleteResult = col.deleteOne(Filters.eq("_id", objectId));

                        if (deleteResult.getDeletedCount() > 0) {
                            sucesso = true;
                            mensagem = "Medicamento deletado com sucesso.";
                        } else {
                            sucesso = false;
                            mensagem = "Medicamento não encontrado.";
                        }
                    } catch (Exception e) {
                        System.err.println("[ERRO] Falha ao deletar: " + e.getMessage());
                        sucesso = false;
                        mensagem = "Erro ao conectar com o banco de dados.";
                    }

                    this.usuario.receba(new Resultado(sucesso, mensagem));
            }

        } catch (Exception e) {
            System.out.println("[SERVIDOR] Conexao encerrada: " + e);
            e.printStackTrace();
        } finally {
            try { transmissor.close(); } catch (Exception ignore) {}
            try { receptor.close(); }    catch (Exception ignore) {}
            try { conexao.close(); }     catch (Exception ignore) {}
            synchronized (usuarios) { usuarios.remove(usuario); }
        }
    }