package sv.udb.blaugrana.view.rendimiento;

import sv.udb.blaugrana.model.Jugador;
import sv.udb.blaugrana.model.ParticipacionPartido;
import sv.udb.blaugrana.model.Partido;
import sv.udb.blaugrana.service.JugadorService;
import sv.udb.blaugrana.service.PartidoService;
import sv.udb.blaugrana.service.RendimientoService;
import sv.udb.blaugrana.util.ColoresBlaugrana;
import sv.udb.blaugrana.util.Medidas;
import sv.udb.blaugrana.util.Mensajes;
import sv.udb.blaugrana.util.PermisosUI;
import sv.udb.blaugrana.util.Tipografia;
import sv.udb.blaugrana.util.graficos.GraficoBarras;
import sv.udb.blaugrana.view.Refrescable;
import sv.udb.blaugrana.view.componentes.EncabezadoSeccion;
import sv.udb.blaugrana.view.componentes.TarjetaEstadistica;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Participacion de jugadores por partido: resumen del encuentro mediante
 * tarjetas de estadistica, tabla detallada por jugador y un grafico de
 * minutos jugados, sin alterar la logica de RendimientoService.
 */
public class RendimientoPanel extends JPanel implements Refrescable {

    private final PartidoService partidoService = new PartidoService();
    private final JugadorService jugadorService = new JugadorService();
    private final RendimientoService rendimientoService = new RendimientoService();

    private final JComboBox<Partido> cmbPartido = new JComboBox<>();
    private final JComboBox<Jugador> cmbJugador = new JComboBox<>();

