package sv.udb.blaugrana.view.jugadores;

import sv.udb.blaugrana.model.Jugador;
import sv.udb.blaugrana.service.JugadorService;
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

public class JugadoresPanel extends JPanel implements Refrescable {

    private final JugadorService jugadorService = new JugadorService();

    private final DefaultTableModel modeloTabla = new DefaultTableModel(
            new Object[]{"ID", "Nº", "Jugador", "Posicion", "Nacionalidad", "Estado"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final JTable tabla = new JTable(modeloTabla);

    private final JTextField txtNumero = new JTextField(4);
    private final JTextField txtNombre = new JTextField(14);
    private final JTextField txtApellido = new JTextField(14);
    private final JTextField txtDocumento = new JTextField(12);
    private final JTextField txtNacionalidad = new JTextField(12);
    private final JTextField txtFechaNacimiento = new JTextField(10);
    private final JTextField txtFechaIngreso = new JTextField(10);
    private final JComboBox<String> cmbPosicion = new JComboBox<>(
            new String[]{"Portero", "Defensa", "Centrocampista", "Delantero"});
    private final JComboBox<String> cmbEstado = new JComboBox<>(new String[]{"ACTIVO", "INACTIVO"});

    private Integer idSeleccionado;

    public JugadoresPanel() {
        setLayout(new BorderLayout());
        setBorder(new EmptyBorder(15, 15, 15, 15));

        JLabel titulo = new JLabel("PLANTILLA - Gestion de jugadores del primer equipo");
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
        panel.setBorder(BorderFactory.createTitledBorder("Datos del jugador"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 4, 4, 4);
        gbc.anchor = GridBagConstraints.WEST;

        agregarCampo(panel, gbc, 0, 0, "N° camiseta:", txtNumero);
        agregarCampo(panel, gbc, 2, 0, "Nombre:", txtNombre);
        agregarCampo(panel, gbc, 4, 0, "Apellido:", txtApellido);

        agregarCampo(panel, gbc, 0, 1, "Documento:", txtDocumento);
        agregarCampo(panel, gbc, 2, 1, "Nacionalidad:", txtNacionalidad);
        agregarCampoComponente(panel, gbc, 4, 1, "Posicion:", cmbPosicion);

        agregarCampo(panel, gbc, 0, 2, "Fecha nacimiento (yyyy-mm-dd):", txtFechaNacimiento);
        agregarCampo(panel, gbc, 2, 2, "Fecha ingreso (yyyy-mm-dd):", txtFechaIngreso);
        agregarCampoComponente(panel, gbc, 4, 2, "Estado:", cmbEstado);

        JButton btnNuevo = new JButton("Nuevo");
        JButton btnGuardar = new JButton("Guardar");
        JButton btnEliminar = new JButton("Eliminar");
        JButton btnRefrescar = new JButton("Refrescar");

        btnNuevo.addActionListener(e -> limpiarFormulario());
        btnGuardar.addActionListener(e -> guardar());
        btnEliminar.addActionListener(e -> eliminar());
        btnRefrescar.addActionListener(e -> refrescar());

        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        panelBotones.add(btnNuevo);
        panelBotones.add(btnGuardar);
        panelBotones.add(btnEliminar);
        panelBotones.add(btnRefrescar);

        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 6;
        panel.add(panelBotones, gbc);

        return panel;
    }

    private void agregarCampo(JPanel panel, GridBagConstraints gbc, int x, int y, String etiqueta, JTextField campo) {
        agregarCampoComponente(panel, gbc, x, y, etiqueta, campo);
    }

    private void agregarCampoComponente(JPanel panel, GridBagConstraints gbc, int x, int y, String etiqueta, JComponent campo) {
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
            jugadorService.buscarPorId(idSeleccionado).ifPresent(j -> {
                txtNumero.setText(String.valueOf(j.getNumeroCamiseta()));
                txtNombre.setText(j.getNombre());
                txtApellido.setText(j.getApellido());
                txtDocumento.setText(j.getDocumento());
                txtNacionalidad.setText(j.getNacionalidad());
                txtFechaNacimiento.setText(j.getFechaNacimiento() != null ? j.getFechaNacimiento().toString() : "");
                txtFechaIngreso.setText(j.getFechaIngreso() != null ? j.getFechaIngreso().toString() : "");
                cmbPosicion.setSelectedItem(j.getPosicion());
                cmbEstado.setSelectedItem(j.getEstado());
            });
        } catch (SQLException e) {
            Mensajes.error(this, "No se pudo cargar el jugador:\n" + e.getMessage());
        }
    }

    private void limpiarFormulario() {
        idSeleccionado = null;
        tabla.clearSelection();
        txtNumero.setText("");
        txtNombre.setText("");
        txtApellido.setText("");
        txtDocumento.setText("");
        txtNacionalidad.setText("");
        txtFechaNacimiento.setText("");
        txtFechaIngreso.setText(LocalDate.now().toString());
        cmbPosicion.setSelectedIndex(0);
        cmbEstado.setSelectedIndex(0);
    }

    private void guardar() {
        if (Validaciones.esVacio(txtNombre.getText()) || Validaciones.esVacio(txtApellido.getText())) {
            Mensajes.error(this, "El nombre y el apellido son obligatorios.");
            return;
        }
        try {
            Jugador jugador = new Jugador();
            if (idSeleccionado != null) {
                jugador.setIdJugador(idSeleccionado);
            }
            jugador.setNumeroCamiseta(Integer.parseInt(txtNumero.getText().trim()));
            jugador.setNombre(txtNombre.getText().trim());
            jugador.setApellido(txtApellido.getText().trim());
            jugador.setDocumento(txtDocumento.getText().trim());
            jugador.setNacionalidad(txtNacionalidad.getText().trim());
            jugador.setPosicion((String) cmbPosicion.getSelectedItem());
            jugador.setEstado((String) cmbEstado.getSelectedItem());
            jugador.setFechaNacimiento(parseFecha(txtFechaNacimiento.getText()));
            LocalDate fechaIngreso = parseFecha(txtFechaIngreso.getText());
            jugador.setFechaIngreso(fechaIngreso != null ? fechaIngreso : LocalDate.now());

            jugadorService.guardar(jugador);
            Mensajes.info(this, "Jugador guardado correctamente.");
            limpiarFormulario();
            refrescar();
        } catch (NumberFormatException e) {
            Mensajes.error(this, "El numero de camiseta debe ser un valor numerico.");
        } catch (DateTimeParseException e) {
            Mensajes.error(this, "Las fechas deben tener el formato yyyy-mm-dd.");
        } catch (SQLException e) {
            Mensajes.error(this, "No se pudo guardar el jugador:\n" + e.getMessage());
        }
    }

    private void eliminar() {
        if (idSeleccionado == null) {
            Mensajes.error(this, "Seleccione un jugador de la tabla.");
            return;
        }
        if (!Mensajes.confirmar(this, "¿Desea eliminar al jugador seleccionado?")) {
            return;
        }
        try {
            jugadorService.eliminar(idSeleccionado);
            limpiarFormulario();
            refrescar();
        } catch (SQLException e) {
            Mensajes.error(this, "No se pudo eliminar el jugador:\n" + e.getMessage());
        }
    }

    private LocalDate parseFecha(String texto) {
        if (Validaciones.esVacio(texto)) {
            return null;
        }
        return LocalDate.parse(texto.trim());
    }

    @Override
    public void refrescar() {
        modeloTabla.setRowCount(0);
        try {
            List<Jugador> jugadores = jugadorService.listar();
            for (Jugador j : jugadores) {
                modeloTabla.addRow(new Object[]{j.getIdJugador(), j.getNumeroCamiseta(), j.getNombreCompleto(),
                        j.getPosicion(), j.getNacionalidad(), j.getEstado()});
            }
        } catch (SQLException e) {
            Mensajes.error(this, "No se pudo cargar la plantilla:\n" + e.getMessage());
        }
    }
}
