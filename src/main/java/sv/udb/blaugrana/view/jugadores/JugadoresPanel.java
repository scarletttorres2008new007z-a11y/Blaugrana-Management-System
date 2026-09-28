package sv.udb.blaugrana.view.jugadores;

import sv.udb.blaugrana.model.Jugador;
import sv.udb.blaugrana.service.JugadorService;
import sv.udb.blaugrana.util.ColoresBlaugrana;
import sv.udb.blaugrana.util.Medidas;
import sv.udb.blaugrana.util.Mensajes;
import sv.udb.blaugrana.util.PermisosUI;
import sv.udb.blaugrana.util.Tipografia;
import sv.udb.blaugrana.util.Validaciones;
import sv.udb.blaugrana.view.Refrescable;
import sv.udb.blaugrana.view.componentes.AvatarJugador;
import sv.udb.blaugrana.view.componentes.EncabezadoSeccion;
import sv.udb.blaugrana.view.componentes.Insignia;

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
 * Plantilla mostrada como una cuadricula de tarjetas de jugador (foto o
 * silueta, dorsal, nombre, posicion y estado), en vez de una tabla plana.
 * Al hacer clic se selecciona el jugador para el formulario de edicion; con
 * doble clic (o el boton "Ver perfil") se abre su ficha detallada.
 */
public class JugadoresPanel extends JPanel implements Refrescable {

    private final JugadorService jugadorService = new JugadorService();

    private final JPanel panelTarjetas = new JPanel(new GridLayout(0, 4, 16, 16));
    private final Map<Integer, TarjetaJugador> tarjetasPorId = new LinkedHashMap<>();
    private List<Jugador> jugadoresCargados = new ArrayList<>();

    private final JTextField txtBuscar = new JTextField(20);

    private final JTextField txtNumero = new JTextField(4);
    private final JTextField txtNombre = new JTextField(14);
    private final JTextField txtApellido = new JTextField(14);
    private final JTextField txtDocumento = new JTextField(12);
    private final JTextField txtNacionalidad = new JTextField(12);
    private final JTextField txtFechaNacimiento = new JTextField(10);
    private final JTextField txtFechaIngreso = new JTextField(10);
    private final JComboBox<String> cmbPosicion = new JComboBox<>(
            new String[]{"Portero", "Defensa", "Centrocampista", "Delantero"});
    private final JComboBox<String> cmbEstado = new JComboBox<>(new String[]{"ACTIVO", "INACTIVO"});

    private Integer idSeleccionado;

