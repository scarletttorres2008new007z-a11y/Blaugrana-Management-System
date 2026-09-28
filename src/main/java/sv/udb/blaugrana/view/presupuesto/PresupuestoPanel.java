package sv.udb.blaugrana.view.presupuesto;

import sv.udb.blaugrana.model.DetallePresupuesto;
import sv.udb.blaugrana.model.Presupuesto;
import sv.udb.blaugrana.service.PresupuestoService;
import sv.udb.blaugrana.util.ColoresBlaugrana;
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
import java.math.BigDecimal;
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

    private final TarjetaEstadistica tarjetaPresupuestado =
            new TarjetaEstadistica("Presupuestado", "$0.00", ColoresBlaugrana.AZUL_OSCURO, Tipografia.DATO_MEDIANO);
    private final TarjetaEstadistica tarjetaEjecutado =
            new TarjetaEstadistica("Ejecutado", "$0.00", ColoresBlaugrana.GRANATE, Tipografia.DATO_MEDIANO);
    private final TarjetaEstadistica tarjetaDisponible =
            new TarjetaEstadistica("Disponible", "$0.00", ColoresBlaugrana.VERDE_ACTIVO, Tipografia.DATO_MEDIANO);

    public PresupuestoPanel() {
        setLayout(new BorderLayout());
        setBackground(ColoresBlaugrana.GRIS_CLARO);
        setBorder(new EmptyBorder(20, 20, 20, 20));

        add(construirEncabezado(), BorderLayout.NORTH);

        tabla.setRowHeight(24);

        JPanel centro = new JPanel(new BorderLayout(0, Medidas.ESPACIO_ENTRE_TARJETAS));
        centro.setOpaque(false);
        centro.add(construirFilaResumen(), BorderLayout.NORTH);
        JPanel contenedorTabla = new JPanel(new BorderLayout());
        contenedorTabla.setBackground(ColoresBlaugrana.BLANCO);
        contenedorTabla.setBorder(BorderFactory.createLineBorder(ColoresBlaugrana.GRIS_MEDIO, 1));
        contenedorTabla.add(new JScrollPane(tabla), BorderLayout.CENTER);
        centro.add(contenedorTabla, BorderLayout.CENTER);

        add(centro, BorderLayout.CENTER);
        add(construirFormulario(), BorderLayout.SOUTH);

        cmbPresupuesto.addActionListener(e -> cargarDetalle());
    }

    private JPanel construirEncabezado() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);
        panel.add(new EncabezadoSeccion("Presupuesto", "Planificación financiera por temporada"), BorderLayout.NORTH);

        JPanel selector = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        selector.setOpaque(false);
        selector.setBorder(new EmptyBorder(0, 0, Medidas.PADDING_SECCION, 0));
        JLabel lblPresupuesto = new JLabel("Presupuesto:");
        lblPresupuesto.setFont(Tipografia.CUERPO);
        lblPresupuesto.setForeground(ColoresBlaugrana.GRIS_TEXTO);
        selector.add(lblPresupuesto);
        selector.add(cmbPresupuesto);
        panel.add(selector, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel construirFilaResumen() {
        JPanel fila = new JPanel(new GridLayout(1, 3, Medidas.ESPACIO_ENTRE_TARJETAS, Medidas.ESPACIO_ENTRE_TARJETAS));
        fila.setOpaque(false);
        fila.setMaximumSize(new Dimension(Integer.MAX_VALUE, 100));
        fila.add(tarjetaPresupuestado);
        fila.add(tarjetaEjecutado);
        fila.add(tarjetaDisponible);
        return fila;
    }

    private JPanel construirFormulario() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(ColoresBlaugrana.BLANCO);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(ColoresBlaugrana.GRIS_MEDIO, 1),
                BorderFactory.createTitledBorder("Nuevo presupuesto / detalle")));
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
        panelBotones.setOpaque(false);
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
            actualizarResumen(List.of());
            return;
        }
        try {
            List<DetallePresupuesto> detalles = presupuestoService.listarDetalle(presupuesto.getIdPresupuesto());
            for (DetallePresupuesto d : detalles) {
                modeloTabla.addRow(new Object[]{d.getIdDetalle(), d.getCategoria(),
                        FormatoMoneda.formatear(d.getMontoPresupuestado()), FormatoMoneda.formatear(d.getMontoEjecutado()),
                        FormatoMoneda.formatear(d.getDisponible())});
            }
            actualizarResumen(detalles);
        } catch (SQLException e) {
            Mensajes.error(this, "No se pudo cargar el detalle del presupuesto", e);
        }
    }

    private void actualizarResumen(List<DetallePresupuesto> detalles) {
        BigDecimal presupuestado = BigDecimal.ZERO;
        BigDecimal ejecutado = BigDecimal.ZERO;
        BigDecimal disponible = BigDecimal.ZERO;
        for (DetallePresupuesto d : detalles) {
            presupuestado = presupuestado.add(d.getMontoPresupuestado());
            ejecutado = ejecutado.add(d.getMontoEjecutado());
            disponible = disponible.add(d.getDisponible());
        }
        tarjetaPresupuestado.setValor(FormatoMoneda.formatear(presupuestado));
        tarjetaEjecutado.setValor(FormatoMoneda.formatear(ejecutado));
        tarjetaDisponible.setValor(FormatoMoneda.formatear(disponible));
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
