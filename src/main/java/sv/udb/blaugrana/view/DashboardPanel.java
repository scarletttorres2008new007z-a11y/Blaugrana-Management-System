package sv.udb.blaugrana.view;

import sv.udb.blaugrana.model.Partido;
import sv.udb.blaugrana.service.ContratoService;
import sv.udb.blaugrana.service.FinanzasService;
import sv.udb.blaugrana.service.JugadorService;
import sv.udb.blaugrana.service.PagoService;
import sv.udb.blaugrana.service.PartidoService;
import sv.udb.blaugrana.service.ReporteService;
import sv.udb.blaugrana.util.ColoresBlaugrana;
import sv.udb.blaugrana.util.FormatoMoneda;
import sv.udb.blaugrana.util.Medidas;
import sv.udb.blaugrana.util.Mensajes;
import sv.udb.blaugrana.util.Tipografia;
import sv.udb.blaugrana.util.graficos.GraficoBarras;
import sv.udb.blaugrana.view.componentes.EncabezadoSeccion;
import sv.udb.blaugrana.view.componentes.EscudoEquipo;
import sv.udb.blaugrana.view.componentes.Insignia;
import sv.udb.blaugrana.view.componentes.PanelDegradado;
import sv.udb.blaugrana.view.componentes.TarjetaEstadistica;
import sv.udb.blaugrana.view.componentes.TarjetaPartido;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Portada del sistema al estilo de un portal deportivo: banda hero con el
 * proximo partido, resumen deportivo y financiero mediante tarjetas de
 * estadistica, franja de ultimos resultados, alertas con insignias de estado
 * y el grafico de plantilla por posicion. Todos los valores provienen de los
 * mismos metodos de servicio que ya existian antes del rediseño visual.
 */
public class DashboardPanel extends JPanel implements Refrescable {

    private static final int DIAS_ALERTA_CONTRATO = 60;
    private static final int MAX_RESULTADOS_RECIENTES = 3;

    private final JugadorService jugadorService = new JugadorService();
    private final ContratoService contratoService = new ContratoService();
    private final PartidoService partidoService = new PartidoService();
    private final FinanzasService finanzasService = new FinanzasService();
    private final PagoService pagoService = new PagoService();
    private final ReporteService reporteService = new ReporteService();

    private final TarjetaEstadistica tarjetaPlantilla =
            new TarjetaEstadistica("Plantilla activa", "0", ColoresBlaugrana.AZUL_OSCURO);
    private final TarjetaEstadistica tarjetaContratos =
            new TarjetaEstadistica("Contratos vigentes", "0", ColoresBlaugrana.AZUL_OSCURO);
    private final TarjetaEstadistica tarjetaPartidosJugados =
            new TarjetaEstadistica("Partidos disputados", "0", ColoresBlaugrana.AZUL_OSCURO);

    private final TarjetaEstadistica tarjetaVictorias =
            new TarjetaEstadistica("Victorias", "0", ColoresBlaugrana.VERDE_ACTIVO, Tipografia.DATO_MEDIANO);
    private final TarjetaEstadistica tarjetaEmpates =
            new TarjetaEstadistica("Empates", "0", ColoresBlaugrana.GRIS_TEXTO_SUAVE, Tipografia.DATO_MEDIANO);
    private final TarjetaEstadistica tarjetaDerrotas =
            new TarjetaEstadistica("Derrotas", "0", ColoresBlaugrana.ROJO_ALERTA, Tipografia.DATO_MEDIANO);
    private final TarjetaEstadistica tarjetaGolesFavor =
            new TarjetaEstadistica("Goles a favor", "0", ColoresBlaugrana.AZUL_MEDIO, Tipografia.DATO_MEDIANO);
    private final TarjetaEstadistica tarjetaGolesContra =
            new TarjetaEstadistica("Goles en contra", "0", ColoresBlaugrana.GRANATE, Tipografia.DATO_MEDIANO);

