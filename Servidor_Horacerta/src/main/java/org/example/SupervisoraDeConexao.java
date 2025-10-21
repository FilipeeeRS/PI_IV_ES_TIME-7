package org.example;

import com.mongodb.MongoWriteException;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;

import static com.mongodb.client.model.Filters.eq;

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
        String uri = "mongodb+srv://william:japa123@meuprimeirocluster.n6uh5ie.mongodb.net/?retryWrites=true&w=majority&appName=MeuPrimeiroCluster";
        try(MongoClient mongoClient = MongoClients.create(uri)) {
            MongoDatabase database = mongoClient.getDatabase("sample_horacerta");
            MongoCollection<Document> collection = database.getCollection("usuario");
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
                    String login = pedido.getLogin();
                    String senha = pedido.getSenha();
                    String funcao = pedido.getFuncao();

                    boolean sucesso;
                    String mensagem;

                    if (nome == null || nome.isBlank() ||
                            login == null || login.isBlank() ||
                            senha == null || senha.isBlank() ||
                            funcao == null || funcao.isBlank()) {
                        sucesso = false;
                        mensagem = "Campos obrigatórios ausentes.";
                        this.usuario.receba(new Resultado(sucesso, mensagem));
                        continue;
                    }

                    try {
                        // 🔒 (opcional) criptografar senha com BCrypt
                        // String hash = org.mindrot.jbcrypt.BCrypt.hashpw(senha, org.mindrot.jbcrypt.BCrypt.gensalt());
                        // String senhaParaSalvar = hash;

                        // por enquanto, salvar a senha original:
                        String senhaParaSalvar = senha;

                        // 🔹 documento a ser salvo no MongoDB
                        Document novoUsuario = new Document("nome", nome)
                                .append("login", login)
                                .append("senha", senhaParaSalvar)
                                .append("funcao", funcao)
                                .append("createdAt", System.currentTimeMillis());

                        // 🔍 verifica se já existe usuário com o mesmo login
                        Document existente = collection.find(eq("login", login)).first();
                        if (existente != null) {
                            sucesso = false;
                            mensagem = "Login já cadastrado.";
                        } else {
                            // 💾 insere no MongoDB
                            collection.insertOne(novoUsuario);
                            sucesso = true;
                            mensagem = "Cadastro realizado com sucesso!";
                        }

                    } catch (Exception e) {
                        sucesso = false;
                        mensagem = "Erro ao salvar no banco: " + e.getMessage();
                        e.printStackTrace();
                    }

                    System.out.println("[SERVIDOR] Enviando resultado para " + login + ": " + mensagem);
                    this.usuario.receba(new Resultado(sucesso, mensagem));
                    System.out.println("[SERVIDOR] Resultado enviado.");
                }
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
}