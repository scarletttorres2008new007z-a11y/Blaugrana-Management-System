package sv.udb.blaugrana.view.bonificaciones;

import sv.udb.blaugrana.model.Bonificacion;
import sv.udb.blaugrana.model.Jugador;
import sv.udb.blaugrana.service.BonificacionService;
import sv.udb.blaugrana.service.JugadorService;
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

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

public class BonificacionesPanel extends JPanel implements Refrescable {

    private final BonificacionService bonificacionService = new BonificacionService();
    private final JugadorService jugadorService = new JugadorService();

    private final DefaultTableModel modeloTabla = new DefaultTableModel(
            new Object[]{"ID", "Jugador", "Concepto", "Valor", "Fecha"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final JTable tabla = new JTable(modeloTabla);
    private final JTextField txtBuscar = new JTextField(20);

    private final JComboBox<Jugador> cmbJugador = new JComboBox<>();
    private final JTextField txtConcepto = new JTextField(20);
    private final JTextField txtValor = new JTextField(10);
    private final JLabel lblTotalJugador = new JLabel("$0.00");

    public BonificacionesPanel() {
        setLayout(new BorderLayout());
        setBackground(ColoresBlaugrana.GRIS_CLARO);
        setBorder(new EmptyBorder(20, 20, 20, 20));

        add(construirEncabezado(), BorderLayout.NORTH);

        tabla.setRowHeight(24);
        FiltroTabla.activarBusqueda(txtBuscar, tabla, modeloTabla);
        JScrollPane scroll = new JScrollPane(tabla);
        scroll.setBorder(BorderFactory.createLineBorder(ColoresBlaugrana.GRIS_MEDIO, 1));
        add(scroll, BorderLayout.CENTER);
        add(construirFormulario(), BorderLayout.SOUTH);
    }

    private JPanel construirEncabezado() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);
        panel.add(new EncabezadoSeccion("Bonificaciones", "Incentivos económicos por rendimiento"), BorderLayout.NORTH);

        JPanel panelBusqueda = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        panelBusqueda.setOpaque(false);
        panelBusqueda.setBorder(new EmptyBorder(0, 0, Medidas.PADDING_SECCION, 0));
        JLabel lblBuscar = new JLabel("Buscar:");
        lblBuscar.setFont(Tipografia.CUERPO);
        lblBuscar.setForeground(ColoresBlaugrana.GRIS_TEXTO);
        panelBusqueda.add(lblBuscar);
        panelBusqueda.add(txtBuscar);
        panel.add(panelBusqueda, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel construirFormulario() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(ColoresBlaugrana.BLANCO);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(ColoresBlaugrana.GRIS_MEDIO, 1),
                BorderFactory.createTitledBorder("Registrar bonificacion manual")));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 4, 4, 4);
        gbc.anchor = GridBagConstraints.WEST;

        campo(panel, gbc, 0, 0, "Jugador:", cmbJugador);
        campo(panel, gbc, 2, 0, "Concepto:", txtConcepto);
        campo(panel, gbc, 4, 0, "Valor:", txtValor);

        cmbJugador.addActionListener(e -> actualizarTotalJugador());

        JButton btnAgregar = new JButton("Agregar bonificacion");
        JButton btnEliminar = new JButton("Eliminar seleccionada");
        JButton btnRefrescar = new JButton("Refrescar");

        btnAgregar.addActionListener(e -> agregar());
        btnEliminar.addActionListener(e -> eliminar());
        btnRefrescar.addActionListener(e -> refrescar());
        PermisosUI.deshabilitarSiSoloLectura(btnAgregar, btnEliminar);

        lblTotalJugador.setFont(Tipografia.CUERPO_NEGRITA);
        lblTotalJugador.setForeground(ColoresBlaugrana.GRANATE);

        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        panelBotones.setOpaque(false);
        JLabel lblTotal = new JLabel("Total del jugador seleccionado: ");
        lblTotal.setFont(Tipografia.CUERPO);
        panelBotones.add(lblTotal);
        panelBotones.add(lblTotalJugador);
        panelBotones.add(btnAgregar);
        panelBotones.add(btnEliminar);
        panelBotones.add(btnRefrescar);

        gbc.gridx = 0;
        gbc.gridy = 1;
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

    private void agregar() {
        Jugador jugador = (Jugador) cmbJugador.getSelectedItem();
        if (jugador == null || Validaciones.esVacio(txtConcepto.getText())) {
            Mensajes.error(this, "Seleccione un jugador e ingrese un concepto.");
            return;
        }
        try {
            Bonificacion bonificacion = new Bonificacion();
            bonificacion.setIdJugador(jugador.getIdJugador());
            bonificacion.setConcepto(txtConcepto.getText().trim());
            bonificacion.setValor(Validaciones.aBigDecimal(txtValor.getText()));
            bonificacion.setFechaGeneracion(LocalDate.now());

            if (!Validaciones.esNumeroPositivo(bonificacion.getValor())) {
                Mensajes.error(this, "El valor debe ser un numero positivo.");
                return;
            }

            bonificacionService.registrarManual(bonificacion);
            txtConcepto.setText("");
            txtValor.setText("");
            refrescar();
        } catch (NumberFormatException e) {
            Mensajes.error(this, "El valor debe ser numerico.");
        } catch (SQLException e) {
            Mensajes.error(this, "No se pudo registrar la bonificacion", e);
        }
    }

    private void eliminar() {
        int fila = tabla.getSelectedRow();
        if (fila < 0) {
            Mensajes.error(this, "Seleccione una bonificacion de la tabla.");
            return;
        }
        Integer id = (Integer) modeloTabla.getValueAt(tabla.convertRowIndexToModel(fila), 0);
        if (!Mensajes.confirmar(this, "¿Desea eliminar la bonificacion seleccionada?")) {
            return;
        }
        try {
            bonificacionService.eliminar(id);
            refrescar();
        } catch (SQLException e) {
            Mensajes.error(this, "No se pudo eliminar la bonificacion", e);
        }
    }

    private void actualizarTotalJugador() {
        Jugador jugador = (Jugador) cmbJugador.getSelectedItem();
        if (jugador == null) {
            lblTotalJugador.setText("$0.00");
            return;
        }
        try {
            lblTotalJugador.setText(FormatoMoneda.formatear(bonificacionService.totalPorJugador(jugador.getIdJugador())));
        } catch (SQLException e) {
            lblTotalJugador.setText("$0.00");
        }
    }

    @Override
    public void refrescar() {
        try {
            Jugador seleccionActual = (Jugador) cmbJugador.getSelectedItem();
            cmbJugador.removeAllItems();
            for (Jugador j : jugadorService.listarActivos()) {
                cmbJugador.addItem(j);
            }
            if (seleccionActual != null) {
                for (int i = 0; i < cmbJugador.getItemCount(); i++) {
                    if (cmbJugador.getItemAt(i).getIdJugador() == seleccionActual.getIdJugador()) {
                        cmbJugador.setSelectedIndex(i);
                    }
                }
            }

            modeloTabla.setRowCount(0);
            List<Bonificacion> bonificaciones = bonificacionService.listar();
            for (Bonificacion b : bonificaciones) {
                modeloTabla.addRow(new Object[]{b.getIdBonificacion(), b.getNombreJugador(), b.getConcepto(),
                        FormatoMoneda.formatear(b.getValor()), b.getFechaGeneracion()});
            }
            actualizarTotalJugador();
        } catch (SQLException e) {
            Mensajes.error(this, "No se pudo cargar las bonificaciones", e);
        }
    }
}
