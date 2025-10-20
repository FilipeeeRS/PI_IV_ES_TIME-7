package org.example;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;

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
                    String login = pedido.getLogin();
                    String senha = pedido.getSenha();

                    boolean sucesso;
                    String mensagem;

                    if (login == null || login.isBlank() || senha == null || senha.isBlank()) {
                        sucesso=false; mensagem="Login/senha inválidos.";
                    } else if (Servidor.CADASTROS.containsKey(login)) {
                        sucesso=false; mensagem="Login já existe.";
                    } else {
                        Servidor.CADASTROS.put(login, senha);
                        sucesso=true;  mensagem="Cadastro realizado com sucesso.";
                    }

                    System.out.println("[SERVIDOR] Enviando resultado para " + login + ": " + mensagem);
                    this.usuario.receba(new Resultado(sucesso, mensagem)); // faz flush
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