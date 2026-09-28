package sv.udb.blaugrana.view.rendimiento;

import sv.udb.blaugrana.model.Jugador;
import sv.udb.blaugrana.model.ParticipacionPartido;
import sv.udb.blaugrana.model.Partido;
import sv.udb.blaugrana.service.JugadorService;
import sv.udb.blaugrana.service.PartidoService;
import sv.udb.blaugrana.service.RendimientoService;
import sv.udb.blaugrana.util.ColoresBlaugrana;
import sv.udb.blaugrana.util.Mensajes;
import sv.udb.blaugrana.util.PermisosUI;
import sv.udb.blaugrana.view.Refrescable;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.util.List;

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

    private Integer idSeleccionado;

    public RendimientoPanel() {
        setLayout(new BorderLayout());
        setBorder(new EmptyBorder(15, 15, 15, 15));

        JLabel titulo = new JLabel("RENDIMIENTO - Participacion de jugadores por partido");
        titulo.setFont(new Font("SansSerif", Font.BOLD, 18));
        titulo.setForeground(ColoresBlaugrana.AZUL_OSCURO);
        add(titulo, BorderLayout.NORTH);

        JPanel selectorPartido = new JPanel(new FlowLayout(FlowLayout.LEFT));
        selectorPartido.add(new JLabel("Partido:"));
        selectorPartido.add(cmbPartido);
        cmbPartido.addActionListener(e -> cargarParticipaciones());

        tabla.setRowHeight(24);
        tabla.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                cargarSeleccion();
            }
        });

        JPanel centro = new JPanel(new BorderLayout());
        centro.add(selectorPartido, BorderLayout.NORTH);
        centro.add(new JScrollPane(tabla), BorderLayout.CENTER);

        add(centro, BorderLayout.CENTER);
        add(construirFormulario(), BorderLayout.SOUTH);
    }

    private JPanel construirFormulario() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Registrar participacion"));
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
            return;
        }
        try {
            List<ParticipacionPartido> participaciones = rendimientoService.listarPorPartido(partido.getIdPartido());
            for (ParticipacionPartido p : participaciones) {
                modeloTabla.addRow(new Object[]{p.getIdParticipacion(), p.getNombreJugador(), p.getMinutosJugados(),
                        p.getGoles(), p.getAsistencias(), p.getTarjetasAmarillas(), p.getTarjetasRojas(), p.isTitular()});
            }
        } catch (SQLException e) {
            Mensajes.error(this, "No se pudo cargar la participacion del partido", e);
        }
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
