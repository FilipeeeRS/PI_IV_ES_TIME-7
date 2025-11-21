package org.example;

import java.util.List;


public class ResultadoListaMedicamentos extends ComunicadoJson {


    private List<Medicamento> medicamentos;


    public ResultadoListaMedicamentos(List<Medicamento> medicamentos) {
        super("ResultadoListaMedicamentos");
        this.medicamentos = medicamentos;
    }

    // Opcional, mas útil para o Servidor
    public List<Medicamento> getMedicamentos() {
        return medicamentos;
    }
}