    private final TarjetaEstadistica tarjetaIngresosMes =
            new TarjetaEstadistica("Ingresos del mes", "$0.00", ColoresBlaugrana.VERDE_ACTIVO, Tipografia.DATO_MEDIANO);
    private final TarjetaEstadistica tarjetaEgresosMes =
            new TarjetaEstadistica("Egresos del mes", "$0.00", ColoresBlaugrana.ROJO_ALERTA, Tipografia.DATO_MEDIANO);
    private final TarjetaEstadistica tarjetaNomina =
            new TarjetaEstadistica("Nómina mensual", "$0.00", ColoresBlaugrana.AZUL_OSCURO, Tipografia.DATO_MEDIANO);
    private final TarjetaEstadistica tarjetaPagosPendientes =
            new TarjetaEstadistica("Pagos pendientes", "0", ColoresBlaugrana.GRANATE, Tipografia.DATO_MEDIANO);

    private final TarjetaEstadistica tarjetaIngresosTotales =
            new TarjetaEstadistica("Ingresos totales", "$0.00", ColoresBlaugrana.VERDE_ACTIVO);
    private final TarjetaEstadistica tarjetaEgresosTotales =
            new TarjetaEstadistica("Egresos totales", "$0.00", ColoresBlaugrana.ROJO_ALERTA);
    private final TarjetaEstadistica tarjetaBalance =
            new TarjetaEstadistica("Balance general", "$0.00", ColoresBlaugrana.AZUL_OSCURO);

    private final JPanel panelHeroPartido = new JPanel(new BorderLayout());
    private final JPanel panelUltimosResultados = new JPanel(new GridLayout(1, 1, 12, 12));
    private final JPanel panelAlertas = new JPanel();
    private final GraficoBarras graficoPosiciones = new GraficoBarras();

    public DashboardPanel() {
        setLayout(new BorderLayout());
        setBackground(ColoresBlaugrana.GRIS_CLARO);

        panelHeroPartido.setOpaque(false);
        panelUltimosResultados.setOpaque(false);

        add(construirHero(), BorderLayout.NORTH);

        JPanel contenido = new JPanel();
        contenido.setOpaque(false);
        contenido.setLayout(new BoxLayout(contenido, BoxLayout.Y_AXIS));
        contenido.setBorder(new EmptyBorder(Medidas.PADDING_SECCION, Medidas.PADDING_SECCION,
                Medidas.PADDING_SECCION, Medidas.PADDING_SECCION));

        agregarAncho(contenido, new EncabezadoSeccion("Resumen deportivo",
                "Plantilla, contratos y actividad de competición"));
        agregarAncho(contenido, filaTarjetas(tarjetaPlantilla, tarjetaContratos, tarjetaPartidosJugados));
        contenido.add(Box.createVerticalStrut(Medidas.ESPACIO_ENTRE_TARJETAS));
        agregarAncho(contenido, filaTarjetas(tarjetaVictorias, tarjetaEmpates, tarjetaDerrotas,
                tarjetaGolesFavor, tarjetaGolesContra));
        contenido.add(Box.createVerticalStrut(Medidas.PADDING_SECCION));

        agregarAncho(contenido, new EncabezadoSeccion("Últimos resultados"));
        agregarAncho(contenido, panelUltimosResultados);
        contenido.add(Box.createVerticalStrut(Medidas.PADDING_SECCION));

        agregarAncho(contenido, new EncabezadoSeccion("Situación financiera",
                "Ingresos, egresos y nómina del mes en curso"));
        agregarAncho(contenido, filaTarjetas(tarjetaIngresosMes, tarjetaEgresosMes, tarjetaNomina,
                tarjetaPagosPendientes));
        contenido.add(Box.createVerticalStrut(Medidas.ESPACIO_ENTRE_TARJETAS));
        agregarAncho(contenido, filaTarjetas(tarjetaIngresosTotales, tarjetaEgresosTotales, tarjetaBalance));
        contenido.add(Box.createVerticalStrut(Medidas.PADDING_SECCION));

        agregarAncho(contenido, new EncabezadoSeccion("Alertas y plantilla por posición"));
        JPanel filaInferior = new JPanel(new GridLayout(1, 2, Medidas.ESPACIO_ENTRE_TARJETAS, 0));
        filaInferior.setOpaque(false);
        filaInferior.setPreferredSize(new Dimension(0, 240));
        filaInferior.setMaximumSize(new Dimension(Integer.MAX_VALUE, 260));
        filaInferior.add(construirPanelAlertas());
        filaInferior.add(construirPanelGrafico());
        agregarAncho(contenido, filaInferior);

        JScrollPane scroll = new JScrollPane(contenido);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        add(scroll, BorderLayout.CENTER);
    }

