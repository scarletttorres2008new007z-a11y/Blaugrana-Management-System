package sv.udb.blaugrana.view.pagos;

import sv.udb.blaugrana.model.Contrato;
import sv.udb.blaugrana.model.Jugador;
import sv.udb.blaugrana.model.Pago;
import sv.udb.blaugrana.service.ContratoService;
import sv.udb.blaugrana.service.JugadorService;
import sv.udb.blaugrana.service.PagoService;
import sv.udb.blaugrana.util.ColoresBlaugrana;
import sv.udb.blaugrana.util.FiltroTabla;
import sv.udb.blaugrana.util.FormatoMoneda;
import sv.udb.blaugrana.util.Mensajes;
import sv.udb.blaugrana.util.PermisosUI;
import sv.udb.blaugrana.util.Validaciones;
import sv.udb.blaugrana.view.Refrescable;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

public class PagosPanel extends JPanel implements Refrescable {

    private final PagoService pagoService = new PagoService();
    private final ContratoService contratoService = new ContratoService();
    private final JugadorService jugadorService = new JugadorService();

    private final DefaultTableModel modeloTabla = new DefaultTableModel(
            new Object[]{"ID", "Jugador", "Periodo", "Salario", "Bonif.", "Deduc.", "Total", "Estado"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final JTable tabla = new JTable(modeloTabla);
    private final JTextField txtBuscar = new JTextField(20);

    private final JComboBox<Jugador> cmbJugador = new JComboBox<>();
    private final JTextField txtPeriodo = new JTextField(8);
    private final JTextField txtDeducciones = new JTextField(8);

    public PagosPanel() {
        setLayout(new BorderLayout());
        setBorder(new EmptyBorder(15, 15, 15, 15));

        JLabel titulo = new JLabel("PAGOS - Planilla de jugadores");
        titulo.setFont(new Font("SansSerif", Font.BOLD, 18));
        titulo.setForeground(ColoresBlaugrana.AZUL_OSCURO);
        add(construirEncabezado(titulo), BorderLayout.NORTH);

        tabla.setRowHeight(24);
        FiltroTabla.activarBusqueda(txtBuscar, tabla, modeloTabla);
        add(new JScrollPane(tabla), BorderLayout.CENTER);
        add(construirFormulario(), BorderLayout.SOUTH);
    }

    private JPanel construirEncabezado(JLabel titulo) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(titulo, BorderLayout.NORTH);
        JPanel panelBusqueda = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panelBusqueda.add(new JLabel("Buscar:"));
        panelBusqueda.add(txtBuscar);
        panel.add(panelBusqueda, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel construirFormulario() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Generar pago"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 4, 4, 4);
        gbc.anchor = GridBagConstraints.WEST;

        campo(panel, gbc, 0, 0, "Jugador:", cmbJugador);
        campo(panel, gbc, 2, 0, "Periodo (yyyy-MM):", txtPeriodo);
        campo(panel, gbc, 4, 0, "Deducciones:", txtDeducciones);

        JButton btnGenerar = new JButton("Generar pago");
        JButton btnMarcarPagado = new JButton("Marcar como pagado");
        JButton btnEliminar = new JButton("Eliminar");
        JButton btnRefrescar = new JButton("Refrescar");

        btnGenerar.addActionListener(e -> generarPago());
        btnMarcarPagado.addActionListener(e -> marcarPagado());
        btnEliminar.addActionListener(e -> eliminar());
        btnRefrescar.addActionListener(e -> refrescar());
        PermisosUI.deshabilitarSiSoloLectura(btnGenerar, btnMarcarPagado, btnEliminar);

        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        panelBotones.add(btnGenerar);
        panelBotones.add(btnMarcarPagado);
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

    private void generarPago() {
        Jugador jugador = (Jugador) cmbJugador.getSelectedItem();
        if (jugador == null || Validaciones.esVacio(txtPeriodo.getText())) {
            Mensajes.error(this, "Seleccione un jugador e ingrese el periodo (yyyy-MM).");
            return;
        }
        try {
            List<Contrato> contratos = contratoService.listarPorJugador(jugador.getIdJugador());
            Optional<Contrato> vigente = contratos.stream().filter(c -> "VIGENTE".equals(c.getEstado())).findFirst();
            if (vigente.isEmpty()) {
                Mensajes.error(this, "El jugador no tiene un contrato vigente.");
                return;
            }
            BigDecimal deducciones = Validaciones.aBigDecimal(txtDeducciones.getText());
            pagoService.generarPago(jugador.getIdJugador(), vigente.get().getIdContrato(), txtPeriodo.getText().trim(), deducciones);
            Mensajes.info(this, "Pago generado correctamente.");
            refrescar();
        } catch (NumberFormatException e) {
            Mensajes.error(this, "Las deducciones deben ser un valor numerico.");
        } catch (IllegalArgumentException | IllegalStateException e) {
            Mensajes.error(this, e.getMessage());
        } catch (SQLException e) {
            Mensajes.error(this, "No se pudo generar el pago", e);
        }
    }

    private void marcarPagado() {
        int fila = tabla.getSelectedRow();
        if (fila < 0) {
            Mensajes.error(this, "Seleccione un pago de la tabla.");
            return;
        }
        Integer id = (Integer) modeloTabla.getValueAt(tabla.convertRowIndexToModel(fila), 0);
        try {
            pagoService.marcarComoPagado(id);
            refrescar();
        } catch (IllegalArgumentException | IllegalStateException e) {
            Mensajes.error(this, e.getMessage());
        } catch (SQLException e) {
            Mensajes.error(this, "No se pudo actualizar el pago", e);
        }
    }

    private void eliminar() {
        int fila = tabla.getSelectedRow();
        if (fila < 0) {
            Mensajes.error(this, "Seleccione un pago de la tabla.");
            return;
        }
        Integer id = (Integer) modeloTabla.getValueAt(tabla.convertRowIndexToModel(fila), 0);
        if (!Mensajes.confirmar(this, "¿Desea eliminar el pago seleccionado?")) {
            return;
        }
        try {
            pagoService.eliminar(id);
            refrescar();
        } catch (SQLException e) {
            Mensajes.error(this, "No se pudo eliminar el pago", e);
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
            if (Validaciones.esVacio(txtPeriodo.getText())) {
                txtPeriodo.setText(LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM")));
            }

            modeloTabla.setRowCount(0);
            List<Pago> pagos = pagoService.listar();
            for (Pago p : pagos) {
                modeloTabla.addRow(new Object[]{p.getIdPago(), p.getNombreJugador(), p.getPeriodo(),
                        FormatoMoneda.formatear(p.getSalarioBase()), FormatoMoneda.formatear(p.getBonificaciones()),
                        FormatoMoneda.formatear(p.getDeducciones()), FormatoMoneda.formatear(p.getTotal()), p.getEstado()});
            }
        } catch (SQLException e) {
            Mensajes.error(this, "No se pudo cargar los pagos", e);
        }
    }
}
