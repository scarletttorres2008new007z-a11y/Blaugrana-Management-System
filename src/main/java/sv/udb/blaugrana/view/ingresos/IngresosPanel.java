package sv.udb.blaugrana.view.ingresos;

import sv.udb.blaugrana.model.CategoriaIngreso;
import sv.udb.blaugrana.model.Ingreso;
import sv.udb.blaugrana.service.FinanzasService;
import sv.udb.blaugrana.util.ColoresBlaugrana;
import sv.udb.blaugrana.util.FiltroTabla;
import sv.udb.blaugrana.util.FormatoMoneda;
import sv.udb.blaugrana.util.Medidas;
import sv.udb.blaugrana.util.Mensajes;
import sv.udb.blaugrana.util.PermisosUI;
import sv.udb.blaugrana.util.Tipografia;
import sv.udb.blaugrana.util.Validaciones;
import sv.udb.blaugrana.view.Refrescable;
import sv.udb.blaugrana.view.componentes.EncabezadoSeccion;
import sv.udb.blaugrana.view.componentes.TarjetaEstadistica;

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
    private final JTextField txtBuscar = new JTextField(20);

    private final JComboBox<CategoriaIngreso> cmbCategoria = new JComboBox<>();
    private final JTextField txtDescripcion = new JTextField(20);
    private final JTextField txtMonto = new JTextField(10);
    private final JTextField txtFecha = new JTextField(10);
    private final TarjetaEstadistica tarjetaTotal =
            new TarjetaEstadistica("Total de ingresos", "$0.00", ColoresBlaugrana.VERDE_ACTIVO, Tipografia.DATO_MEDIANO);

    private Integer idSeleccionado;

    public IngresosPanel() {
        setLayout(new BorderLayout());
        setBackground(ColoresBlaugrana.GRIS_CLARO);
        setBorder(new EmptyBorder(20, 20, 20, 20));

        add(construirEncabezado(), BorderLayout.NORTH);

        tabla.setRowHeight(24);
        tabla.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                cargarSeleccion();
            }
        });
        FiltroTabla.activarBusqueda(txtBuscar, tabla, modeloTabla);
        JScrollPane scroll = new JScrollPane(tabla);
        scroll.setBorder(BorderFactory.createLineBorder(ColoresBlaugrana.GRIS_MEDIO, 1));
        add(scroll, BorderLayout.CENTER);
        add(construirFormulario(), BorderLayout.SOUTH);
    }

    private JPanel construirEncabezado() {
        JPanel panel = new JPanel(new BorderLayout(Medidas.ESPACIO_ENTRE_TARJETAS, 0));
        panel.setOpaque(false);

        JPanel bloqueTitulo = new JPanel(new BorderLayout());
        bloqueTitulo.setOpaque(false);
        bloqueTitulo.add(new EncabezadoSeccion("Ingresos", "Fuentes de ingreso del club"), BorderLayout.NORTH);

        JPanel panelBusqueda = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        panelBusqueda.setOpaque(false);
        panelBusqueda.setBorder(new EmptyBorder(0, 0, Medidas.PADDING_SECCION, 0));
        JLabel lblBuscar = new JLabel("Buscar:");
        lblBuscar.setFont(Tipografia.CUERPO);
        lblBuscar.setForeground(ColoresBlaugrana.GRIS_TEXTO);
        panelBusqueda.add(lblBuscar);
        panelBusqueda.add(txtBuscar);
        bloqueTitulo.add(panelBusqueda, BorderLayout.SOUTH);

        tarjetaTotal.setPreferredSize(new Dimension(220, 70));
        JPanel envoltorioTarjeta = new JPanel(new BorderLayout());
        envoltorioTarjeta.setOpaque(false);
        envoltorioTarjeta.setBorder(new EmptyBorder(0, 0, Medidas.PADDING_SECCION, 0));
        envoltorioTarjeta.add(tarjetaTotal, BorderLayout.NORTH);

        panel.add(bloqueTitulo, BorderLayout.CENTER);
        panel.add(envoltorioTarjeta, BorderLayout.EAST);
        return panel;
    }

    private JPanel construirFormulario() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(ColoresBlaugrana.BLANCO);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(ColoresBlaugrana.GRIS_MEDIO, 1),
                BorderFactory.createTitledBorder("Registrar ingreso")));
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
        PermisosUI.deshabilitarSiSoloLectura(btnGuardar, btnEliminar);

        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        panelBotones.setOpaque(false);
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
        idSeleccionado = (Integer) modeloTabla.getValueAt(tabla.convertRowIndexToModel(fila), 0);
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
            Mensajes.error(this, "No se pudo cargar el ingreso", e);
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
            Mensajes.error(this, "No se pudo guardar el ingreso", e);
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
            Mensajes.error(this, "No se pudo eliminar el ingreso", e);
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
            tarjetaTotal.setValor(FormatoMoneda.formatear(finanzasService.totalIngresos()));
        } catch (SQLException e) {
            Mensajes.error(this, "No se pudo cargar los ingresos", e);
        }
    }
}