    private void agregarAncho(JPanel contenedor, JComponent hijo) {
        hijo.setAlignmentX(Component.LEFT_ALIGNMENT);
        contenedor.add(hijo);
    }

    private JPanel filaTarjetas(JComponent... tarjetas) {
        JPanel fila = new JPanel(new GridLayout(1, tarjetas.length, Medidas.ESPACIO_ENTRE_TARJETAS,
                Medidas.ESPACIO_ENTRE_TARJETAS));
        fila.setOpaque(false);
        fila.setMaximumSize(new Dimension(Integer.MAX_VALUE, 110));
        for (JComponent tarjeta : tarjetas) {
            fila.add(tarjeta);
        }
        return fila;
    }

    private JPanel construirHero() {
        PanelDegradado hero = new PanelDegradado(new BorderLayout(24, 0),
                ColoresBlaugrana.AZUL_OSCURO, ColoresBlaugrana.AZUL_MEDIO, true);
        hero.setBorder(new EmptyBorder(24, Medidas.PADDING_SECCION, 24, Medidas.PADDING_SECCION));

        JPanel bloqueTitulo = new JPanel();
        bloqueTitulo.setOpaque(false);
        bloqueTitulo.setLayout(new BoxLayout(bloqueTitulo, BoxLayout.Y_AXIS));

        JPanel filaTitulo = new JPanel(new FlowLayout(FlowLayout.LEFT, 16, 0));
        filaTitulo.setOpaque(false);
        filaTitulo.setAlignmentX(Component.LEFT_ALIGNMENT);
        filaTitulo.add(new EscudoEquipo("FC Barcelona", true, 60));

        JPanel textoTitulo = new JPanel();
        textoTitulo.setOpaque(false);
        textoTitulo.setLayout(new BoxLayout(textoTitulo, BoxLayout.Y_AXIS));
        JLabel titulo = new JLabel("Panel general del club");
        titulo.setFont(Tipografia.DISPLAY);
        titulo.setForeground(ColoresBlaugrana.BLANCO);
        titulo.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel subtitulo = new JLabel("Resumen deportivo y financiero en tiempo real");
        subtitulo.setFont(Tipografia.NOTA);
        subtitulo.setForeground(ColoresBlaugrana.DORADO_SUAVE);
        subtitulo.setAlignmentX(Component.LEFT_ALIGNMENT);
        textoTitulo.add(titulo);
        textoTitulo.add(subtitulo);
        filaTitulo.add(textoTitulo);

        bloqueTitulo.add(Box.createVerticalGlue());
        bloqueTitulo.add(filaTitulo);
        bloqueTitulo.add(Box.createVerticalGlue());

        JPanel tarjetaProximo = new JPanel(new BorderLayout(0, 8));
        tarjetaProximo.setBackground(ColoresBlaugrana.BLANCO);
        tarjetaProximo.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(ColoresBlaugrana.DORADO, 1),
                new EmptyBorder(Medidas.PADDING_TARJETA, Medidas.PADDING_TARJETA,
                        Medidas.PADDING_TARJETA, Medidas.PADDING_TARJETA)));
        tarjetaProximo.setPreferredSize(new Dimension(360, 160));

        JLabel etiquetaProximo = new JLabel("PRÓXIMO PARTIDO");
        etiquetaProximo.setFont(Tipografia.ETIQUETA);
        etiquetaProximo.setForeground(ColoresBlaugrana.GRIS_TEXTO_SUAVE);

        tarjetaProximo.add(etiquetaProximo, BorderLayout.NORTH);
        tarjetaProximo.add(panelHeroPartido, BorderLayout.CENTER);

        hero.add(bloqueTitulo, BorderLayout.CENTER);
        hero.add(tarjetaProximo, BorderLayout.EAST);
        return hero;
    }

    private JPanel construirPanelAlertas() {
        JPanel contenedor = new JPanel(new BorderLayout());
        contenedor.setBackground(ColoresBlaugrana.BLANCO);
        contenedor.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(ColoresBlaugrana.GRIS_MEDIO, 1),
                new EmptyBorder(Medidas.PADDING_TARJETA, Medidas.PADDING_TARJETA,
                        Medidas.PADDING_TARJETA, Medidas.PADDING_TARJETA)));

        JLabel titulo = new JLabel("Alertas recientes");
        titulo.setFont(Tipografia.SUBTITULO);
        titulo.setForeground(ColoresBlaugrana.AZUL_OSCURO);
        titulo.setBorder(new EmptyBorder(0, 0, 10, 0));
        contenedor.add(titulo, BorderLayout.NORTH);

        panelAlertas.setLayout(new BoxLayout(panelAlertas, BoxLayout.Y_AXIS));
        panelAlertas.setOpaque(false);
        JScrollPane scroll = new JScrollPane(panelAlertas);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        contenedor.add(scroll, BorderLayout.CENTER);
        return contenedor;
    }

    private JPanel construirPanelGrafico() {
        JPanel contenedor = new JPanel(new BorderLayout());
        contenedor.setBackground(ColoresBlaugrana.BLANCO);
        contenedor.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(ColoresBlaugrana.GRIS_MEDIO, 1),
                new EmptyBorder(Medidas.PADDING_TARJETA, Medidas.PADDING_TARJETA,
                        Medidas.PADDING_TARJETA, Medidas.PADDING_TARJETA)));

        JLabel titulo = new JLabel("Plantilla por posición");
        titulo.setFont(Tipografia.SUBTITULO);
        titulo.setForeground(ColoresBlaugrana.AZUL_OSCURO);
        titulo.setBorder(new EmptyBorder(0, 0, 10, 0));
        contenedor.add(titulo, BorderLayout.NORTH);
        contenedor.add(graficoPosiciones, BorderLayout.CENTER);
        return contenedor;
    }

    @Override
    public void refrescar() {
        try {
            tarjetaPlantilla.setValor(String.valueOf(jugadorService.contarActivos()));
            tarjetaContratos.setValor(String.valueOf(contratoService.contarVigentes()));
            tarjetaPartidosJugados.setValor(String.valueOf(partidoService.contarFinalizados()));

            tarjetaVictorias.setValor(String.valueOf(partidoService.contarGanados()));
            tarjetaEmpates.setValor(String.valueOf(partidoService.contarEmpatados()));
            tarjetaDerrotas.setValor(String.valueOf(partidoService.contarPerdidos()));
            tarjetaGolesFavor.setValor(String.valueOf(partidoService.sumarGolesFavor()));
            tarjetaGolesContra.setValor(String.valueOf(partidoService.sumarGolesContra()));

            tarjetaIngresosMes.setValor(FormatoMoneda.formatear(finanzasService.ingresosMesActual()));
            tarjetaEgresosMes.setValor(FormatoMoneda.formatear(finanzasService.egresosMesActual()));
            tarjetaNomina.setValor(FormatoMoneda.formatear(contratoService.nominaMensual()));

            int pagosPendientes = pagoService.contarPendientes();
            tarjetaPagosPendientes.setValor(String.valueOf(pagosPendientes));

            BigDecimal ingresosTotales = finanzasService.totalIngresos();
            BigDecimal egresosTotales = finanzasService.totalEgresos();
            tarjetaIngresosTotales.setValor(FormatoMoneda.formatear(ingresosTotales));
            tarjetaEgresosTotales.setValor(FormatoMoneda.formatear(egresosTotales));
            tarjetaBalance.setValor(FormatoMoneda.formatear(ingresosTotales.subtract(egresosTotales)));

            actualizarProximoPartido();
            actualizarUltimosResultados();

            int contratosPorVencer = contratoService.contarPorVencer(DIAS_ALERTA_CONTRATO);
            int partidosProgramados = partidoService.contarProgramados();
            BigDecimal montoPendiente = pagoService.sumarPendientes();
            actualizarAlertas(contratosPorVencer, pagosPendientes, montoPendiente, partidosProgramados);

            List<Object[]> plantillaPorPosicion = reporteService.reportePlantillaPorPosicion();
            List<String> etiquetas = new ArrayList<>();
            List<Double> valores = new ArrayList<>();
            for (Object[] fila : plantillaPorPosicion) {
                etiquetas.add(String.valueOf(fila[0]));
                valores.add(((Number) fila[1]).doubleValue());
            }
            graficoPosiciones.setDatos("Plantilla por posición", etiquetas, valores);
        } catch (SQLException e) {
            Mensajes.error(this, "No se pudo cargar el dashboard", e);
        }
    }

    private void actualizarProximoPartido() throws SQLException {
        panelHeroPartido.removeAll();
        Optional<Partido> proximo = partidoService.buscarProximo();
        if (proximo.isPresent()) {
            panelHeroPartido.add(new TarjetaPartido(proximo.get(), true), BorderLayout.CENTER);
        } else {
            JLabel sinPartidos = new JLabel("Sin partidos programados", SwingConstants.CENTER);
            sinPartidos.setFont(Tipografia.CUERPO);
            sinPartidos.setForeground(ColoresBlaugrana.GRIS_TEXTO_SUAVE);
            panelHeroPartido.add(sinPartidos, BorderLayout.CENTER);
        }
        panelHeroPartido.revalidate();
        panelHeroPartido.repaint();
    }

    private void actualizarUltimosResultados() throws SQLException {
        panelUltimosResultados.removeAll();
        List<Partido> recientes = partidoService.listar().stream()
                .filter(p -> Partido.ESTADO_FINALIZADO.equals(p.getEstado()))
                .limit(MAX_RESULTADOS_RECIENTES)
                .toList();

        if (recientes.isEmpty()) {
            panelUltimosResultados.setLayout(new GridLayout(1, 1, 12, 12));
            JLabel sinResultados = new JLabel("Aún no hay partidos finalizados registrados.");
            sinResultados.setFont(Tipografia.CUERPO);
            sinResultados.setForeground(ColoresBlaugrana.GRIS_TEXTO_SUAVE);
            panelUltimosResultados.add(sinResultados);
        } else {
            panelUltimosResultados.setLayout(new GridLayout(1, recientes.size(), 12, 12));
            for (Partido partido : recientes) {
                JPanel tarjeta = new JPanel(new BorderLayout());
                tarjeta.setBackground(ColoresBlaugrana.BLANCO);
                tarjeta.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(ColoresBlaugrana.GRIS_MEDIO, 1),
                        new EmptyBorder(12, 12, 12, 12)));
                tarjeta.add(new TarjetaPartido(partido, false), BorderLayout.CENTER);
                panelUltimosResultados.add(tarjeta);
            }
        }
        panelUltimosResultados.revalidate();
        panelUltimosResultados.repaint();
    }

    private void actualizarAlertas(int contratosPorVencer, int pagosPendientes, BigDecimal montoPendiente,
                                    int partidosProgramados) {
        panelAlertas.removeAll();

        boolean hayAlertas = false;
        if (contratosPorVencer > 0) {
            panelAlertas.add(filaAlerta("PENDIENTE", contratosPorVencer + " contrato(s) vencen en los próximos "
                    + DIAS_ALERTA_CONTRATO + " días", ColoresBlaugrana.ROJO_ALERTA));
            hayAlertas = true;
        }
        if (pagosPendientes > 0) {
            panelAlertas.add(filaAlerta("PENDIENTE", pagosPendientes + " pago(s) pendiente(s) por "
                    + FormatoMoneda.formatear(montoPendiente), ColoresBlaugrana.AMBAR_ALERTA));
            hayAlertas = true;
        }
        if (partidosProgramados > 0) {
            panelAlertas.add(filaAlerta("PROGRAMADO", partidosProgramados + " partido(s) programado(s)",
                    ColoresBlaugrana.AZUL_MEDIO));
            hayAlertas = true;
        }
        if (!hayAlertas) {
            panelAlertas.add(filaAlerta("ACTIVO", "Sin alertas activas.", ColoresBlaugrana.GRIS_TEXTO));
        }

        panelAlertas.revalidate();
        panelAlertas.repaint();
    }

    private JPanel filaAlerta(String estadoInsignia, String texto, Color colorTexto) {
        JPanel fila = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 4));
        fila.setOpaque(false);
        fila.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel etiqueta = new JLabel(texto);
        etiqueta.setFont(Tipografia.CUERPO);
        etiqueta.setForeground(colorTexto);

        fila.add(new Insignia(estadoInsignia));
        fila.add(etiqueta);
        return fila;
    }
}
