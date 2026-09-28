package sv.udb.blaugrana.view;

import sv.udb.blaugrana.session.SesionUsuario;
import sv.udb.blaugrana.util.ColoresBlaugrana;
import sv.udb.blaugrana.view.bonificaciones.BonificacionesPanel;
import sv.udb.blaugrana.view.contratos.ContratosPanel;
import sv.udb.blaugrana.view.egresos.EgresosPanel;
import sv.udb.blaugrana.view.ingresos.IngresosPanel;
import sv.udb.blaugrana.view.jugadores.JugadoresPanel;
import sv.udb.blaugrana.view.pagos.PagosPanel;
import sv.udb.blaugrana.view.partidos.PartidosPanel;
import sv.udb.blaugrana.view.personal.PersonalPanel;
import sv.udb.blaugrana.view.presupuesto.PresupuestoPanel;
import sv.udb.blaugrana.view.rendimiento.RendimientoPanel;
import sv.udb.blaugrana.view.reportes.ReportesPanel;
import sv.udb.blaugrana.view.usuarios.UsuariosPanel;

import javax.swing.*;
import java.awt.*;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Supplier;

public class FrmPrincipal extends JFrame {

    private final CardLayout cardLayout = new CardLayout();
    private final JPanel panelContenido = new JPanel(cardLayout);
    private final Map<String, JComponent> paneles = new LinkedHashMap<>();

