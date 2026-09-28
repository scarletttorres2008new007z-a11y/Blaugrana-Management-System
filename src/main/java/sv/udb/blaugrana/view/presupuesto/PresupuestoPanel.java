package sv.udb.blaugrana.view.presupuesto;

import sv.udb.blaugrana.model.DetallePresupuesto;
import sv.udb.blaugrana.model.Presupuesto;
import sv.udb.blaugrana.service.PresupuestoService;
import sv.udb.blaugrana.util.ColoresBlaugrana;
import sv.udb.blaugrana.util.FormatoMoneda;
import sv.udb.blaugrana.util.Mensajes;
import sv.udb.blaugrana.util.PermisosUI;
import sv.udb.blaugrana.util.Validaciones;
import sv.udb.blaugrana.view.Refrescable;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.util.List;

public class PresupuestoPanel extends JPanel implements Refrescable {

    private final PresupuestoService presupuestoService = new PresupuestoService();

    private final JComboBox<Presupuesto> cmbPresupuesto = new JComboBox<>();
    private final DefaultTableModel modeloTabla = new DefaultTableModel(
            new Object[]{"ID", "Categoria", "Presupuestado", "Ejecutado", "Disponible"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final JTable tabla = new JTable(modeloTabla);

    private final JTextField txtTemporada = new JTextField(10);
    private final JTextField txtNombrePresupuesto = new JTextField(20);
    private final JTextField txtCategoria = new JTextField(14);
    private final JTextField txtPresupuestado = new JTextField(10);
    private final JTextField txtEjecutado = new JTextField(10);

    public PresupuestoPanel() {
        setLayout(new BorderLayout());
        setBorder(new EmptyBorder(15, 15, 15, 15));

        JLabel titulo = new JLabel("PRESUPUESTO - Planificacion financiera por temporada");
        titulo.setFont(new Font("SansSerif", Font.BOLD, 18));
        titulo.setForeground(ColoresBlaugrana.AZUL_OSCURO);
        add(titulo, BorderLayout.NORTH);

        JPanel selector = new JPanel(new FlowLayout(FlowLayout.LEFT));
        selector.add(new JLabel("Presupuesto:"));
        selector.add(cmbPresupuesto);
        cmbPresupuesto.addActionListener(e -> cargarDetalle());

        tabla.setRowHeight(24);

        JPanel centro = new JPanel(new BorderLayout());
        centro.add(selector, BorderLayout.NORTH);
        centro.add(new JScrollPane(tabla), BorderLayout.CENTER);

        add(centro, BorderLayout.CENTER);
        add(construirFormulario(), BorderLayout.SOUTH);
    }

    private JPanel construirFormulario() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Nuevo presupuesto / detalle"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 4, 4, 4);
        gbc.anchor = GridBagConstraints.WEST;

        campo(panel, gbc, 0, 0, "Temporada (ej. 2027/2028):", txtTemporada);
        campo(panel, gbc, 2, 0, "Nombre presupuesto:", txtNombrePresupuesto);
        JButton btnNuevoPresupuesto = new JButton("Crear presupuesto");
        btnNuevoPresupuesto.addActionListener(e -> crearPresupuesto());
        gbc.gridx = 4;
        gbc.gridy = 0;
        panel.add(btnNuevoPresupuesto, gbc);

        campo(panel, gbc, 0, 1, "Categoria:", txtCategoria);
        campo(panel, gbc, 2, 1, "Presupuestado:", txtPresupuestado);
        campo(panel, gbc, 4, 1, "Ejecutado:", txtEjecutado);

        JButton btnAgregarDetalle = new JButton("Agregar categoria al presupuesto");
        btnAgregarDetalle.addActionListener(e -> agregarDetalle());
        PermisosUI.deshabilitarSiSoloLectura(btnNuevoPresupuesto, btnAgregarDetalle);

        JButton btnRefrescar = new JButton("Refrescar");
        btnRefrescar.addActionListener(e -> refrescar());

        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        panelBotones.add(btnAgregarDetalle);
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

    private void crearPresupuesto() {
        if (Validaciones.esVacio(txtTemporada.getText()) || Validaciones.esVacio(txtNombrePresupuesto.getText())) {
            Mensajes.error(this, "Ingrese la temporada y el nombre del presupuesto.");
            return;
        }
        try {
            Presupuesto presupuesto = new Presupuesto();
            presupuesto.setTemporada(txtTemporada.getText().trim());
            presupuesto.setNombre(txtNombrePresupuesto.getText().trim());
            presupuestoService.guardarPresupuesto(presupuesto);
            txtTemporada.setText("");
            txtNombrePresupuesto.setText("");
            refrescar();
        } catch (SQLException e) {
            Mensajes.error(this, "No se pudo crear el presupuesto", e);
        }
    }

    private void agregarDetalle() {
        Presupuesto presupuesto = (Presupuesto) cmbPresupuesto.getSelectedItem();
        if (presupuesto == null) {
            Mensajes.error(this, "Cree o seleccione un presupuesto primero.");
            return;
        }
        if (Validaciones.esVacio(txtCategoria.getText())) {
            Mensajes.error(this, "Ingrese la categoria.");
            return;
        }
        try {
            DetallePresupuesto detalle = new DetallePresupuesto();
            detalle.setIdPresupuesto(presupuesto.getIdPresupuesto());
            detalle.setCategoria(txtCategoria.getText().trim());
            detalle.setMontoPresupuestado(Validaciones.aBigDecimal(txtPresupuestado.getText()));
            detalle.setMontoEjecutado(Validaciones.aBigDecimal(txtEjecutado.getText()));

            presupuestoService.guardarDetalle(detalle);
            txtCategoria.setText("");
            txtPresupuestado.setText("");
            txtEjecutado.setText("");
            cargarDetalle();
        } catch (NumberFormatException e) {
            Mensajes.error(this, "Los montos deben ser numericos.");
        } catch (SQLException e) {
            Mensajes.error(this, "No se pudo agregar el detalle", e);
        }
    }

    private void cargarDetalle() {
        modeloTabla.setRowCount(0);
        Presupuesto presupuesto = (Presupuesto) cmbPresupuesto.getSelectedItem();
        if (presupuesto == null) {
            return;
        }
        try {
            List<DetallePresupuesto> detalles = presupuestoService.listarDetalle(presupuesto.getIdPresupuesto());
            for (DetallePresupuesto d : detalles) {
                modeloTabla.addRow(new Object[]{d.getIdDetalle(), d.getCategoria(),
                        FormatoMoneda.formatear(d.getMontoPresupuestado()), FormatoMoneda.formatear(d.getMontoEjecutado()),
                        FormatoMoneda.formatear(d.getDisponible())});
            }
        } catch (SQLException e) {
            Mensajes.error(this, "No se pudo cargar el detalle del presupuesto", e);
        }
    }

    @Override
    public void refrescar() {
        try {
            Presupuesto seleccionActual = (Presupuesto) cmbPresupuesto.getSelectedItem();
            cmbPresupuesto.removeAllItems();
            for (Presupuesto p : presupuestoService.listar()) {
                cmbPresupuesto.addItem(p);
            }
            if (seleccionActual != null) {
                for (int i = 0; i < cmbPresupuesto.getItemCount(); i++) {
                    if (cmbPresupuesto.getItemAt(i).getIdPresupuesto() == seleccionActual.getIdPresupuesto()) {
                        cmbPresupuesto.setSelectedIndex(i);
                    }
                }
            }
            cargarDetalle();
        } catch (SQLException e) {
            Mensajes.error(this, "No se pudo cargar los presupuestos", e);
        }
    }
}
