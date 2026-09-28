package sv.udb.blaugrana.view.partidos;

import sv.udb.blaugrana.model.Partido;
import sv.udb.blaugrana.service.BonificacionService;
import sv.udb.blaugrana.service.PartidoService;
import sv.udb.blaugrana.util.ColoresBlaugrana;
import sv.udb.blaugrana.util.Medidas;
import sv.udb.blaugrana.util.Mensajes;
import sv.udb.blaugrana.util.PermisosUI;
import sv.udb.blaugrana.util.Tipografia;
import sv.udb.blaugrana.util.Validaciones;
import sv.udb.blaugrana.view.Refrescable;
import sv.udb.blaugrana.view.componentes.EncabezadoSeccion;
import sv.udb.blaugrana.view.componentes.Insignia;
import sv.udb.blaugrana.view.componentes.TarjetaPartido;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.SQLException;
import java.text.Normalizer;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Calendario y resultados presentados como una lista de "match cards"
 * (escudos, marcador o VS, competicion/fecha/resultado) en vez de una tabla
 * plana, con seleccion por clic para editar en el formulario inferior.
 */
public class PartidosPanel extends JPanel implements Refrescable {

    private final PartidoService partidoService = new PartidoService();
    private final BonificacionService bonificacionService = new BonificacionService();

    private final JPanel panelLista = new JPanel();
    private final Map<Integer, TarjetaFilaPartido> tarjetasPorId = new LinkedHashMap<>();
    private List<Partido> partidosCargados = new ArrayList<>();

    private final JTextField txtBuscar = new JTextField(20);

    private final JTextField txtCompeticion = new JTextField(16);
    private final JTextField txtFecha = new JTextField(10);
    private final JTextField txtRival = new JTextField(16);
    private final JComboBox<String> cmbCondicion = new JComboBox<>(new String[]{"LOCAL", "VISITANTE"});
    private final JTextField txtGolesFavor = new JTextField(4);
    private final JTextField txtGolesContra = new JTextField(4);
    private final JComboBox<String> cmbEstado = new JComboBox<>(new String[]{"PROGRAMADO", "FINALIZADO"});

    private Integer idSeleccionado;

