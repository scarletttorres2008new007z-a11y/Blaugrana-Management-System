package sv.udb.blaugrana.view.ingresos;

import sv.udb.blaugrana.model.CategoriaIngreso;
import sv.udb.blaugrana.model.Ingreso;
import sv.udb.blaugrana.service.FinanzasService;
import sv.udb.blaugrana.util.ColoresBlaugrana;
import sv.udb.blaugrana.util.FormatoMoneda;
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

public class IngresosPanel extends JPanel implements Refrescable {

    private final FinanzasService finanzasService = new FinanzasService();

    private final DefaultTableModel modeloTabla = new DefaultTableModel(
            new Object[]{"ID", "Categoria", "Descripcion", "Monto", "Fecha"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final JTable tabla = new JTable(modeloTabla);

    private final JComboBox<CategoriaIngreso> cmbCategoria = new JComboBox<>();
    private final JTextField txtDescripcion = new JTextField(20);
    private final JTextField txtMonto = new JTextField(10);
    private final JTextField txtFecha = new JTextField(10);
    private final JLabel lblTotal = new JLabel("$0.00");

    private Integer idSeleccionado;

    public IngresosPanel() {
        setLayout(new BorderLayout());
        setBorder(new EmptyBorder(15, 15, 15, 15));

        JLabel titulo = new JLabel("INGRESOS - Fuentes de ingreso del club");
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
        panel.setBorder(BorderFactory.createTitledBorder("Registrar ingreso"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 4, 4, 4);
        gbc.anchor = GridBagConstraints.WEST;

        campo(panel, gbc, 0, 0, "Categoria:", cmbCategoria);
        campo(panel, gbc, 2, 0, "Descripcion:", txtDescripcion);
        campo(panel, gbc, 0, 1, "Monto:", txtMonto);
        campo(panel, gbc, 2, 1, "Fecha (yyyy-mm-dd):", txtFecha);

        JButton btnNuevo = new JButton("Nuevo");
        JButton btnGuardar = new JButton("Guardar");
        JButton btnEliminar = new JButton("Eliminar");
        JButton btnRefrescar = new JButton("Refrescar");

        btnNuevo.addActionListener(e -> limpiarFormulario());
        btnGuardar.addActionListener(e -> guardar());
        btnEliminar.addActionListener(e -> eliminar());
        btnRefrescar.addActionListener(e -> refrescar());

        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        panelBotones.add(new JLabel("Total ingresos: "));
        panelBotones.add(lblTotal);
        panelBotones.add(btnNuevo);
        panelBotones.add(btnGuardar);
        panelBotones.add(btnEliminar);
        panelBotones.add(btnRefrescar);

        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 4;
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
            finanzasService.listarIngresos().stream()
                    .filter(i -> i.getIdIngreso() == idSeleccionado)
                    .findFirst()
                    .ifPresent(i -> {
                        seleccionarCategoria(i.getIdCategoriaIngreso());
                        txtDescripcion.setText(i.getDescripcion());
                        txtMonto.setText(i.getMonto().toPlainString());
                        txtFecha.setText(i.getFecha().toString());
                    });
        } catch (SQLException e) {
            Mensajes.error(this, "No se pudo cargar el ingreso:\n" + e.getMessage());
        }
    }

    private void seleccionarCategoria(int idCategoria) {
        for (int i = 0; i < cmbCategoria.getItemCount(); i++) {
            if (cmbCategoria.getItemAt(i).getIdCategoriaIngreso() == idCategoria) {
                cmbCategoria.setSelectedIndex(i);
                return;
            }
        }
    }

    private void limpiarFormulario() {
        idSeleccionado = null;
        tabla.clearSelection();
        txtDescripcion.setText("");
        txtMonto.setText("");
        txtFecha.setText(LocalDate.now().toString());
    }

    private void guardar() {
        CategoriaIngreso categoria = (CategoriaIngreso) cmbCategoria.getSelectedItem();
        if (categoria == null) {
            Mensajes.error(this, "Seleccione una categoria.");
            return;
        }
        try {
            Ingreso ingreso = new Ingreso();
            if (idSeleccionado != null) {
                ingreso.setIdIngreso(idSeleccionado);
            }
            ingreso.setIdCategoriaIngreso(categoria.getIdCategoriaIngreso());
            ingreso.setDescripcion(txtDescripcion.getText().trim());
            ingreso.setMonto(Validaciones.aBigDecimal(txtMonto.getText()));
            ingreso.setFecha(LocalDate.parse(txtFecha.getText().trim()));

            if (!Validaciones.esNumeroPositivo(ingreso.getMonto())) {
                Mensajes.error(this, "El monto debe ser un numero positivo.");
                return;
            }

            finanzasService.guardarIngreso(ingreso);
            limpiarFormulario();
            refrescar();
        } catch (DateTimeParseException e) {
            Mensajes.error(this, "La fecha debe tener el formato yyyy-mm-dd.");
        } catch (NumberFormatException e) {
            Mensajes.error(this, "El monto debe ser un valor numerico.");
        } catch (SQLException e) {
            Mensajes.error(this, "No se pudo guardar el ingreso:\n" + e.getMessage());
        }
    }

    private void eliminar() {
        if (idSeleccionado == null) {
            Mensajes.error(this, "Seleccione un ingreso de la tabla.");
            return;
        }
        if (!Mensajes.confirmar(this, "¿Desea eliminar el ingreso seleccionado?")) {
            return;
        }
        try {
            finanzasService.eliminarIngreso(idSeleccionado);
            limpiarFormulario();
            refrescar();
        } catch (SQLException e) {
            Mensajes.error(this, "No se pudo eliminar el ingreso:\n" + e.getMessage());
        }
    }

    @Override
    public void refrescar() {
        try {
            cmbCategoria.removeAllItems();
            for (CategoriaIngreso c : finanzasService.listarCategoriasIngreso()) {
                cmbCategoria.addItem(c);
            }

            modeloTabla.setRowCount(0);
            List<Ingreso> ingresos = finanzasService.listarIngresos();
            for (Ingreso i : ingresos) {
                modeloTabla.addRow(new Object[]{i.getIdIngreso(), i.getNombreCategoria(), i.getDescripcion(),
                        FormatoMoneda.formatear(i.getMonto()), i.getFecha()});
            }
            lblTotal.setText(FormatoMoneda.formatear(finanzasService.totalIngresos()));
        } catch (SQLException e) {
            Mensajes.error(this, "No se pudo cargar los ingresos:\n" + e.getMessage());
        }
    }
}
