package org.example;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Scanner;

public class Cliente {
    public static void main(String[] args) {
        String host = "127.0.0.1"; // ou IP da máquina do servidor
        int porta = 3000;          // mesma porta configurada no servidor

        try (Socket socket = new Socket(host, porta)) {
            System.out.println("[CLIENTE] Conectado ao servidor " + host + ":" + porta);

            // IMPORTANTE: saída antes da entrada
            ObjectOutputStream transmissor = new ObjectOutputStream(socket.getOutputStream());
            ObjectInputStream  receptor    = new ObjectInputStream(socket.getInputStream());

            Scanner scanner = new Scanner(System.in);

            // Interface simples
            System.out.print("Nome: ");
            String nome = scanner.nextLine();
            System.out.print("Login: ");
            String login = scanner.nextLine();
            System.out.print("Senha: ");
            String senha = scanner.nextLine();
            System.out.print("Funcao: ");
            String funcao = scanner.nextLine();

            // envia o pedido
            PedidoDeCadastro pedido = new PedidoDeCadastro(nome, login, senha, funcao);
            transmissor.writeObject(pedido);
            transmissor.flush();

            System.out.println("[CLIENTE] Pedido enviado. Aguardando resposta...");

            // recebe resposta
            Object resposta = receptor.readObject();

            if (resposta instanceof Resultado r) {
                System.out.println("[CLIENTE] Servidor respondeu:");
                System.out.println("Sucesso: " + r.isSucesso());
                System.out.println("Mensagem: " + r.getMensagem());
            } else {
                System.out.println("[CLIENTE] Resposta inesperada: " + resposta);
            }

            transmissor.close();
            receptor.close();
            scanner.close();

        } catch (Exception e) {
            System.err.println("[CLIENTE] Erro: " + e.getMessage());
            e.printStackTrace();
        }
    }
}