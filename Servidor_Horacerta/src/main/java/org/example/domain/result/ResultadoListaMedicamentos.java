package org.example.domain.result;

import org.example.domain.Medicamento;
import org.example.protocol.ComunicadoJson;

import java.util.ArrayList;
import java.util.Objects;
import java.util.List;


public class ResultadoListaMedicamentos extends ComunicadoJson {

    private List<Medicamento> medicamentos;

    public ResultadoListaMedicamentos(List<Medicamento> medicamentos) {
        super("ResultadoListaMedicamentos");
        this.medicamentos = medicamentos;
    }

    // Construtor de copia
    public ResultadoListaMedicamentos(ResultadoListaMedicamentos outro) {
        super(outro.getTipo());

        if (outro.medicamentos != null) {

            this.medicamentos = new ArrayList<>();
            for (Medicamento m : outro.medicamentos) {
                this.medicamentos.add(new Medicamento(m));
            }
        } else {
            this.medicamentos = null;
        }
    }

    // Opcional, mas útil para o Servidor
    public List<Medicamento> getMedicamentos() {
        return medicamentos;
    }

    @Override
    public int hashCode() {
        // Combina o hash do pai com o hash da lista (List.hashCode() usa os elementos).
        return Objects.hash(super.hashCode(), medicamentos);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;

        // Compara o pai primeiro
        if (!super.equals(obj)) return false;

        if (getClass() != obj.getClass()) return false;

        ResultadoListaMedicamentos other = (ResultadoListaMedicamentos) obj;

        // Compara a lista (List.equals() verifica o tamanho e a igualdade dos elementos).
        return Objects.equals(medicamentos, other.medicamentos);
    }

    @Override
    public String toString() {
        int count = (medicamentos == null) ? 0 : medicamentos.size();
        return "ResultadoListaMedicamentos [Total de Medicamentos: " + count + "]";
    }
}