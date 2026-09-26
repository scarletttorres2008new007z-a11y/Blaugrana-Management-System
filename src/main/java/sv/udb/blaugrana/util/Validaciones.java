package sv.udb.blaugrana.util;

import java.math.BigDecimal;

public final class Validaciones {

    private Validaciones() {
    }

    public static boolean esVacio(String texto) {
        return texto == null || texto.isBlank();
    }

    public static boolean esNumeroPositivo(BigDecimal valor) {
        return valor != null && valor.compareTo(BigDecimal.ZERO) >= 0;
    }

    public static boolean esEnteroPositivo(int valor) {
        return valor >= 0;
    }

    public static BigDecimal aBigDecimal(String texto) {
        if (esVacio(texto)) {
            return BigDecimal.ZERO;
        }
        return new BigDecimal(texto.trim().replace(",", ""));
    }
}
