package org.example.protocol;

import java.io.Serializable;

public abstract class Comunicado implements Serializable {
    private static final long serialVersionUID = 1L;

    @Override
    public String toString() {
        return "Comunicado [Classe: " + this.getClass().getSimpleName() + "]";
    }

    @Override
    public int hashCode() {

        return super.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;

        if (obj == null || getClass() != obj.getClass()) return false;

        return true;
    }

}