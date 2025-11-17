package org.example;

import java.util.ArrayList;

public class ResultadoListaMedicamentos extends Comunicado {
    private ArrayList<Medicamento> lista;

    public ResultadoListaMedicamentos(ArrayList<Medicamento> lista) {
        this.lista = lista;
    }

    // Getter para o Android pegar a lista
    public ArrayList<Medicamento> getLista() {
        return lista;
    }
}