    public FrmPrincipal() {
        super("Blaugrana Management System");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1200, 750);
        setLocationRelativeTo(null);
        setMinimumSize(new Dimension(1000, 650));
        construirInterfaz();
    }

    private void construirInterfaz() {
        panelContenido.setBackground(ColoresBlaugrana.GRIS_CLARO);

        registrarPanelSiPermitido("dashboard", DashboardPanel::new);
        registrarPanelSiPermitido("jugadores", JugadoresPanel::new);
        registrarPanelSiPermitido("personal", PersonalPanel::new);
        registrarPanelSiPermitido("contratos", ContratosPanel::new);
        registrarPanelSiPermitido("partidos", PartidosPanel::new);
        registrarPanelSiPermitido("rendimiento", RendimientoPanel::new);
        registrarPanelSiPermitido("bonificaciones", BonificacionesPanel::new);
        registrarPanelSiPermitido("ingresos", IngresosPanel::new);
        registrarPanelSiPermitido("egresos", EgresosPanel::new);
        registrarPanelSiPermitido("presupuesto", PresupuestoPanel::new);
        registrarPanelSiPermitido("pagos", PagosPanel::new);
        registrarPanelSiPermitido("reportes", ReportesPanel::new);
        registrarPanelSiPermitido("usuarios", UsuariosPanel::new);

        JPanel panelRaiz = new JPanel(new BorderLayout());
        panelRaiz.add(construirEncabezado(), BorderLayout.NORTH);
        panelRaiz.add(construirMenuLateral(), BorderLayout.WEST);
        panelRaiz.add(panelContenido, BorderLayout.CENTER);
        setContentPane(panelRaiz);
        mostrarPanel("dashboard");
    }

    private void registrarPanelSiPermitido(String clave, Supplier<JComponent> fabricante) {
        if (!SesionUsuario.puedeAcceder(clave)) {
            return;
        }
        JComponent panel = fabricante.get();
        paneles.put(clave, panel);
        panelContenido.add(panel, clave);
    }

    private void mostrarPanel(String clave) {
        JComponent panel = paneles.get(clave);
        if (panel == null) {
            return;
        }
        if (panel instanceof Refrescable refrescable) {
            refrescable.refrescar();
        }
        cardLayout.show(panelContenido, clave);
    }

    private JPanel construirEncabezado() {
        JPanel encabezado = new JPanel(new BorderLayout());
        encabezado.setBackground(ColoresBlaugrana.AZUL_OSCURO);
        encabezado.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

        JLabel titulo = new JLabel("BLAUGRANA MANAGEMENT");
        titulo.setFont(new Font("SansSerif", Font.BOLD, 20));
        titulo.setForeground(ColoresBlaugrana.DORADO);

        String nombreUsuario = SesionUsuario.getUsuarioActivo() != null
                ? SesionUsuario.getUsuarioActivo().getNombreCompleto()
                : "Invitado";
        JLabel usuario = new JLabel(nombreUsuario + "  ");
        usuario.setForeground(ColoresBlaugrana.BLANCO);
        usuario.setFont(new Font("SansSerif", Font.PLAIN, 13));

        JButton btnCerrarSesion = new JButton("Cerrar sesion");
        btnCerrarSesion.setFocusPainted(false);
        btnCerrarSesion.addActionListener(e -> cerrarSesion());

        JPanel panelDerecho = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        panelDerecho.setOpaque(false);
        panelDerecho.add(usuario);
        panelDerecho.add(btnCerrarSesion);

        encabezado.add(titulo, BorderLayout.WEST);
        encabezado.add(panelDerecho, BorderLayout.EAST);
        return encabezado;
    }

    private JScrollPane construirMenuLateral() {
        JPanel menu = new JPanel();
        menu.setLayout(new BoxLayout(menu, BoxLayout.Y_AXIS));
        menu.setBackground(ColoresBlaugrana.GRANATE);
        menu.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));

        agregarGrupo(menu, "INICIO", new String[][]{{"Dashboard", "dashboard"}});
        agregarGrupo(menu, "CLUB", new String[][]{
                {"Jugadores", "jugadores"}, {"Personal", "personal"}, {"Contratos", "contratos"}});
        agregarGrupo(menu, "DEPORTIVO", new String[][]{
                {"Partidos", "partidos"}, {"Rendimiento", "rendimiento"}, {"Bonificaciones", "bonificaciones"}});
        agregarGrupo(menu, "FINANZAS", new String[][]{
                {"Pagos", "pagos"}, {"Ingresos", "ingresos"}, {"Egresos", "egresos"}, {"Presupuesto", "presupuesto"}});
        agregarGrupo(menu, "REPORTES", new String[][]{{"Reportes", "reportes"}});
        agregarGrupo(menu, "ADMINISTRACION", new String[][]{{"Usuarios", "usuarios"}});

        menu.add(Box.createVerticalGlue());

        JScrollPane scroll = new JScrollPane(menu);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.setPreferredSize(new Dimension(210, 0));
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        return scroll;
    }

    private void agregarGrupo(JPanel menu, String tituloSeccion, String[][] opciones) {
        boolean hayAlgunaVisible = false;
        for (String[] opcion : opciones) {
            if (paneles.containsKey(opcion[1])) {
                hayAlgunaVisible = true;
                break;
            }
        }
        if (!hayAlgunaVisible) {
            return;
        }
        agregarSeccion(menu, tituloSeccion);
        for (String[] opcion : opciones) {
            if (paneles.containsKey(opcion[1])) {
                agregarOpcion(menu, opcion[0], opcion[1]);
            }
        }
    }

    private void agregarSeccion(JPanel menu, String texto) {
        JLabel etiqueta = new JLabel(texto);
        etiqueta.setForeground(ColoresBlaugrana.DORADO);
        etiqueta.setFont(new Font("SansSerif", Font.BOLD, 11));
        etiqueta.setBorder(BorderFactory.createEmptyBorder(14, 16, 4, 16));
        etiqueta.setAlignmentX(Component.LEFT_ALIGNMENT);
        menu.add(etiqueta);
    }

    private void agregarOpcion(JPanel menu, String texto, String clave) {
        JButton boton = new JButton(texto);
        boton.setAlignmentX(Component.LEFT_ALIGNMENT);
        boton.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
        boton.setHorizontalAlignment(SwingConstants.LEFT);
        boton.setBackground(ColoresBlaugrana.GRANATE);
        boton.setForeground(ColoresBlaugrana.BLANCO);
        boton.setBorder(BorderFactory.createEmptyBorder(6, 24, 6, 10));
        boton.setFocusPainted(false);
        boton.setBorderPainted(false);
        boton.setContentAreaFilled(true);
        boton.addActionListener(e -> mostrarPanel(clave));
        menu.add(boton);
    }

    private void cerrarSesion() {
        SesionUsuario.cerrar();
        dispose();
        new FrmLogin().setVisible(true);
    }
}