    private final DefaultTableModel modeloTabla = new DefaultTableModel(
            new Object[]{"ID", "Jugador", "Min", "Goles", "Asist.", "TA", "TR", "Titular"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final JTable tabla = new JTable(modeloTabla);

    private final JTextField txtMinutos = new JTextField(4);
    private final JTextField txtGoles = new JTextField(4);
    private final JTextField txtAsistencias = new JTextField(4);
    private final JTextField txtAmarillas = new JTextField(4);
    private final JTextField txtRojas = new JTextField(4);
    private final JCheckBox chkTitular = new JCheckBox("Titular");

    private final TarjetaEstadistica tarjetaJugadoresUtilizados =
            new TarjetaEstadistica("Jugadores utilizados", "0", ColoresBlaugrana.AZUL_OSCURO, Tipografia.DATO_MEDIANO);
    private final TarjetaEstadistica tarjetaGolesPartido =
            new TarjetaEstadistica("Goles del partido", "0", ColoresBlaugrana.GRANATE, Tipografia.DATO_MEDIANO);
    private final TarjetaEstadistica tarjetaAsistenciasPartido =
            new TarjetaEstadistica("Asistencias", "0", ColoresBlaugrana.AZUL_MEDIO, Tipografia.DATO_MEDIANO);
    private final TarjetaEstadistica tarjetaTarjetasPartido =
            new TarjetaEstadistica("Tarjetas mostradas", "0", ColoresBlaugrana.AMBAR_ALERTA, Tipografia.DATO_MEDIANO);

    private final GraficoBarras graficoMinutos = new GraficoBarras();

    private Integer idSeleccionado;

    public RendimientoPanel() {
        setLayout(new BorderLayout());
        setBackground(ColoresBlaugrana.GRIS_CLARO);
        setBorder(new EmptyBorder(20, 20, 20, 20));

        add(construirEncabezado(), BorderLayout.NORTH);

        JPanel centro = new JPanel(new BorderLayout(0, Medidas.ESPACIO_ENTRE_TARJETAS));
        centro.setOpaque(false);
        centro.add(construirFilaResumen(), BorderLayout.NORTH);

        JPanel filaTablaGrafico = new JPanel(new BorderLayout(Medidas.ESPACIO_ENTRE_TARJETAS, 0));
        filaTablaGrafico.setOpaque(false);
        filaTablaGrafico.add(construirTarjetaTabla(), BorderLayout.CENTER);
        filaTablaGrafico.add(construirTarjetaGrafico(), BorderLayout.EAST);
        centro.add(filaTablaGrafico, BorderLayout.CENTER);

        add(centro, BorderLayout.CENTER);
        add(construirFormulario(), BorderLayout.SOUTH);

        tabla.setRowHeight(24);
        tabla.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                cargarSeleccion();
            }
        });
        cmbPartido.addActionListener(e -> cargarParticipaciones());
    }

    private JPanel construirEncabezado() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);
        panel.add(new EncabezadoSeccion("Rendimiento", "Participación de jugadores por partido"), BorderLayout.NORTH);

        JPanel selectorPartido = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        selectorPartido.setOpaque(false);
        selectorPartido.setBorder(new EmptyBorder(0, 0, Medidas.PADDING_SECCION, 0));
        JLabel lblPartido = new JLabel("Partido:");
        lblPartido.setFont(Tipografia.CUERPO);
        lblPartido.setForeground(ColoresBlaugrana.GRIS_TEXTO);
        selectorPartido.add(lblPartido);
        selectorPartido.add(cmbPartido);
        panel.add(selectorPartido, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel construirFilaResumen() {
        JPanel fila = new JPanel(new GridLayout(1, 4, Medidas.ESPACIO_ENTRE_TARJETAS, Medidas.ESPACIO_ENTRE_TARJETAS));
        fila.setOpaque(false);
        fila.setMaximumSize(new Dimension(Integer.MAX_VALUE, 100));
        fila.add(tarjetaJugadoresUtilizados);
        fila.add(tarjetaGolesPartido);
        fila.add(tarjetaAsistenciasPartido);
        fila.add(tarjetaTarjetasPartido);
        return fila;
    }

    private JPanel construirTarjetaTabla() {
        JPanel contenedor = new JPanel(new BorderLayout());
        contenedor.setBackground(ColoresBlaugrana.BLANCO);
        contenedor.setBorder(BorderFactory.createLineBorder(ColoresBlaugrana.GRIS_MEDIO, 1));
        contenedor.add(new JScrollPane(tabla), BorderLayout.CENTER);
        return contenedor;
    }

    private JPanel construirTarjetaGrafico() {
        JPanel contenedor = new JPanel(new BorderLayout());
        contenedor.setBackground(ColoresBlaugrana.BLANCO);
        contenedor.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(ColoresBlaugrana.GRIS_MEDIO, 1),
                new EmptyBorder(Medidas.PADDING_TARJETA, Medidas.PADDING_TARJETA,
                        Medidas.PADDING_TARJETA, Medidas.PADDING_TARJETA)));
        contenedor.setPreferredSize(new Dimension(300, 0));

        JScrollPane scroll = new JScrollPane(graficoMinutos);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        contenedor.add(scroll, BorderLayout.CENTER);
        return contenedor;
    }

    private JPanel construirFormulario() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(ColoresBlaugrana.BLANCO);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(ColoresBlaugrana.GRIS_MEDIO, 1),
                BorderFactory.createTitledBorder("Registrar participacion")));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 4, 4, 4);
        gbc.anchor = GridBagConstraints.WEST;

        campo(panel, gbc, 0, 0, "Jugador:", cmbJugador);
        campo(panel, gbc, 2, 0, "Minutos:", txtMinutos);
        campo(panel, gbc, 4, 0, "Goles:", txtGoles);

        campo(panel, gbc, 0, 1, "Asistencias:", txtAsistencias);
        campo(panel, gbc, 2, 1, "T. Amarillas:", txtAmarillas);
        campo(panel, gbc, 4, 1, "T. Rojas:", txtRojas);

        gbc.gridx = 0;
        gbc.gridy = 2;
        panel.add(chkTitular, gbc);

        JButton btnNuevo = new JButton("Nuevo");
        JButton btnGuardar = new JButton("Guardar");
        JButton btnEliminar = new JButton("Eliminar");

        btnNuevo.addActionListener(e -> limpiarFormulario());
        btnGuardar.addActionListener(e -> guardar());
        btnEliminar.addActionListener(e -> eliminar());
        PermisosUI.deshabilitarSiSoloLectura(btnGuardar, btnEliminar);

        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        panelBotones.setOpaque(false);
        panelBotones.add(btnNuevo);
        panelBotones.add(btnGuardar);
        panelBotones.add(btnEliminar);

        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 6;
        panel.add(panelBotones, gbc);
        return panel;
    }

    private void campo(JPanel panel, GridBagConstraints gbc, int x, int y, String etiqueta, JComponent campo) {
        gbc.gridwidth = 1;
        gbc.gridx = x;
        gbc.gridy = y;
        panel.add(new JLabel(etiqueta), gbc);
        gbc.gridx = x + 1;
        panel.add(campo, gbc);
    }

    private void cargarSeleccion() {
        int fila = tabla.getSelectedRow();
        if (fila < 0) {
            return;
        }
        idSeleccionado = (Integer) modeloTabla.getValueAt(fila, 0);
        txtMinutos.setText(String.valueOf(modeloTabla.getValueAt(fila, 2)));
        txtGoles.setText(String.valueOf(modeloTabla.getValueAt(fila, 3)));
        txtAsistencias.setText(String.valueOf(modeloTabla.getValueAt(fila, 4)));
        txtAmarillas.setText(String.valueOf(modeloTabla.getValueAt(fila, 5)));
        txtRojas.setText(String.valueOf(modeloTabla.getValueAt(fila, 6)));
        chkTitular.setSelected(Boolean.TRUE.equals(modeloTabla.getValueAt(fila, 7)));
        String nombreJugador = (String) modeloTabla.getValueAt(fila, 1);
        for (int i = 0; i < cmbJugador.getItemCount(); i++) {
            if (cmbJugador.getItemAt(i).getNombreCompleto().equals(nombreJugador)) {
                cmbJugador.setSelectedIndex(i);
                break;
            }
        }
    }

    private void limpiarFormulario() {
        idSeleccionado = null;
        tabla.clearSelection();
        txtMinutos.setText("90");
        txtGoles.setText("0");
        txtAsistencias.setText("0");
        txtAmarillas.setText("0");
        txtRojas.setText("0");
        chkTitular.setSelected(true);
    }

    private void guardar() {
        Partido partido = (Partido) cmbPartido.getSelectedItem();
        Jugador jugador = (Jugador) cmbJugador.getSelectedItem();
        if (partido == null || jugador == null) {
            Mensajes.error(this, "Seleccione un partido y un jugador.");
            return;
        }
        try {
            ParticipacionPartido participacion = new ParticipacionPartido();
            if (idSeleccionado != null) {
                participacion.setIdParticipacion(idSeleccionado);
            }
            participacion.setIdPartido(partido.getIdPartido());
            participacion.setIdJugador(jugador.getIdJugador());
            participacion.setMinutosJugados(Integer.parseInt(txtMinutos.getText().trim()));
            participacion.setGoles(Integer.parseInt(txtGoles.getText().trim()));
            participacion.setAsistencias(Integer.parseInt(txtAsistencias.getText().trim()));
            participacion.setTarjetasAmarillas(Integer.parseInt(txtAmarillas.getText().trim()));
            participacion.setTarjetasRojas(Integer.parseInt(txtRojas.getText().trim()));
            participacion.setTitular(chkTitular.isSelected());

            rendimientoService.guardar(participacion);
            limpiarFormulario();
            cargarParticipaciones();
        } catch (NumberFormatException e) {
            Mensajes.error(this, "Minutos, goles, asistencias y tarjetas deben ser numeros.");
        } catch (IllegalStateException e) {
            Mensajes.error(this, e.getMessage());
        } catch (SQLException e) {
            Mensajes.error(this, "No se pudo guardar la participacion", e);
        }
    }

    private void eliminar() {
        if (idSeleccionado == null) {
            Mensajes.error(this, "Seleccione un registro de la tabla.");
            return;
        }
        try {
            rendimientoService.eliminar(idSeleccionado);
            limpiarFormulario();
            cargarParticipaciones();
        } catch (SQLException e) {
            Mensajes.error(this, "No se pudo eliminar el registro", e);
        }
    }

    private void cargarParticipaciones() {
        modeloTabla.setRowCount(0);
        Partido partido = (Partido) cmbPartido.getSelectedItem();
        if (partido == null) {
            actualizarResumen(List.of());
            return;
        }
        try {
            List<ParticipacionPartido> participaciones = rendimientoService.listarPorPartido(partido.getIdPartido());
            for (ParticipacionPartido p : participaciones) {
                modeloTabla.addRow(new Object[]{p.getIdParticipacion(), p.getNombreJugador(), p.getMinutosJugados(),
                        p.getGoles(), p.getAsistencias(), p.getTarjetasAmarillas(), p.getTarjetasRojas(), p.isTitular()});
            }
            actualizarResumen(participaciones);
        } catch (SQLException e) {
            Mensajes.error(this, "No se pudo cargar la participacion del partido", e);
        }
    }

    private void actualizarResumen(List<ParticipacionPartido> participaciones) {
        int goles = 0;
        int asistencias = 0;
        int tarjetas = 0;
        List<String> etiquetas = new ArrayList<>();
        List<Double> minutos = new ArrayList<>();
        for (ParticipacionPartido p : participaciones) {
            goles += p.getGoles();
            asistencias += p.getAsistencias();
            tarjetas += p.getTarjetasAmarillas() + p.getTarjetasRojas();
            etiquetas.add(p.getNombreJugador());
            minutos.add((double) p.getMinutosJugados());
        }
        tarjetaJugadoresUtilizados.setValor(String.valueOf(participaciones.size()));
        tarjetaGolesPartido.setValor(String.valueOf(goles));
        tarjetaAsistenciasPartido.setValor(String.valueOf(asistencias));
        tarjetaTarjetasPartido.setValor(String.valueOf(tarjetas));
        graficoMinutos.setDatos("Minutos por jugador", etiquetas, minutos);
    }

    @Override
    public void refrescar() {
        try {
            Partido seleccionActual = (Partido) cmbPartido.getSelectedItem();
            cmbPartido.removeAllItems();
            for (Partido p : partidoService.listar()) {
                cmbPartido.addItem(p);
            }
            if (seleccionActual != null) {
                for (int i = 0; i < cmbPartido.getItemCount(); i++) {
                    if (cmbPartido.getItemAt(i).getIdPartido() == seleccionActual.getIdPartido()) {
                        cmbPartido.setSelectedIndex(i);
                    }
                }
            }

            cmbJugador.removeAllItems();
            for (Jugador j : jugadorService.listarActivos()) {
                cmbJugador.addItem(j);
            }
            limpiarFormulario();
            cargarParticipaciones();
        } catch (SQLException e) {
            Mensajes.error(this, "No se pudo cargar la informacion de rendimiento", e);
        }
    }
}
