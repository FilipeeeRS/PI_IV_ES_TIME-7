package org.example;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.net.Socket;

import com.google.gson.Gson;

/** Parceiro do lado do CLIENTE, trocando JSON por linha */
public class Parceiro {
    private final Socket conexao;
    private final BufferedReader receptor;
    private final BufferedWriter transmissor;
    private final Gson gson = new Gson();

    // buffer para um "peek" simples (não usado no fluxo atual, mas útil)
    private String proximoJson = null;

    public Parceiro(Socket conexao, BufferedReader receptor, BufferedWriter transmissor) throws Exception {
        if (conexao == null)     throw new Exception("Conexao ausente");
        if (receptor == null)    throw new Exception("Receptor ausente");
        if (transmissor == null) throw new Exception("Transmissor ausente");
        this.conexao = conexao;
        this.receptor = receptor;
        this.transmissor = transmissor;
    }

    /** Envia um Comunicado (será serializado em JSON, uma linha, com \n + flush). */
    public void receba(Comunicado comunicado) throws Exception {
        try {
            String json = gson.toJson(comunicado);
            transmissor.write(json);
            transmissor.write("\n");     // IMPORTANTE: servidor espera fim de linha
            transmissor.flush();         // garante que sai do buffer
        } catch (IOException e) {
            throw new Exception("Erro de transmissao: " + e.getMessage(), e);
        }
    }

    /** Lê uma linha JSON do servidor e devolve como ComunicadoJson. */
    public Comunicado envie() throws Exception {
        try {
            if (proximoJson == null)
                proximoJson = receptor.readLine();   // bloqueia até chegar \n ou fechar

            if (proximoJson == null)
                throw new Exception("Conexao encerrada pelo servidor");

            String json = proximoJson;
            proximoJson = null;
            return new ComunicadoJson(json);         // sua classe embrulha o JSON cru
        } catch (IOException e) {
            throw new Exception("Erro de recepcao: " + e.getMessage(), e);
        }
    }

    /** Fecha streams e socket. */
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

    // ---------- utilitários opcionais ----------

    /** Envia uma string JSON já pronta (caso queira). */
    public void recebaJson(String json) throws Exception {
        try {
            transmissor.write(json);
            transmissor.write("\n");
            transmissor.flush();
        } catch (IOException e) {
            throw new Exception("Erro de transmissao: " + e.getMessage(), e);
        }
    }

    /** Lê a próxima linha crua (JSON bruto). */
    public String envieJson() throws Exception {
        try {
            if (proximoJson == null)
                proximoJson = receptor.readLine();
            String json = proximoJson;
            proximoJson = null;
            return json;
        } catch (IOException e) {
            throw new Exception("Erro de recepcao: " + e.getMessage(), e);
        }
    }
}