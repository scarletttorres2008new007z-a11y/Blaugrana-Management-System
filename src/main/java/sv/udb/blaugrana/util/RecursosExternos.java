package sv.udb.blaugrana.util;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Ubica y prepara la carpeta externa donde el usuario puede colocar,
 * manualmente y bajo su propia responsabilidad de derechos de autor,
 * fotografias reales de jugadores o el escudo del club.
 *
 * La aplicacion NUNCA descarga ni genera estas imagenes: solo las busca
 * aqui en tiempo de ejecucion y, si no las encuentra, usa un marcador
 * generico ("sin foto"). Esto evita distribuir contenido con derechos de
 * autor de terceros dentro del proyecto.
 */
public final class RecursosExternos {

    private static final File CARPETA_BASE = new File(System.getProperty("user.dir"), "recursos");
    private static final File CARPETA_JUGADORES = new File(CARPETA_BASE, "jugadores");

    private RecursosExternos() {
    }

    public static File carpetaJugadores() {
        return CARPETA_JUGADORES;
    }

    public static File escudoClub() {
        return new File(CARPETA_BASE, "escudo.png");
    }

    /** Busca la foto de un jugador por su numero de camiseta (jpg, jpeg o png). */
    public static File buscarFotoJugador(int numeroCamiseta) {
        for (String extension : new String[]{"jpg", "jpeg", "png"}) {
            File archivo = new File(CARPETA_JUGADORES, numeroCamiseta + "." + extension);
            if (archivo.isFile()) {
                return archivo;
            }
        }
        return null;
    }

    /**
     * Crea la carpeta "recursos/jugadores" junto con un LEEME si todavia no
     * existen, para que el usuario sepa exactamente donde colocar imagenes
     * propias o con licencia de uso.
     */
    public static void prepararCarpetas() {
        if (CARPETA_JUGADORES.mkdirs()) {
            File leeme = new File(CARPETA_JUGADORES, "LEEME.txt");
            try (FileWriter escritor = new FileWriter(leeme)) {
                escritor.write(
                        "Coloca aqui fotografias de jugadores que tengas derecho a usar,\n" +
                        "nombradas con el numero de camiseta del jugador, por ejemplo:\n\n" +
                        "  10.jpg   (Lamine Yamal, dorsal 10)\n" +
                        "  8.png    (Pedri, dorsal 8)\n\n" +
                        "Formatos aceptados: .jpg, .jpeg, .png\n\n" +
                        "Si no colocas una foto, la aplicacion muestra un marcador\n" +
                        "generico en su lugar; nunca genera ni inventa una imagen.\n\n" +
                        "El escudo del club, si tienes derecho a usarlo, se coloca en:\n" +
                        "  " + CARPETA_BASE.getAbsolutePath() + File.separator + "escudo.png\n"
                );
            } catch (IOException ignorada) {
                // No es critico si no se pudo escribir el LEEME; la carpeta ya quedo creada.
            }
        }
    }
}
