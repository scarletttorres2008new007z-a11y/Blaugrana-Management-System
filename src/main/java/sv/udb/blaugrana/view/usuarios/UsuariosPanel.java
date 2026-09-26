package sv.udb.blaugrana.view.usuarios;

import sv.udb.blaugrana.model.Rol;
import sv.udb.blaugrana.model.Usuario;
import sv.udb.blaugrana.service.UsuarioService;
import sv.udb.blaugrana.util.ColoresBlaugrana;
import sv.udb.blaugrana.util.Mensajes;
import sv.udb.blaugrana.util.Validaciones;
import sv.udb.blaugrana.view.Refrescable;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.util.List;

public class UsuariosPanel extends JPanel implements Refrescable {

    private final UsuarioService usuarioService = new UsuarioService();

    private final DefaultTableModel modeloTabla = new DefaultTableModel(
            new Object[]{"ID", "Usuario", "Nombre completo", "Rol", "Correo", "Estado"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final JTable tabla = new JTable(modeloTabla);

    private final JTextField txtNombreUsuario = new JTextField(14);
    private final JPasswordField txtContrasena = new JPasswordField(14);
    private final JTextField txtNombreCompleto = new JTextField(18);
    private final JTextField txtCorreo = new JTextField(18);
    private final JComboBox<Rol> cmbRol = new JComboBox<>();
    private final JComboBox<String> cmbEstado = new JComboBox<>(new String[]{"ACTIVO", "INACTIVO"});

    private Integer idSeleccionado;

    public UsuariosPanel() {
        setLayout(new BorderLayout());
        setBorder(new EmptyBorder(15, 15, 15, 15));

        JLabel titulo = new JLabel("USUARIOS - Administracion de accesos al sistema");
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
        panel.setBorder(BorderFactory.createTitledBorder("Datos del usuario"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 4, 4, 4);
        gbc.anchor = GridBagConstraints.WEST;

        campo(panel, gbc, 0, 0, "Usuario:", txtNombreUsuario);
        campo(panel, gbc, 2, 0, "Contrasena (solo al crear):", txtContrasena);
        campo(panel, gbc, 4, 0, "Rol:", cmbRol);

        campo(panel, gbc, 0, 1, "Nombre completo:", txtNombreCompleto);
        campo(panel, gbc, 2, 1, "Correo:", txtCorreo);
        campo(panel, gbc, 4, 1, "Estado:", cmbEstado);

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
        gbc.gridy = 2;
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
            usuarioService.listar().stream()
                    .filter(u -> u.getIdUsuario() == idSeleccionado)
                    .findFirst()
                    .ifPresent(u -> {
                        txtNombreUsuario.setText(u.getNombreUsuario());
                        txtNombreUsuario.setEditable(false);
                        txtContrasena.setText("");
                        txtNombreCompleto.setText(u.getNombreCompleto());
                        txtCorreo.setText(u.getCorreo());
                        seleccionarRol(u.getIdRol());
                        cmbEstado.setSelectedItem(u.getEstado());
                    });
        } catch (SQLException e) {
            Mensajes.error(this, "No se pudo cargar el usuario:\n" + e.getMessage());
        }
    }

    private void seleccionarRol(int idRol) {
        for (int i = 0; i < cmbRol.getItemCount(); i++) {
            if (cmbRol.getItemAt(i).getIdRol() == idRol) {
                cmbRol.setSelectedIndex(i);
                return;
            }
        }
    }

    private void limpiarFormulario() {
        idSeleccionado = null;
        tabla.clearSelection();
        txtNombreUsuario.setText("");
        txtNombreUsuario.setEditable(true);
        txtContrasena.setText("");
        txtNombreCompleto.setText("");
        txtCorreo.setText("");
        cmbEstado.setSelectedIndex(0);
    }

    private void guardar() {
        Rol rol = (Rol) cmbRol.getSelectedItem();
        if (Validaciones.esVacio(txtNombreUsuario.getText()) || Validaciones.esVacio(txtNombreCompleto.getText()) || rol == null) {
            Mensajes.error(this, "Usuario, nombre completo y rol son obligatorios.");
            return;
        }
        try {
            if (idSeleccionado == null) {
                String contrasena = new String(txtContrasena.getPassword());
                if (Validaciones.esVacio(contrasena)) {
                    Mensajes.error(this, "Debe ingresar una contrasena para el nuevo usuario.");
                    return;
                }
                Usuario usuario = new Usuario();
                usuario.setNombreUsuario(txtNombreUsuario.getText().trim());
                usuario.setIdRol(rol.getIdRol());
                usuario.setNombreCompleto(txtNombreCompleto.getText().trim());
                usuario.setCorreo(txtCorreo.getText().trim());
                usuario.setEstado((String) cmbEstado.getSelectedItem());
                usuarioService.crear(usuario, contrasena);
            } else {
                Usuario usuario = new Usuario();
                usuario.setIdUsuario(idSeleccionado);
                usuario.setIdRol(rol.getIdRol());
                usuario.setNombreCompleto(txtNombreCompleto.getText().trim());
                usuario.setCorreo(txtCorreo.getText().trim());
                usuario.setEstado((String) cmbEstado.getSelectedItem());
                usuarioService.actualizar(usuario);

                String nuevaContrasena = new String(txtContrasena.getPassword());
                if (!Validaciones.esVacio(nuevaContrasena)) {
                    usuarioService.cambiarContrasena(idSeleccionado, nuevaContrasena);
                }
            }
            Mensajes.info(this, "Usuario guardado correctamente.");
            limpiarFormulario();
            refrescar();
        } catch (SQLException e) {
            Mensajes.error(this, "No se pudo guardar el usuario:\n" + e.getMessage());
        }
    }

    private void eliminar() {
        if (idSeleccionado == null) {
            Mensajes.error(this, "Seleccione un usuario de la tabla.");
            return;
        }
        if (!Mensajes.confirmar(this, "¿Desea eliminar el usuario seleccionado?")) {
            return;
        }
        try {
            usuarioService.eliminar(idSeleccionado);
            limpiarFormulario();
            refrescar();
        } catch (SQLException e) {
            Mensajes.error(this, "No se pudo eliminar el usuario:\n" + e.getMessage());
        }
    }

    @Override
    public void refrescar() {
        try {
            cmbRol.removeAllItems();
            for (Rol r : usuarioService.listarRoles()) {
                cmbRol.addItem(r);
            }

            modeloTabla.setRowCount(0);
            List<Usuario> usuarios = usuarioService.listar();
            for (Usuario u : usuarios) {
                modeloTabla.addRow(new Object[]{u.getIdUsuario(), u.getNombreUsuario(), u.getNombreCompleto(),
                        u.getNombreRol(), u.getCorreo(), u.getEstado()});
            }
        } catch (SQLException e) {
            Mensajes.error(this, "No se pudo cargar los usuarios:\n" + e.getMessage());
        }
    }
}
