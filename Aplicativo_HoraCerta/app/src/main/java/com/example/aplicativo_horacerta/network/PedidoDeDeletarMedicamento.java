
package com.example.aplicativo_horacerta.network;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

// Classe para representar o pedido de exclusão de um medicamento
// Herda de ComunicadoJson para incluir o campo de operação
public class PedidoDeDeletarMedicamento extends ComunicadoJson implements Serializable {

        private static final long serialVersionUID = 1L;

        // Campo com o ID do Remédio
        @SerializedName("id")
        private String idMedicamento;

        // Campo com o ID do Usuário
        @SerializedName("idUsuario")
        private String idUsuario;


        public PedidoDeDeletarMedicamento(String idMedicamento, String idUsuario) {
                // Define o campo "operacao" fixo no ComunicadoJson para o Switch do servidor
                super("PedidoDeDeletarMedicamento");

                // Validação básica
                if (idMedicamento == null || idMedicamento.isBlank()) {
                        throw new IllegalArgumentException("ID do medicamento inválido");
                }
                if (idUsuario == null || idUsuario.isBlank()) {
                        throw new IllegalArgumentException("ID do usuário inválido");
                }

                this.idMedicamento = idMedicamento;
                this.idUsuario = idUsuario;
        }

        // Getters
        public String getIdMedicamento() {
                return idMedicamento;
        }

        public String getIdUsuario() {
                return idUsuario;
        }
}

