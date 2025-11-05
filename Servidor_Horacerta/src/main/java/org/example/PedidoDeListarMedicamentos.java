package org.example;

public class PedidoDeListarMedicamentos extends Comunicado {
    private String idUsuario; // ID do cuidador e/ou idoso, quem quer ver a lista

    public PedidoDeListarMedicamentos(String idUsuario) {
        this.idUsuario = idUsuario;
    }

    public String getIdUsuario() {
        return idUsuario;
    }
}