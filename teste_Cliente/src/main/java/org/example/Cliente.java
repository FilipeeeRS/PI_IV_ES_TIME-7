package org.example;

import java.io.*;
import java.net.Socket;
import java.util.Scanner;

public class Cliente {
    public static void main(String[] args) {
        try (Socket conexao = new Socket("localhost", 3000)) {

            BufferedWriter transmissor = new BufferedWriter(new OutputStreamWriter(conexao.getOutputStream()));
            BufferedReader receptor = new BufferedReader(new InputStreamReader(conexao.getInputStream()));

            Parceiro servidor = new Parceiro(conexao, receptor, transmissor);

            System.out.println("Conectado ao servidor em localhost:3000");
            System.out.println("Digite uma opcao: login | cadastro | sair");

            Scanner teclado = new Scanner(System.in);

            while (true) {
                System.out.print("> ");
                String comando = teclado.nextLine();

                if (comando.equalsIgnoreCase("sair")) {
                    servidor.receba(new PedidoParaSair());
                    servidor.adeus();
                    System.out.println("Você saiu do servidor.");
                    break;
                }
                else if (comando.equalsIgnoreCase("cadastro")) {
                    System.out.print("Nome: ");
                    String nome = teclado.nextLine();
                    System.out.print("Login (email): ");
                    String login = teclado.nextLine();
                    System.out.print("Senha: ");
                    String senha = teclado.nextLine();
                    System.out.print("Função: ");
                    String funcao = teclado.nextLine();

                    PedidoDeCadastro pedido = new PedidoDeCadastro(nome, login, senha, funcao);
                    servidor.receba(pedido);

                    Object resposta = servidor.envie();
                    System.out.println("Resposta do servidor: " + resposta);
                }
                else if (comando.equalsIgnoreCase("login")) {
                    System.out.print("Login: ");
                    String login = teclado.nextLine();
                    System.out.print("Senha: ");
                    String senha = teclado.nextLine();

                    PedidoDeLogin pedido = new PedidoDeLogin(login, senha);
                    servidor.receba(pedido);

                    Object resposta = servidor.envie();
                    System.out.println("Resposta do servidor: " + resposta);
                }
                else {
                    System.out.println("Comando desconhecido.");
                }
            }

        } catch (Exception e) {
            System.err.println("Erro no cliente: " + e.getMessage());
            e.printStackTrace();
        }
    }
}