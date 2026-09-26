package sv.udb.blaugrana.util;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Locale;

public final class FormatoMoneda {

    private static final NumberFormat FORMATO = NumberFormat.getCurrencyInstance(new Locale("en", "US"));

    private FormatoMoneda() {
    }

    public static String formatear(BigDecimal monto) {
        if (monto == null) {
            return FORMATO.format(BigDecimal.ZERO);
        }
        return FORMATO.format(monto);
    }
}