    public PartidosPanel() {
        setLayout(new BorderLayout());
        setBackground(ColoresBlaugrana.GRIS_CLARO);
        setBorder(new EmptyBorder(20, 20, 20, 20));

        add(construirEncabezado(), BorderLayout.NORTH);

        panelLista.setLayout(new BoxLayout(panelLista, BoxLayout.Y_AXIS));
        panelLista.setOpaque(false);

        JScrollPane scroll = new JScrollPane(panelLista);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        add(scroll, BorderLayout.CENTER);

        add(construirFormulario(), BorderLayout.SOUTH);

        txtBuscar.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                renderizarLista();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                renderizarLista();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                renderizarLista();
            }
        });
    }

    private JPanel construirEncabezado() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);
        panel.add(new EncabezadoSeccion("Partidos", "Calendario y resultados de FC Barcelona"), BorderLayout.NORTH);

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
                BorderFactory.createTitledBorder("Datos del partido")));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 4, 4, 4);
        gbc.anchor = GridBagConstraints.WEST;

        campo(panel, gbc, 0, 0, "Competicion:", txtCompeticion);
        campo(panel, gbc, 2, 0, "Fecha (yyyy-mm-dd):", txtFecha);
        campo(panel, gbc, 4, 0, "Rival:", txtRival);

        campo(panel, gbc, 0, 1, "Condicion:", cmbCondicion);
        campo(panel, gbc, 2, 1, "Goles a favor:", txtGolesFavor);
        campo(panel, gbc, 4, 1, "Goles en contra:", txtGolesContra);

        campo(panel, gbc, 0, 2, "Estado:", cmbEstado);

        JButton btnNuevo = new JButton("Nuevo");
        JButton btnGuardar = new JButton("Guardar");
        JButton btnEliminar = new JButton("Eliminar");
        JButton btnGenerarBonificaciones = new JButton("Generar bonificaciones");
        JButton btnRefrescar = new JButton("Refrescar");

        btnNuevo.addActionListener(e -> limpiarFormulario());
        btnGuardar.addActionListener(e -> guardar());
        btnEliminar.addActionListener(e -> eliminar());
        btnGenerarBonificaciones.addActionListener(e -> generarBonificaciones());
        btnRefrescar.addActionListener(e -> refrescar());
        PermisosUI.deshabilitarSiSoloLectura(btnGuardar, btnEliminar, btnGenerarBonificaciones);

        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        panelBotones.setOpaque(false);
        panelBotones.add(btnNuevo);
        panelBotones.add(btnGuardar);
        panelBotones.add(btnEliminar);
        panelBotones.add(btnGenerarBonificaciones);
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

    private void renderizarLista() {
        String filtro = normalizar(txtBuscar.getText());
        panelLista.removeAll();
        tarjetasPorId.clear();

        for (Partido partido : partidosCargados) {
            if (!filtro.isBlank() && !coincide(partido, filtro)) {
                continue;
            }
            TarjetaFilaPartido tarjeta = new TarjetaFilaPartido(partido);
            tarjeta.setSeleccionada(idSeleccionado != null && idSeleccionado == partido.getIdPartido());
            tarjeta.setAlignmentX(Component.LEFT_ALIGNMENT);
            tarjetasPorId.put(partido.getIdPartido(), tarjeta);
            panelLista.add(tarjeta);
            panelLista.add(Box.createVerticalStrut(Medidas.ESPACIO_ENTRE_TARJETAS));
        }

        if (tarjetasPorId.isEmpty()) {
            JLabel lblVacio = new JLabel("No se encontraron partidos.");
            lblVacio.setFont(Tipografia.CUERPO);
            lblVacio.setForeground(ColoresBlaugrana.GRIS_TEXTO_SUAVE);
            lblVacio.setAlignmentX(Component.LEFT_ALIGNMENT);
            panelLista.add(lblVacio);
        }

        panelLista.revalidate();
        panelLista.repaint();
    }

    private boolean coincide(Partido partido, String filtroNormalizado) {
        String texto = normalizar(String.join(" ",
                partido.getCompeticion(),
                String.valueOf(partido.getFecha()),
                partido.getRival(),
                String.valueOf(partido.getCondicion()),
                String.valueOf(partido.getResultado()),
                String.valueOf(partido.getEstado())));
        return texto.contains(filtroNormalizado);
    }

    private String normalizar(String texto) {
        if (texto == null) {
            return "";
        }
        String sinAcentos = Normalizer.normalize(texto, Normalizer.Form.NFD)
                .replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
        return sinAcentos.toLowerCase().trim();
    }

    private void seleccionarPartido(int idPartido) {
        idSeleccionado = idPartido;
        for (Map.Entry<Integer, TarjetaFilaPartido> entrada : tarjetasPorId.entrySet()) {
            entrada.getValue().setSeleccionada(entrada.getKey() == idPartido);
        }
        try {
            partidoService.buscarPorId(idPartido).ifPresent(p -> {
                txtCompeticion.setText(p.getCompeticion());
                txtFecha.setText(p.getFecha().toString());
                txtRival.setText(p.getRival());
                cmbCondicion.setSelectedItem(p.getCondicion());
                txtGolesFavor.setText(String.valueOf(p.getGolesFavor()));
                txtGolesContra.setText(String.valueOf(p.getGolesContra()));
                cmbEstado.setSelectedItem(p.getEstado());
            });
        } catch (SQLException e) {
            Mensajes.error(this, "No se pudo cargar el partido", e);
        }
    }

    private void limpiarFormulario() {
        idSeleccionado = null;
        for (TarjetaFilaPartido tarjeta : tarjetasPorId.values()) {
            tarjeta.setSeleccionada(false);
        }
        txtCompeticion.setText("");
        txtFecha.setText(LocalDate.now().toString());
        txtRival.setText("");
        cmbCondicion.setSelectedIndex(0);
        txtGolesFavor.setText("0");
        txtGolesContra.setText("0");
        cmbEstado.setSelectedIndex(0);
    }

    private void guardar() {
        if (Validaciones.esVacio(txtCompeticion.getText()) || Validaciones.esVacio(txtRival.getText())) {
            Mensajes.error(this, "La competicion y el rival son obligatorios.");
            return;
        }
        try {
            Partido partido = new Partido();
            if (idSeleccionado != null) {
                partido.setIdPartido(idSeleccionado);
            }
            partido.setCompeticion(txtCompeticion.getText().trim());
            partido.setFecha(LocalDate.parse(txtFecha.getText().trim()));
            partido.setRival(txtRival.getText().trim());
            partido.setCondicion((String) cmbCondicion.getSelectedItem());
            partido.setGolesFavor(Integer.parseInt(txtGolesFavor.getText().trim()));
            partido.setGolesContra(Integer.parseInt(txtGolesContra.getText().trim()));
            partido.setEstado((String) cmbEstado.getSelectedItem());

            partidoService.guardar(partido);
            Mensajes.info(this, "Partido guardado correctamente.");
            limpiarFormulario();
            refrescar();
        } catch (DateTimeParseException e) {
            Mensajes.error(this, "La fecha debe tener el formato yyyy-mm-dd.");
        } catch (NumberFormatException e) {
            Mensajes.error(this, "Los goles deben ser valores numericos.");
        } catch (SQLException e) {
            Mensajes.error(this, "No se pudo guardar el partido", e);
        }
    }

    private void eliminar() {
        if (idSeleccionado == null) {
            Mensajes.error(this, "Seleccione un partido de la lista.");
            return;
        }
        if (!Mensajes.confirmar(this, "¿Desea eliminar el partido seleccionado?")) {
            return;
        }
        try {
            partidoService.eliminar(idSeleccionado);
            limpiarFormulario();
            refrescar();
        } catch (SQLException e) {
            Mensajes.error(this, "No se pudo eliminar el partido", e);
        }
    }

    private void generarBonificaciones() {
        if (idSeleccionado == null) {
            Mensajes.error(this, "Seleccione un partido finalizado de la lista.");
            return;
        }
        try {
            int generadas = bonificacionService.generarBonificacionesPartido(idSeleccionado);
            Mensajes.info(this, "Se generaron " + generadas + " bonificaciones para este partido.");
        } catch (IllegalStateException | IllegalArgumentException e) {
            Mensajes.error(this, e.getMessage());
        } catch (SQLException e) {
            Mensajes.error(this, "No se pudieron generar las bonificaciones", e);
        }
    }

    @Override
    public void refrescar() {
        try {
            partidosCargados = partidoService.listar();
            renderizarLista();
        } catch (SQLException e) {
            Mensajes.error(this, "No se pudo cargar los partidos", e);
        }
    }

    /** Fila clicable con la tarjeta de partido, la insignia de estado y la condicion. */
    private final class TarjetaFilaPartido extends JPanel {

        TarjetaFilaPartido(Partido partido) {
            setLayout(new BorderLayout(16, 0));
            setBackground(ColoresBlaugrana.BLANCO);
            setBorder(bordeNormal());
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setMaximumSize(new Dimension(Integer.MAX_VALUE, 110));

            JPanel envoltorioTarjeta = new JPanel(new BorderLayout());
            envoltorioTarjeta.setOpaque(false);
            envoltorioTarjeta.setBorder(new EmptyBorder(Medidas.PADDING_TARJETA, Medidas.PADDING_TARJETA,
                    Medidas.PADDING_TARJETA, 0));
            envoltorioTarjeta.add(new TarjetaPartido(partido, false), BorderLayout.CENTER);

            JPanel lateral = new JPanel();
            lateral.setOpaque(false);
            lateral.setLayout(new BoxLayout(lateral, BoxLayout.Y_AXIS));
            lateral.setBorder(new EmptyBorder(Medidas.PADDING_TARJETA, 0, Medidas.PADDING_TARJETA,
                    Medidas.PADDING_TARJETA));

            JPanel filaInsignia = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
            filaInsignia.setOpaque(false);
            filaInsignia.setAlignmentX(Component.RIGHT_ALIGNMENT);
            filaInsignia.add(new Insignia(partido.getEstado()));

            JLabel lblCondicion = new JLabel(
                    Partido.CONDICION_LOCAL.equals(partido.getCondicion()) ? "Local" : "Visitante");
            lblCondicion.setFont(Tipografia.NOTA);
            lblCondicion.setForeground(ColoresBlaugrana.GRIS_TEXTO_SUAVE);
            lblCondicion.setAlignmentX(Component.RIGHT_ALIGNMENT);
            lblCondicion.setHorizontalAlignment(SwingConstants.RIGHT);

            lateral.add(Box.createVerticalGlue());
            lateral.add(filaInsignia);
            lateral.add(Box.createVerticalStrut(6));
            lateral.add(lblCondicion);
            lateral.add(Box.createVerticalGlue());

            add(envoltorioTarjeta, BorderLayout.CENTER);
            add(lateral, BorderLayout.EAST);

            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    seleccionarPartido(partido.getIdPartido());
                }
            });
        }

        void setSeleccionada(boolean seleccionada) {
            setBorder(seleccionada ? bordeSeleccionado() : bordeNormal());
        }

        private Border bordeNormal() {
            return BorderFactory.createLineBorder(ColoresBlaugrana.GRIS_MEDIO, 1);
        }

        private Border bordeSeleccionado() {
            return BorderFactory.createLineBorder(ColoresBlaugrana.DORADO, 2);
        }
    }
}
