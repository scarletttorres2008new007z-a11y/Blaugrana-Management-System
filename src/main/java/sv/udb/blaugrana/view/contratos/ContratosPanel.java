package sv.udb.blaugrana.view.contratos;

import sv.udb.blaugrana.model.Contrato;
import sv.udb.blaugrana.model.Jugador;
import sv.udb.blaugrana.service.ContratoService;
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
import sv.udb.blaugrana.view.componentes.RenderizadorInsignia;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;

public class ContratosPanel extends JPanel implements Refrescable {

    private static final int COLUMNA_ESTADO = 5;

    private final ContratoService contratoService = new ContratoService();
    private final JugadorService jugadorService = new JugadorService();

    private final DefaultTableModel modeloTabla = new DefaultTableModel(
            new Object[]{"ID", "Jugador", "Inicio", "Fin", "Salario base", "Estado"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final JTable tabla = new JTable(modeloTabla);
    private final JTextField txtBuscar = new JTextField(20);
    private final JComboBox<Jugador> cmbJugador = new JComboBox<>();
    private final JTextField txtFechaInicio = new JTextField(10);
    private final JTextField txtFechaFin = new JTextField(10);
    private final JTextField txtSalario = new JTextField(10);
    private final JTextField txtCondiciones = new JTextField(30);
    private final JComboBox<String> cmbEstado = new JComboBox<>(new String[]{"VIGENTE", "FINALIZADO", "RESCINDIDO"});

    private Integer idSeleccionado;

    public ContratosPanel() {
        setLayout(new BorderLayout());
        setBackground(ColoresBlaugrana.GRIS_CLARO);
        setBorder(new EmptyBorder(20, 20, 20, 20));

        add(construirEncabezado(), BorderLayout.NORTH);

        tabla.setRowHeight(28);
        tabla.getColumnModel().getColumn(COLUMNA_ESTADO).setCellRenderer(new RenderizadorInsignia());
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
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);
        panel.add(new EncabezadoSeccion("Contratos", "Gestión de contratos de jugadores"), BorderLayout.NORTH);

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
                BorderFactory.createTitledBorder("Datos del contrato")));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 4, 4, 4);
        gbc.anchor = GridBagConstraints.WEST;

        campo(panel, gbc, 0, 0, "Jugador:", cmbJugador);
        campo(panel, gbc, 2, 0, "Fecha inicio (yyyy-mm-dd):", txtFechaInicio);
        campo(panel, gbc, 4, 0, "Fecha fin (yyyy-mm-dd):", txtFechaFin);

        campo(panel, gbc, 0, 1, "Salario base anual:", txtSalario);
        campo(panel, gbc, 2, 1, "Estado:", cmbEstado);
        campo(panel, gbc, 0, 2, "Condiciones:", txtCondiciones);

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
        gbc.gridy = 3;
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
        idSeleccionado = (Integer) modeloTabla.getValueAt(tabla.convertRowIndexToModel(fila), 0);
        try {
            contratoService.buscarPorId(idSeleccionado).ifPresent(c -> {
                seleccionarJugadorEnCombo(c.getIdJugador());
                txtFechaInicio.setText(c.getFechaInicio().toString());
                txtFechaFin.setText(c.getFechaFin().toString());
                txtSalario.setText(c.getSalarioBase().toPlainString());
                txtCondiciones.setText(c.getCondiciones());
                cmbEstado.setSelectedItem(c.getEstado());
            });
        } catch (SQLException e) {
            Mensajes.error(this, "No se pudo cargar el contrato", e);
        }
    }

    private void seleccionarJugadorEnCombo(int idJugador) {
        for (int i = 0; i < cmbJugador.getItemCount(); i++) {
            Jugador j = cmbJugador.getItemAt(i);
            if (j.getIdJugador() == idJugador) {
                cmbJugador.setSelectedIndex(i);
                return;
            }
        }
    }

    private void limpiarFormulario() {
        idSeleccionado = null;
        tabla.clearSelection();
        txtFechaInicio.setText(LocalDate.now().toString());
        txtFechaFin.setText(LocalDate.now().plusYears(3).toString());
        txtSalario.setText("");
        txtCondiciones.setText("");
        cmbEstado.setSelectedIndex(0);
    }

    private void guardar() {
        Jugador jugador = (Jugador) cmbJugador.getSelectedItem();
        if (jugador == null) {
            Mensajes.error(this, "Debe seleccionar un jugador.");
            return;
        }
        try {
            Contrato contrato = new Contrato();
            if (idSeleccionado != null) {
                contrato.setIdContrato(idSeleccionado);
            }
            contrato.setIdJugador(jugador.getIdJugador());
            contrato.setFechaInicio(LocalDate.parse(txtFechaInicio.getText().trim()));
            contrato.setFechaFin(LocalDate.parse(txtFechaFin.getText().trim()));
            contrato.setSalarioBase(Validaciones.aBigDecimal(txtSalario.getText()));
            contrato.setCondiciones(txtCondiciones.getText().trim());
            contrato.setEstado((String) cmbEstado.getSelectedItem());

            if (!Validaciones.esNumeroPositivo(contrato.getSalarioBase())) {
                Mensajes.error(this, "El salario base debe ser un numero positivo.");
                return;
            }

            contratoService.guardar(contrato);
            Mensajes.info(this, "Contrato guardado correctamente.");
            limpiarFormulario();
            refrescar();
        } catch (DateTimeParseException e) {
            Mensajes.error(this, "Las fechas deben tener el formato yyyy-mm-dd.");
        } catch (NumberFormatException e) {
            Mensajes.error(this, "El salario base debe ser un valor numerico valido.");
        } catch (IllegalArgumentException | IllegalStateException e) {
            Mensajes.error(this, e.getMessage());
        } catch (SQLException e) {
            Mensajes.error(this, "No se pudo guardar el contrato", e);
        }
    }

    private void eliminar() {
        if (idSeleccionado == null) {
            Mensajes.error(this, "Seleccione un contrato de la tabla.");
            return;
        }
        if (!Mensajes.confirmar(this, "¿Desea eliminar el contrato seleccionado?")) {
            return;
        }
        try {
            contratoService.eliminar(idSeleccionado);
            limpiarFormulario();
            refrescar();
        } catch (SQLException e) {
            Mensajes.error(this, "No se pudo eliminar el contrato", e);
        }
    }

    @Override
    public void refrescar() {
        try {
            cmbJugador.removeAllItems();
            List<Jugador> jugadores = jugadorService.listarActivos();
            for (Jugador j : jugadores) {
                cmbJugador.addItem(j);
            }

            modeloTabla.setRowCount(0);
            List<Contrato> contratos = contratoService.listar();
            for (Contrato c : contratos) {
                modeloTabla.addRow(new Object[]{c.getIdContrato(), c.getNombreJugador(), c.getFechaInicio(),
                        c.getFechaFin(), FormatoMoneda.formatear(c.getSalarioBase()), c.getEstado()});
            }
        } catch (SQLException e) {
            Mensajes.error(this, "No se pudo cargar los contratos", e);
        }
    }
}
