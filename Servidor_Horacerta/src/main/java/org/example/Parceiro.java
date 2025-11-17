package org.example;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.net.Socket;
import com.google.gson.Gson;


public class Parceiro {
    private final Socket conexao;
    private final BufferedReader receptor;
    private final BufferedWriter transmissor;
    private final Gson gson = new Gson();


    private String proximoJson = null;

    public Parceiro(Socket conexao, BufferedReader receptor, BufferedWriter transmissor) throws Exception {
        if (conexao == null)     throw new Exception("Conexao ausente");
        if (receptor == null)    throw new Exception("Receptor ausente");
        if (transmissor == null) throw new Exception("Transmissor ausente");
        this.conexao = conexao;
        this.receptor = receptor;
        this.transmissor = transmissor;
    }


    public void receba(Comunicado x) throws Exception {
        try {
            String json = gson.toJson(x);
            transmissor.write(json);
            transmissor.write("\n");   // MUITO importante p/ readLine() do cliente
            transmissor.flush();
        } catch (IOException e) {
            throw new Exception("Erro de transmissao: " + e.getMessage(), e);
        }
    }



    public Comunicado envie() throws Exception {
        try {
            if (proximoJson == null) proximoJson = receptor.readLine(); // bloqueia até chegar \n
            if (proximoJson == null) throw new Exception("Conexao encerrada pelo cliente");

            String json = proximoJson;
            proximoJson = null;
            return new ComunicadoJson(json);
        } catch (IOException e) {
            throw new Exception("Erro de recepcao: " + e.getMessage(), e);
        }
    }

    /** Fecha streams e socket (tenta flush antes) */
    public void adeus() throws Exception {
        try {
            try { transmissor.flush(); } catch (Exception ignored) {}
            transmissor.close();
            receptor.close();
            conexao.close();
        } catch (IOException e) {
            throw new Exception("Erro de desconexao: " + e.getMessage(), e);
        }
    }

    // Helpers opcionais para JSON cru (se quiser usar)
    public void recebaJson(String json) throws Exception {
        try {
            transmissor.write(json);
            transmissor.write("\n");
            transmissor.flush();
        } catch (IOException e) {
            throw new Exception("Erro de transmissao: " + e.getMessage(), e);
        }
    }

    public String envieJson() throws Exception {
        try {
            if (proximoJson == null) proximoJson = receptor.readLine();
            String json = proximoJson;
            proximoJson = null;
            return json;
        } catch (IOException e) {
            throw new Exception("Erro de recepcao: " + e.getMessage(), e);
        }
    }
}