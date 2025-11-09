package org.example;

import java.io.Serializable;

public class PedidoDeDeletarMedicamento extends Comunicado {
    private static final long serialVersionUID = 1L;

    private String idMedicamento;

    public PedidoDeDeletarMedicamento(String idMedicamento) {
        if (idMedicamento == null || idMedicamento.isBlank())
            throw new IllegalArgumentException("ID do medicamento inválido");

        this.idMedicamento = idMedicamento;
    }

    public String getIdMedicamento() {
        return this.idMedicamento;
    }

    @Override
    public String toString() {
        return "PedidoDeDeletarMedicamento{" + "idMedicamento='" + idMedicamento + '\'' + '}';
    }
}