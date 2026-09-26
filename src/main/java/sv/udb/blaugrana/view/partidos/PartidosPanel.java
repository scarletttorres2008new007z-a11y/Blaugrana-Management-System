package sv.udb.blaugrana.view.partidos;

import sv.udb.blaugrana.model.Partido;
import sv.udb.blaugrana.service.BonificacionService;
import sv.udb.blaugrana.service.PartidoService;
import sv.udb.blaugrana.util.ColoresBlaugrana;
import sv.udb.blaugrana.util.Mensajes;
import sv.udb.blaugrana.util.Validaciones;
import sv.udb.blaugrana.view.Refrescable;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;

public class PartidosPanel extends JPanel implements Refrescable {

    private final PartidoService partidoService = new PartidoService();
    private final BonificacionService bonificacionService = new BonificacionService();

    private final DefaultTableModel modeloTabla = new DefaultTableModel(
            new Object[]{"ID", "Competicion", "Fecha", "Rival", "Condicion", "Marcador", "Resultado", "Estado"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final JTable tabla = new JTable(modeloTabla);

    private final JTextField txtCompeticion = new JTextField(16);
    private final JTextField txtFecha = new JTextField(10);
    private final JTextField txtRival = new JTextField(16);
    private final JComboBox<String> cmbCondicion = new JComboBox<>(new String[]{"LOCAL", "VISITANTE"});
    private final JTextField txtGolesFavor = new JTextField(4);
    private final JTextField txtGolesContra = new JTextField(4);
    private final JComboBox<String> cmbEstado = new JComboBox<>(new String[]{"PROGRAMADO", "FINALIZADO"});

    private Integer idSeleccionado;

    public PartidosPanel() {
        setLayout(new BorderLayout());
        setBorder(new EmptyBorder(15, 15, 15, 15));

        JLabel titulo = new JLabel("PARTIDOS - Calendario y resultados de FC Barcelona");
        titulo.setFont(new Font("SansSerif", Font.BOLD, 18));
        titulo.setForeground(ColoresBlaugrana.AZUL_OSCURO);
        add(titulo, BorderLayout.NORTH);

        tabla.setRowHeight(24);
        tabla.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                cargarSeleccion();
            }
        });
        add(new JScrollPane(tabla), BorderLayout.CENTER);
        add(construirFormulario(), BorderLayout.SOUTH);
    }

    private JPanel construirFormulario() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Datos del partido"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 4, 4, 4);
        gbc.anchor = GridBagConstraints.WEST;

        campo(panel, gbc, 0, 0, "Competicion:", txtCompeticion);
        campo(panel, gbc, 2, 0, "Fecha (yyyy-mm-dd):", txtFecha);
        campo(panel, gbc, 4, 0, "Rival:", txtRival);

        campo(panel, gbc, 0, 1, "Condicion:", cmbCondicion);
        campo(panel, gbc, 2, 1, "Goles a favor:", txtGolesFavor);
        campo(panel, gbc, 4, 1, "Goles en contra:", txtGolesContra);

        campo(panel, gbc, 0, 2, "Estado:", cmbEstado);

        JButton btnNuevo = new JButton("Nuevo");
        JButton btnGuardar = new JButton("Guardar");
        JButton btnEliminar = new JButton("Eliminar");
        JButton btnGenerarBonificaciones = new JButton("Generar bonificaciones");
        JButton btnRefrescar = new JButton("Refrescar");

        btnNuevo.addActionListener(e -> limpiarFormulario());
        btnGuardar.addActionListener(e -> guardar());
        btnEliminar.addActionListener(e -> eliminar());
        btnGenerarBonificaciones.addActionListener(e -> generarBonificaciones());
        btnRefrescar.addActionListener(e -> refrescar());

        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        panelBotones.add(btnNuevo);
        panelBotones.add(btnGuardar);
        panelBotones.add(btnEliminar);
        panelBotones.add(btnGenerarBonificaciones);
        panelBotones.add(btnRefrescar);

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
        try {
            partidoService.buscarPorId(idSeleccionado).ifPresent(p -> {
                txtCompeticion.setText(p.getCompeticion());
                txtFecha.setText(p.getFecha().toString());
                txtRival.setText(p.getRival());
                cmbCondicion.setSelectedItem(p.getCondicion());
                txtGolesFavor.setText(String.valueOf(p.getGolesFavor()));
                txtGolesContra.setText(String.valueOf(p.getGolesContra()));
                cmbEstado.setSelectedItem(p.getEstado());
            });
        } catch (SQLException e) {
            Mensajes.error(this, "No se pudo cargar el partido:\n" + e.getMessage());
        }
    }

    private void limpiarFormulario() {
        idSeleccionado = null;
        tabla.clearSelection();
        txtCompeticion.setText("");
        txtFecha.setText(LocalDate.now().toString());
        txtRival.setText("");
        cmbCondicion.setSelectedIndex(0);
        txtGolesFavor.setText("0");
        txtGolesContra.setText("0");
        cmbEstado.setSelectedIndex(0);
    }

    private void guardar() {
        if (Validaciones.esVacio(txtCompeticion.getText()) || Validaciones.esVacio(txtRival.getText())) {
            Mensajes.error(this, "La competicion y el rival son obligatorios.");
            return;
        }
        try {
            Partido partido = new Partido();
            if (idSeleccionado != null) {
                partido.setIdPartido(idSeleccionado);
            }
            partido.setCompeticion(txtCompeticion.getText().trim());
            partido.setFecha(LocalDate.parse(txtFecha.getText().trim()));
            partido.setRival(txtRival.getText().trim());
            partido.setCondicion((String) cmbCondicion.getSelectedItem());
            partido.setGolesFavor(Integer.parseInt(txtGolesFavor.getText().trim()));
            partido.setGolesContra(Integer.parseInt(txtGolesContra.getText().trim()));
            partido.setEstado((String) cmbEstado.getSelectedItem());

            partidoService.guardar(partido);
            Mensajes.info(this, "Partido guardado correctamente.");
            limpiarFormulario();
            refrescar();
        } catch (DateTimeParseException e) {
            Mensajes.error(this, "La fecha debe tener el formato yyyy-mm-dd.");
        } catch (NumberFormatException e) {
            Mensajes.error(this, "Los goles deben ser valores numericos.");
        } catch (SQLException e) {
            Mensajes.error(this, "No se pudo guardar el partido:\n" + e.getMessage());
        }
    }

    private void eliminar() {
        if (idSeleccionado == null) {
            Mensajes.error(this, "Seleccione un partido de la tabla.");
            return;
        }
        if (!Mensajes.confirmar(this, "¿Desea eliminar el partido seleccionado?")) {
            return;
        }
        try {
            partidoService.eliminar(idSeleccionado);
            limpiarFormulario();
            refrescar();
        } catch (SQLException e) {
            Mensajes.error(this, "No se pudo eliminar el partido:\n" + e.getMessage());
        }
    }

    private void generarBonificaciones() {
        if (idSeleccionado == null) {
            Mensajes.error(this, "Seleccione un partido finalizado de la tabla.");
            return;
        }
        try {
            int generadas = bonificacionService.generarBonificacionesPartido(idSeleccionado);
            Mensajes.info(this, "Se generaron " + generadas + " bonificaciones para este partido.");
        } catch (IllegalStateException | IllegalArgumentException e) {
            Mensajes.error(this, e.getMessage());
        } catch (SQLException e) {
            Mensajes.error(this, "No se pudieron generar las bonificaciones:\n" + e.getMessage());
        }
    }

    @Override
    public void refrescar() {
        modeloTabla.setRowCount(0);
        try {
            List<Partido> partidos = partidoService.listar();
            for (Partido p : partidos) {
                modeloTabla.addRow(new Object[]{p.getIdPartido(), p.getCompeticion(), p.getFecha(), p.getRival(),
                        p.getCondicion(), p.getMarcador(), p.getResultado(), p.getEstado()});
            }
        } catch (SQLException e) {
            Mensajes.error(this, "No se pudo cargar los partidos:\n" + e.getMessage());
        }
    }
}
