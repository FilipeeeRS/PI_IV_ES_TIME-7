package org.example;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.bson.Document;



public class SupervisoraDeConexao extends Thread {
    private Parceiro usuario;
    private Socket conexao;
    private ArrayList<Parceiro> usuarios;
    private Gson gson;

    public SupervisoraDeConexao(Socket conexao, ArrayList<Parceiro> usuarios)
            throws Exception {
        if (conexao == null)
            throw new Exception("Conexao ausente");

        if (usuarios == null)
            throw new Exception("Usuarios ausentes");

        this.conexao = conexao;
        this.usuarios = usuarios;
        this.gson = new Gson();
    }

    public void run() {
        try {
            BufferedWriter transmissor = new BufferedWriter(
                    new OutputStreamWriter(this.conexao.getOutputStream(), StandardCharsets.UTF_8));
            BufferedReader receptor = new BufferedReader(
                    new InputStreamReader(this.conexao.getInputStream(), StandardCharsets.UTF_8));

            this.usuario = new Parceiro(this.conexao, receptor, transmissor);

            synchronized (this.usuarios) {
                this.usuarios.add(this.usuario);
            }

            for (;;) {
                // Tenta ler o comunicado JSON bruto
                ComunicadoJson comunicadoJson = (ComunicadoJson) this.usuario.envie();

                if (comunicadoJson == null)
                    return;

                // 1. Extrai o tipo de operação
                String json = comunicadoJson.getJson();
                JsonObject obj = gson.fromJson(json, JsonObject.class);
                String tipo = obj.get("operacao").getAsString();
                boolean resultado;

                // 2. Processa a operação com o Switch
                switch (tipo) {
                    case "PedidoParaSair":
                        synchronized (this.usuarios) {
                            resultado = this.usuarios.remove(this.usuario);
                        }
                        this.usuario.receba(new ResultadoOperacao(resultado, "LogOut"));
                        this.usuario.adeus();
                        return;

                    case "Cadastro":
                        PedidoDeCadastro cadastro = gson.fromJson(json, PedidoDeCadastro.class);
                        resultado = cadastro.criarDocumento();
                        this.usuario.receba(new ResultadoOperacao(resultado, "ResultadoCadastro"));
                        break;

                    case "Login":
                        PedidoDeLogin login = gson.fromJson(json, PedidoDeLogin.class);
                        Usuario user = login.getUserData();
                        boolean userFound = user != null;
                        this.usuario.receba(new ResultadoLogin(userFound, "ResultadoLogin", user));
                        break;

                    case "ConectarIdoso":
                        PedidoDeConexao pedidoConexao = gson.fromJson(json, PedidoDeConexao.class);
                        boolean sucessoConexao = pedidoConexao.realizarVinculo();
                        String msg = sucessoConexao ? "Vinculo realizado com sucesso!" : "Erro ao vincular. Verifique o email.";
                        this.usuario.receba(new ResultadoOperacao(sucessoConexao, msg));
                        break;

                    case "BuscarIdoso":
                        PedidoBuscarIdoso pedidoBusca = gson.fromJson(json, PedidoBuscarIdoso.class);
                        String nomeEncontrado = pedidoBusca.procurarNomeNoBanco();
                        boolean achou = (nomeEncontrado != null);
                        this.usuario.receba(new ResultadoBuscaIdoso(achou, nomeEncontrado));
                        break;

                    case "BuscarCuidador":
                        PedidoBuscarCuidador pedidoC = gson.fromJson(json, PedidoBuscarCuidador.class);
                        String nomeCuidador = pedidoC.processarBuscaNoBanco();
                        boolean achouC = (nomeCuidador != null);
                        String nomeFinal = achouC ? nomeCuidador : "Nenhum cuidador vinculado";
                        this.usuario.receba(new ResultadoBuscaCuidador(achouC, nomeFinal));
                        break;

                    case "PedidoDeCriarMedicamento":
                        PedidoDeCriarMedicamento pedidoMedicamento = gson.fromJson(json, PedidoDeCriarMedicamento.class);
                        resultado = pedidoMedicamento.executar();
                        this.usuario.receba(new ResultadoOperacao(resultado, "PedidoDeCriarMedicamento"));
                        break;

                    case "PedidoDeListarMedicamentos":
                        PedidoDeListarMedicamentos pedidoListarMedicamento = gson.fromJson(json, PedidoDeListarMedicamentos.class);

                        // Executa a busca
                        List<Document> listaDocumentos = pedidoListarMedicamento.executar();

                        // Converte Document para POJO (Medicamento)
                        List<Medicamento> listaMedicamentosPOJO = new ArrayList<>();
                        for (Document doc : listaDocumentos) {
                            // Presume-se que Medicamento.fromDocument(doc) está implementado
                            listaMedicamentosPOJO.add(Medicamento.fromDocument(doc));
                        }

                        // Envia a resposta de LISTA e ENCERRA este case
                        this.usuario.receba(new ResultadoListaMedicamentos(listaMedicamentosPOJO));
                        break; // <--- O break garante que o código não continue

                    case "PedidoDeEditarMedicamento":
                        PedidoDeEditarMedicamento pedidoEdit = gson.fromJson(json, PedidoDeEditarMedicamento.class);
                        boolean editou = pedidoEdit.executar();
                        this.usuario.receba(new ResultadoOperacao(editou, "MedicamentoEditado"));
                        break;

                    case "PedidoDeDeletarMedicamento":
                        PedidoDeDeletarMedicamento pedidoDel = gson.fromJson(json, PedidoDeDeletarMedicamento.class);
                        boolean deletou = pedidoDel.executar();
                        this.usuario.receba(new ResultadoOperacao(deletou, "MedicamentoDeletado"));
                        break;

                    default:
                        System.err.println("Comunicado desconhecido: " + tipo);
                        break;
                }
            } // Fim do for(;;)
        } catch (Exception erro) {
            System.err.println("Erro de supervisao: " + erro.getMessage());
            erro.printStackTrace(); // Adicionei para melhor debug
            try {
                if (usuario != null)
                    usuario.adeus();
            } catch (Exception ignored) {
            }
        }
    }
}