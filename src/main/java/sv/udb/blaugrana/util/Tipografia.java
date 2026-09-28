package sv.udb.blaugrana.util;

import java.awt.Font;

/**
 * Escala tipografica unica de la aplicacion. Centralizar los tamanos aqui
 * evita que cada panel invente su propio "new Font(...)" y que la app se
 * vea con una mezcla de tamanos sin jerarquia.
 */
public final class Tipografia {

    private static final String FAMILIA = "SansSerif";

    /** Titulo de portada / hero (ej. "PROXIMO PARTIDO", nombre de un jugador en su ficha). */
    public static final Font DISPLAY = new Font(FAMILIA, Font.BOLD, 26);

    /** Titulo de un modulo o de una tarjeta destacada (ej. "PLANTILLA"). */
    public static final Font TITULO = new Font(FAMILIA, Font.BOLD, 20);

    /** Subtitulo o etiqueta de seccion (ej. "RENDIMIENTO ACUMULADO"). */
    public static final Font SUBTITULO = new Font(FAMILIA, Font.BOLD, 13);

    /** Numero grande de una estadistica (ej. "24", "$125,000"). */
    public static final Font DATO_GRANDE = new Font(FAMILIA, Font.BOLD, 30);

    /** Numero mediano de una estadistica secundaria. */
    public static final Font DATO_MEDIANO = new Font(FAMILIA, Font.BOLD, 20);

    /** Texto de cuerpo normal (formularios, tablas). */
    public static final Font CUERPO = new Font(FAMILIA, Font.PLAIN, 13);

    /** Texto de cuerpo en negrita (valores dentro de una fila de datos). */
    public static final Font CUERPO_NEGRITA = new Font(FAMILIA, Font.BOLD, 13);

    /** Etiqueta pequena y discreta (nombre de una tarjeta, pie de foto). */
    public static final Font ETIQUETA = new Font(FAMILIA, Font.BOLD, 11);

    /** Texto muy pequeno (nota, aclaracion). */
    public static final Font NOTA = new Font(FAMILIA, Font.PLAIN, 11);

    private Tipografia() {
    }
}
