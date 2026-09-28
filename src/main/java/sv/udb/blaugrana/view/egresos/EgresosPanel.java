package sv.udb.blaugrana.view.egresos;

import sv.udb.blaugrana.model.CategoriaEgreso;
import sv.udb.blaugrana.model.Egreso;
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

public class EgresosPanel extends JPanel implements Refrescable {

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

    private final JComboBox<CategoriaEgreso> cmbCategoria = new JComboBox<>();
    private final JTextField txtDescripcion = new JTextField(20);
    private final JTextField txtMonto = new JTextField(10);
    private final JTextField txtFecha = new JTextField(10);
    private final TarjetaEstadistica tarjetaTotal =
            new TarjetaEstadistica("Total de egresos", "$0.00", ColoresBlaugrana.ROJO_ALERTA, Tipografia.DATO_MEDIANO);

    private Integer idSeleccionado;

    public EgresosPanel() {
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
        bloqueTitulo.add(new EncabezadoSeccion("Egresos", "Gastos operativos del club"), BorderLayout.NORTH);

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
                BorderFactory.createTitledBorder("Registrar egreso")));
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
            finanzasService.listarEgresos().stream()
                    .filter(e -> e.getIdEgreso() == idSeleccionado)
                    .findFirst()
                    .ifPresent(e -> {
                        seleccionarCategoria(e.getIdCategoriaEgreso());
                        txtDescripcion.setText(e.getDescripcion());
                        txtMonto.setText(e.getMonto().toPlainString());
                        txtFecha.setText(e.getFecha().toString());
                    });
        } catch (SQLException e) {
            Mensajes.error(this, "No se pudo cargar el egreso", e);
        }
    }

    private void seleccionarCategoria(int idCategoria) {
        for (int i = 0; i < cmbCategoria.getItemCount(); i++) {
            if (cmbCategoria.getItemAt(i).getIdCategoriaEgreso() == idCategoria) {
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
        CategoriaEgreso categoria = (CategoriaEgreso) cmbCategoria.getSelectedItem();
        if (categoria == null) {
            Mensajes.error(this, "Seleccione una categoria.");
            return;
        }
        try {
            Egreso egreso = new Egreso();
            if (idSeleccionado != null) {
                egreso.setIdEgreso(idSeleccionado);
            }
            egreso.setIdCategoriaEgreso(categoria.getIdCategoriaEgreso());
            egreso.setDescripcion(txtDescripcion.getText().trim());
            egreso.setMonto(Validaciones.aBigDecimal(txtMonto.getText()));
            egreso.setFecha(LocalDate.parse(txtFecha.getText().trim()));

            if (!Validaciones.esNumeroPositivo(egreso.getMonto())) {
                Mensajes.error(this, "El monto debe ser un numero positivo.");
                return;
            }

            finanzasService.guardarEgreso(egreso);
            limpiarFormulario();
            refrescar();
        } catch (DateTimeParseException e) {
            Mensajes.error(this, "La fecha debe tener el formato yyyy-mm-dd.");
        } catch (NumberFormatException e) {
            Mensajes.error(this, "El monto debe ser un valor numerico.");
        } catch (SQLException e) {
            Mensajes.error(this, "No se pudo guardar el egreso", e);
        }
    }

    private void eliminar() {
        if (idSeleccionado == null) {
            Mensajes.error(this, "Seleccione un egreso de la tabla.");
            return;
        }
        if (!Mensajes.confirmar(this, "¿Desea eliminar el egreso seleccionado?")) {
            return;
        }
        try {
            finanzasService.eliminarEgreso(idSeleccionado);
            limpiarFormulario();
            refrescar();
        } catch (SQLException e) {
            Mensajes.error(this, "No se pudo eliminar el egreso", e);
        }
    }

    @Override
    public void refrescar() {
        try {
            cmbCategoria.removeAllItems();
            for (CategoriaEgreso c : finanzasService.listarCategoriasEgreso()) {
                cmbCategoria.addItem(c);
            }

            modeloTabla.setRowCount(0);
            List<Egreso> egresos = finanzasService.listarEgresos();
            for (Egreso e : egresos) {
                modeloTabla.addRow(new Object[]{e.getIdEgreso(), e.getNombreCategoria(), e.getDescripcion(),
                        FormatoMoneda.formatear(e.getMonto()), e.getFecha()});
            }
            tarjetaTotal.setValor(FormatoMoneda.formatear(finanzasService.totalEgresos()));
        } catch (SQLException e) {
            Mensajes.error(this, "No se pudo cargar los egresos", e);
        }
    }
}
