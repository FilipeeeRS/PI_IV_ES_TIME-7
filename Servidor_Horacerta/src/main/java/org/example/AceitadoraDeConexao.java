package org.example;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class AceitadoraDeConexao extends Thread{
    private final int porta;
    private final ArrayList<Parceiro> usuarios;

    public AceitadoraDeConexao(String porta, ArrayList<Parceiro> usuarios){
        this.porta = Integer.parseInt(porta);
        this.usuarios = usuarios;
    }
    @Override
    public void run(){
        try(ServerSocket servidor = new ServerSocket(porta)) {
            System.out.println("[SERVIDOR] Ouvindo na porta" + porta + "...");
            for (;;){
                Socket conexao = servidor.accept();
                System.out.println("[SERVIDOR] Nova conexao: " + conexao.getRemoteSocketAddress());

                ObjectOutputStream oos = new ObjectOutputStream(conexao.getOutputStream());
                ObjectInputStream ois = new ObjectInputStream(conexao.getInputStream());

                new SupervisoraDeConexao(conexao, usuarios, oos, ois).start();
            }

        }catch (Exception e){
            System.err.println("[SERVIDOR] Erro na aceitadora: " + e);
            e.printStackTrace();
        }
    }
}