    public JugadoresPanel() {
        setLayout(new BorderLayout());
        setBackground(ColoresBlaugrana.GRIS_CLARO);
        setBorder(new EmptyBorder(20, 20, 20, 20));

        add(construirEncabezado(), BorderLayout.NORTH);

        panelTarjetas.setOpaque(false);
        JPanel envoltorioTarjetas = new JPanel(new BorderLayout());
        envoltorioTarjetas.setOpaque(false);
        envoltorioTarjetas.add(panelTarjetas, BorderLayout.NORTH);

        JScrollPane scroll = new JScrollPane(envoltorioTarjetas);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        add(scroll, BorderLayout.CENTER);

        add(construirFormulario(), BorderLayout.SOUTH);

        txtBuscar.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                renderizarTarjetas();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                renderizarTarjetas();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                renderizarTarjetas();
            }
        });
    }

    private JPanel construirEncabezado() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);
        panel.add(new EncabezadoSeccion("Plantilla", "Gestión de jugadores del primer equipo"), BorderLayout.NORTH);

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
                BorderFactory.createTitledBorder("Datos del jugador")));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 4, 4, 4);
        gbc.anchor = GridBagConstraints.WEST;

        agregarCampo(panel, gbc, 0, 0, "N° camiseta:", txtNumero);
        agregarCampo(panel, gbc, 2, 0, "Nombre:", txtNombre);
        agregarCampo(panel, gbc, 4, 0, "Apellido:", txtApellido);

        agregarCampo(panel, gbc, 0, 1, "Documento:", txtDocumento);
        agregarCampo(panel, gbc, 2, 1, "Nacionalidad:", txtNacionalidad);
        agregarCampoComponente(panel, gbc, 4, 1, "Posicion:", cmbPosicion);

        agregarCampo(panel, gbc, 0, 2, "Fecha nacimiento (yyyy-mm-dd):", txtFechaNacimiento);
        agregarCampo(panel, gbc, 2, 2, "Fecha ingreso (yyyy-mm-dd):", txtFechaIngreso);
        agregarCampoComponente(panel, gbc, 4, 2, "Estado:", cmbEstado);

        JButton btnNuevo = new JButton("Nuevo");
        JButton btnGuardar = new JButton("Guardar");
        JButton btnEliminar = new JButton("Eliminar");
        JButton btnVerPerfil = new JButton("Ver perfil");
        JButton btnRefrescar = new JButton("Refrescar");

        btnNuevo.addActionListener(e -> limpiarFormulario());
        btnGuardar.addActionListener(e -> guardar());
        btnEliminar.addActionListener(e -> eliminar());
        btnVerPerfil.addActionListener(e -> verPerfil());
        btnRefrescar.addActionListener(e -> refrescar());
        PermisosUI.deshabilitarSiSoloLectura(btnGuardar, btnEliminar);

        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        panelBotones.setOpaque(false);
        panelBotones.add(btnNuevo);
        panelBotones.add(btnGuardar);
        panelBotones.add(btnEliminar);
        panelBotones.add(btnVerPerfil);
        panelBotones.add(btnRefrescar);

        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 6;
        panel.add(panelBotones, gbc);

        return panel;
    }

    private void agregarCampo(JPanel panel, GridBagConstraints gbc, int x, int y, String etiqueta, JTextField campo) {
        agregarCampoComponente(panel, gbc, x, y, etiqueta, campo);
    }

    private void agregarCampoComponente(JPanel panel, GridBagConstraints gbc, int x, int y, String etiqueta, JComponent campo) {
        gbc.gridwidth = 1;
        gbc.gridx = x;
        gbc.gridy = y;
        panel.add(new JLabel(etiqueta), gbc);
        gbc.gridx = x + 1;
        panel.add(campo, gbc);
    }

    private void renderizarTarjetas() {
        String filtro = normalizar(txtBuscar.getText());
        panelTarjetas.removeAll();
        tarjetasPorId.clear();

        for (Jugador jugador : jugadoresCargados) {
            if (!filtro.isBlank() && !coincide(jugador, filtro)) {
                continue;
            }
            TarjetaJugador tarjeta = new TarjetaJugador(jugador);
            tarjeta.setSeleccionada(idSeleccionado != null && idSeleccionado == jugador.getIdJugador());
            tarjetasPorId.put(jugador.getIdJugador(), tarjeta);
            panelTarjetas.add(tarjeta);
        }

        if (tarjetasPorId.isEmpty()) {
            JLabel lblVacio = new JLabel("No se encontraron jugadores.");
            lblVacio.setFont(Tipografia.CUERPO);
            lblVacio.setForeground(ColoresBlaugrana.GRIS_TEXTO_SUAVE);
            panelTarjetas.add(lblVacio);
        }

        panelTarjetas.revalidate();
        panelTarjetas.repaint();
    }

    private boolean coincide(Jugador jugador, String filtroNormalizado) {
        String texto = normalizar(String.join(" ",
                jugador.getNombreCompleto(),
                String.valueOf(jugador.getNumeroCamiseta()),
                String.valueOf(jugador.getPosicion()),
                String.valueOf(jugador.getNacionalidad()),
                String.valueOf(jugador.getEstado())));
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

    private void seleccionarJugador(int idJugador) {
        idSeleccionado = idJugador;
        for (Map.Entry<Integer, TarjetaJugador> entrada : tarjetasPorId.entrySet()) {
            entrada.getValue().setSeleccionada(entrada.getKey() == idJugador);
        }
        try {
            jugadorService.buscarPorId(idJugador).ifPresent(j -> {
                txtNumero.setText(String.valueOf(j.getNumeroCamiseta()));
                txtNombre.setText(j.getNombre());
                txtApellido.setText(j.getApellido());
                txtDocumento.setText(j.getDocumento());
                txtNacionalidad.setText(j.getNacionalidad());
                txtFechaNacimiento.setText(j.getFechaNacimiento() != null ? j.getFechaNacimiento().toString() : "");
                txtFechaIngreso.setText(j.getFechaIngreso() != null ? j.getFechaIngreso().toString() : "");
                cmbPosicion.setSelectedItem(j.getPosicion());
                cmbEstado.setSelectedItem(j.getEstado());
            });
        } catch (SQLException e) {
            Mensajes.error(this, "No se pudo cargar el jugador", e);
        }
    }

    private void limpiarFormulario() {
        idSeleccionado = null;
        for (TarjetaJugador tarjeta : tarjetasPorId.values()) {
            tarjeta.setSeleccionada(false);
        }
        txtNumero.setText("");
        txtNombre.setText("");
        txtApellido.setText("");
        txtDocumento.setText("");
        txtNacionalidad.setText("");
        txtFechaNacimiento.setText("");
        txtFechaIngreso.setText(LocalDate.now().toString());
        cmbPosicion.setSelectedIndex(0);
        cmbEstado.setSelectedIndex(0);
    }

    private void guardar() {
        if (Validaciones.esVacio(txtNombre.getText()) || Validaciones.esVacio(txtApellido.getText())) {
            Mensajes.error(this, "El nombre y el apellido son obligatorios.");
            return;
        }
        try {
            Jugador jugador = new Jugador();
            if (idSeleccionado != null) {
                jugador.setIdJugador(idSeleccionado);
            }
            jugador.setNumeroCamiseta(Integer.parseInt(txtNumero.getText().trim()));
            jugador.setNombre(txtNombre.getText().trim());
            jugador.setApellido(txtApellido.getText().trim());
            jugador.setDocumento(txtDocumento.getText().trim());
            jugador.setNacionalidad(txtNacionalidad.getText().trim());
            jugador.setPosicion((String) cmbPosicion.getSelectedItem());
            jugador.setEstado((String) cmbEstado.getSelectedItem());
            jugador.setFechaNacimiento(parseFecha(txtFechaNacimiento.getText()));
            LocalDate fechaIngreso = parseFecha(txtFechaIngreso.getText());
            jugador.setFechaIngreso(fechaIngreso != null ? fechaIngreso : LocalDate.now());

            jugadorService.guardar(jugador);
            Mensajes.info(this, "Jugador guardado correctamente.");
            limpiarFormulario();
            refrescar();
        } catch (NumberFormatException e) {
            Mensajes.error(this, "El numero de camiseta debe ser un valor numerico.");
        } catch (DateTimeParseException e) {
            Mensajes.error(this, "Las fechas deben tener el formato yyyy-mm-dd.");
        } catch (IllegalArgumentException | IllegalStateException e) {
            Mensajes.error(this, e.getMessage());
        } catch (SQLException e) {
            Mensajes.error(this, "No se pudo guardar el jugador", e);
        }
    }

    private void eliminar() {
        if (idSeleccionado == null) {
            Mensajes.error(this, "Seleccione un jugador de la plantilla.");
            return;
        }
        if (!Mensajes.confirmar(this, "¿Desea eliminar al jugador seleccionado?")) {
            return;
        }
        try {
            jugadorService.eliminar(idSeleccionado);
            limpiarFormulario();
            refrescar();
        } catch (SQLException e) {
            Mensajes.error(this, "No se pudo eliminar el jugador", e);
        }
    }

    private void verPerfil() {
        if (idSeleccionado == null) {
            Mensajes.error(this, "Seleccione un jugador de la plantilla.");
            return;
        }
        try {
            jugadorService.buscarPorId(idSeleccionado).ifPresentOrElse(
                    jugador -> {
                        Frame ventana = (Frame) SwingUtilities.getWindowAncestor(this);
                        new PerfilJugadorDialog(ventana, jugador).setVisible(true);
                    },
                    () -> Mensajes.error(this, "El jugador seleccionado ya no existe.")
            );
        } catch (SQLException e) {
            Mensajes.error(this, "No se pudo cargar el perfil del jugador", e);
        }
    }

    private LocalDate parseFecha(String texto) {
        if (Validaciones.esVacio(texto)) {
            return null;
        }
        return LocalDate.parse(texto.trim());
    }

    @Override
    public void refrescar() {
        try {
            jugadoresCargados = jugadorService.listar();
            renderizarTarjetas();
        } catch (SQLException e) {
            Mensajes.error(this, "No se pudo cargar la plantilla", e);
        }
    }

    /** Tarjeta clicable de un jugador: foto/silueta, dorsal, nombre, posicion y estado. */
    private final class TarjetaJugador extends JPanel {

        TarjetaJugador(Jugador jugador) {
            setLayout(new BorderLayout());
            setBackground(ColoresBlaugrana.BLANCO);
            setBorder(bordeNormal());
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

            JPanel contenido = new JPanel();
            contenido.setOpaque(false);
            contenido.setLayout(new BoxLayout(contenido, BoxLayout.Y_AXIS));
            contenido.setBorder(new EmptyBorder(Medidas.PADDING_TARJETA, Medidas.PADDING_TARJETA,
                    Medidas.PADDING_TARJETA, Medidas.PADDING_TARJETA));

            AvatarJugador avatar = new AvatarJugador(jugador.getNumeroCamiseta(), 96, 96);
            avatar.setAlignmentX(Component.CENTER_ALIGNMENT);

            JLabel lblDorsal = new JLabel("Nº " + jugador.getNumeroCamiseta(), SwingConstants.CENTER);
            lblDorsal.setFont(Tipografia.ETIQUETA);
            lblDorsal.setForeground(ColoresBlaugrana.DORADO);
            lblDorsal.setAlignmentX(Component.CENTER_ALIGNMENT);

            JLabel lblNombre = new JLabel(jugador.getNombreCompleto(), SwingConstants.CENTER);
            lblNombre.setFont(Tipografia.CUERPO_NEGRITA);
            lblNombre.setForeground(ColoresBlaugrana.AZUL_OSCURO);
            lblNombre.setAlignmentX(Component.CENTER_ALIGNMENT);

            JLabel lblPosicion = new JLabel(String.valueOf(jugador.getPosicion()), SwingConstants.CENTER);
            lblPosicion.setFont(Tipografia.NOTA);
            lblPosicion.setForeground(ColoresBlaugrana.GRIS_TEXTO_SUAVE);
            lblPosicion.setAlignmentX(Component.CENTER_ALIGNMENT);

            JPanel filaInsignia = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 6));
            filaInsignia.setOpaque(false);
            filaInsignia.setAlignmentX(Component.CENTER_ALIGNMENT);
            filaInsignia.add(new Insignia(jugador.getEstado()));

            contenido.add(avatar);
            contenido.add(Box.createVerticalStrut(8));
            contenido.add(lblDorsal);
            contenido.add(lblNombre);
            contenido.add(lblPosicion);
            contenido.add(filaInsignia);

            add(contenido, BorderLayout.CENTER);

            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    seleccionarJugador(jugador.getIdJugador());
                    if (e.getClickCount() == 2) {
                        verPerfil();
                    }
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
