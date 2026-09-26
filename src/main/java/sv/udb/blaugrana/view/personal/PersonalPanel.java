package sv.udb.blaugrana.view.personal;

import sv.udb.blaugrana.model.Personal;
import sv.udb.blaugrana.service.PersonalService;
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

public class PersonalPanel extends JPanel implements Refrescable {

    private final PersonalService personalService = new PersonalService();

    private final DefaultTableModel modeloTabla = new DefaultTableModel(
            new Object[]{"ID", "Nombre completo", "Cargo", "Area", "Nacionalidad", "Estado"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final JTable tabla = new JTable(modeloTabla);

    private final JTextField txtNombre = new JTextField(14);
    private final JTextField txtApellido = new JTextField(14);
    private final JTextField txtDocumento = new JTextField(12);
    private final JTextField txtCargo = new JTextField(18);
    private final JTextField txtArea = new JTextField(14);
    private final JTextField txtNacionalidad = new JTextField(12);
    private final JTextField txtFechaIngreso = new JTextField(10);
    private final JComboBox<String> cmbEstado = new JComboBox<>(new String[]{"ACTIVO", "INACTIVO"});

    private Integer idSeleccionado;

    public PersonalPanel() {
        setLayout(new BorderLayout());
        setBorder(new EmptyBorder(15, 15, 15, 15));

        JLabel titulo = new JLabel("PERSONAL - Cuerpo tecnico y staff del club");
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
        panel.setBorder(BorderFactory.createTitledBorder("Datos del personal"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 4, 4, 4);
        gbc.anchor = GridBagConstraints.WEST;

        campo(panel, gbc, 0, 0, "Nombre:", txtNombre);
        campo(panel, gbc, 2, 0, "Apellido:", txtApellido);
        campo(panel, gbc, 4, 0, "Documento:", txtDocumento);

        campo(panel, gbc, 0, 1, "Cargo:", txtCargo);
        campo(panel, gbc, 2, 1, "Area:", txtArea);
        campo(panel, gbc, 4, 1, "Nacionalidad:", txtNacionalidad);

        campo(panel, gbc, 0, 2, "Fecha ingreso (yyyy-mm-dd):", txtFechaIngreso);
        campoComponente(panel, gbc, 2, 2, "Estado:", cmbEstado);

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

    private void campo(JPanel panel, GridBagConstraints gbc, int x, int y, String etiqueta, JTextField campo) {
        campoComponente(panel, gbc, x, y, etiqueta, campo);
    }

    private void campoComponente(JPanel panel, GridBagConstraints gbc, int x, int y, String etiqueta, JComponent campo) {
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
            personalService.listar().stream()
                    .filter(p -> p.getIdPersonal() == idSeleccionado)
                    .findFirst()
                    .ifPresent(p -> {
                        txtNombre.setText(p.getNombre());
                        txtApellido.setText(p.getApellido());
                        txtDocumento.setText(p.getDocumento());
                        txtCargo.setText(p.getCargo());
                        txtArea.setText(p.getArea());
                        txtNacionalidad.setText(p.getNacionalidad());
                        txtFechaIngreso.setText(p.getFechaIngreso() != null ? p.getFechaIngreso().toString() : "");
                        cmbEstado.setSelectedItem(p.getEstado());
                    });
        } catch (SQLException e) {
            Mensajes.error(this, "No se pudo cargar el registro:\n" + e.getMessage());
        }
    }

    private void limpiarFormulario() {
        idSeleccionado = null;
        tabla.clearSelection();
        txtNombre.setText("");
        txtApellido.setText("");
        txtDocumento.setText("");
        txtCargo.setText("");
        txtArea.setText("");
        txtNacionalidad.setText("");
        txtFechaIngreso.setText(LocalDate.now().toString());
        cmbEstado.setSelectedIndex(0);
    }

    private void guardar() {
        if (Validaciones.esVacio(txtNombre.getText()) || Validaciones.esVacio(txtApellido.getText())
                || Validaciones.esVacio(txtCargo.getText())) {
            Mensajes.error(this, "Nombre, apellido y cargo son obligatorios.");
            return;
        }
        try {
            Personal personal = new Personal();
            if (idSeleccionado != null) {
                personal.setIdPersonal(idSeleccionado);
            }
            personal.setNombre(txtNombre.getText().trim());
            personal.setApellido(txtApellido.getText().trim());
            personal.setDocumento(txtDocumento.getText().trim());
            personal.setCargo(txtCargo.getText().trim());
            personal.setArea(txtArea.getText().trim());
            personal.setNacionalidad(txtNacionalidad.getText().trim());
            personal.setEstado((String) cmbEstado.getSelectedItem());
            LocalDate fechaIngreso = Validaciones.esVacio(txtFechaIngreso.getText())
                    ? LocalDate.now() : LocalDate.parse(txtFechaIngreso.getText().trim());
            personal.setFechaIngreso(fechaIngreso);

            personalService.guardar(personal);
            Mensajes.info(this, "Registro guardado correctamente.");
            limpiarFormulario();
            refrescar();
        } catch (DateTimeParseException e) {
            Mensajes.error(this, "La fecha de ingreso debe tener el formato yyyy-mm-dd.");
        } catch (SQLException e) {
            Mensajes.error(this, "No se pudo guardar el registro:\n" + e.getMessage());
        }
    }

    private void eliminar() {
        if (idSeleccionado == null) {
            Mensajes.error(this, "Seleccione un registro de la tabla.");
            return;
        }
        if (!Mensajes.confirmar(this, "¿Desea eliminar el registro seleccionado?")) {
            return;
        }
        try {
            personalService.eliminar(idSeleccionado);
            limpiarFormulario();
            refrescar();
        } catch (SQLException e) {
            Mensajes.error(this, "No se pudo eliminar el registro:\n" + e.getMessage());
        }
    }

    @Override
    public void refrescar() {
        modeloTabla.setRowCount(0);
        try {
            List<Personal> lista = personalService.listar();
            for (Personal p : lista) {
                modeloTabla.addRow(new Object[]{p.getIdPersonal(), p.getNombreCompleto(), p.getCargo(),
                        p.getArea(), p.getNacionalidad(), p.getEstado()});
            }
        } catch (SQLException e) {
            Mensajes.error(this, "No se pudo cargar el personal:\n" + e.getMessage());
        }
    }
}
