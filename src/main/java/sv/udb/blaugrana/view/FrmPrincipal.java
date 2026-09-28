package sv.udb.blaugrana.view;

import sv.udb.blaugrana.session.SesionUsuario;
import sv.udb.blaugrana.util.ColoresBlaugrana;
import sv.udb.blaugrana.util.Tipografia;
import sv.udb.blaugrana.view.bonificaciones.BonificacionesPanel;
import sv.udb.blaugrana.view.componentes.AvatarIniciales;
import sv.udb.blaugrana.view.componentes.IconoMenu;
import sv.udb.blaugrana.view.componentes.PanelDegradado;
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

    private record OpcionMenu(String etiqueta, String clave, IconoMenu.Tipo icono) {
    }

    private final CardLayout cardLayout = new CardLayout();
    private final JPanel panelContenido = new JPanel(cardLayout);
    private final Map<String, JComponent> paneles = new LinkedHashMap<>();
    private final Map<String, BotonMenuLateral> botonesMenu = new LinkedHashMap<>();
    private String claveActual;

    public FrmPrincipal() {
        super("Blaugrana Management System");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1250, 780);
        setLocationRelativeTo(null);
        setMinimumSize(new Dimension(1050, 680));
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
        claveActual = clave;
        for (Map.Entry<String, BotonMenuLateral> entrada : botonesMenu.entrySet()) {
            entrada.getValue().setSeleccionado(entrada.getKey().equals(clave));
        }
        if (panel instanceof Refrescable refrescable) {
            refrescable.refrescar();
        }
        cardLayout.show(panelContenido, clave);
    }

    private JPanel construirEncabezado() {
        PanelDegradado encabezado = new PanelDegradado(new BorderLayout(),
                ColoresBlaugrana.AZUL_OSCURO, ColoresBlaugrana.AZUL_MEDIO, true);
        encabezado.setBorder(BorderFactory.createEmptyBorder(12, 24, 12, 24));

        JPanel bloqueTitulo = new JPanel();
        bloqueTitulo.setOpaque(false);
        bloqueTitulo.setLayout(new BoxLayout(bloqueTitulo, BoxLayout.Y_AXIS));

        JLabel titulo = new JLabel("BLAUGRANA MANAGEMENT");
        titulo.setFont(Tipografia.DISPLAY);
        titulo.setForeground(ColoresBlaugrana.DORADO);
        titulo.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel subtitulo = new JLabel("Sistema Integral de Gestión Deportiva");
        subtitulo.setFont(Tipografia.NOTA);
        subtitulo.setForeground(ColoresBlaugrana.GRIS_CLARO);
        subtitulo.setAlignmentX(Component.LEFT_ALIGNMENT);

        bloqueTitulo.add(titulo);
        bloqueTitulo.add(subtitulo);

        String nombreUsuario = SesionUsuario.getUsuarioActivo() != null
                ? SesionUsuario.getUsuarioActivo().getNombreCompleto()
                : "Invitado";
        String rolUsuario = SesionUsuario.getUsuarioActivo() != null
                ? SesionUsuario.getUsuarioActivo().getNombreRol()
                : "";

        JPanel bloqueUsuario = new JPanel();
        bloqueUsuario.setOpaque(false);
        bloqueUsuario.setLayout(new BoxLayout(bloqueUsuario, BoxLayout.Y_AXIS));
        JLabel lblNombre = new JLabel(nombreUsuario);
        lblNombre.setFont(Tipografia.CUERPO_NEGRITA);
        lblNombre.setForeground(ColoresBlaugrana.BLANCO);
        lblNombre.setAlignmentX(Component.RIGHT_ALIGNMENT);
        JLabel lblRol = new JLabel(rolUsuario);
        lblRol.setFont(Tipografia.NOTA);
        lblRol.setForeground(ColoresBlaugrana.GRIS_CLARO);
        lblRol.setAlignmentX(Component.RIGHT_ALIGNMENT);
        bloqueUsuario.add(lblNombre);
        bloqueUsuario.add(lblRol);

        JButton btnCerrarSesion = new JButton("Cerrar sesión");
        btnCerrarSesion.setFocusPainted(false);
        btnCerrarSesion.addActionListener(e -> cerrarSesion());

        JPanel panelDerecho = new JPanel(new FlowLayout(FlowLayout.RIGHT, 14, 0));
        panelDerecho.setOpaque(false);
        panelDerecho.add(new AvatarIniciales(nombreUsuario, ColoresBlaugrana.GRANATE, 38));
        panelDerecho.add(bloqueUsuario);
        panelDerecho.add(btnCerrarSesion);

        encabezado.add(bloqueTitulo, BorderLayout.WEST);
        encabezado.add(panelDerecho, BorderLayout.EAST);
        return encabezado;
    }

    private JScrollPane construirMenuLateral() {
        PanelDegradado menu = new PanelDegradado(null, ColoresBlaugrana.GRANATE, ColoresBlaugrana.GRANATE_OSCURO, false);
        menu.setLayout(new BoxLayout(menu, BoxLayout.Y_AXIS));
        menu.setBorder(BorderFactory.createEmptyBorder(14, 0, 10, 0));

        agregarGrupo(menu, "INICIO", new OpcionMenu[]{
                new OpcionMenu("Dashboard", "dashboard", IconoMenu.Tipo.DASHBOARD)});
        agregarGrupo(menu, "CLUB", new OpcionMenu[]{
                new OpcionMenu("Jugadores", "jugadores", IconoMenu.Tipo.JUGADORES),
                new OpcionMenu("Personal", "personal", IconoMenu.Tipo.PERSONAL),
                new OpcionMenu("Contratos", "contratos", IconoMenu.Tipo.CONTRATOS)});
        agregarGrupo(menu, "DEPORTIVO", new OpcionMenu[]{
                new OpcionMenu("Partidos", "partidos", IconoMenu.Tipo.PARTIDOS),
                new OpcionMenu("Rendimiento", "rendimiento", IconoMenu.Tipo.RENDIMIENTO),
                new OpcionMenu("Bonificaciones", "bonificaciones", IconoMenu.Tipo.BONIFICACIONES)});
        agregarGrupo(menu, "FINANZAS", new OpcionMenu[]{
                new OpcionMenu("Pagos", "pagos", IconoMenu.Tipo.PAGOS),
                new OpcionMenu("Ingresos", "ingresos", IconoMenu.Tipo.INGRESOS),
                new OpcionMenu("Egresos", "egresos", IconoMenu.Tipo.EGRESOS),
                new OpcionMenu("Presupuesto", "presupuesto", IconoMenu.Tipo.PRESUPUESTO)});
        agregarGrupo(menu, "REPORTES", new OpcionMenu[]{
                new OpcionMenu("Reportes", "reportes", IconoMenu.Tipo.REPORTES)});
        agregarGrupo(menu, "ADMINISTRACIÓN", new OpcionMenu[]{
                new OpcionMenu("Usuarios", "usuarios", IconoMenu.Tipo.USUARIOS)});

        menu.add(Box.createVerticalGlue());

        JScrollPane scroll = new JScrollPane(menu);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.setPreferredSize(new Dimension(225, 0));
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        return scroll;
    }

    private void agregarGrupo(JPanel menu, String tituloSeccion, OpcionMenu[] opciones) {
        boolean hayAlgunaVisible = false;
        for (OpcionMenu opcion : opciones) {
            if (paneles.containsKey(opcion.clave())) {
                hayAlgunaVisible = true;
                break;
            }
        }
        if (!hayAlgunaVisible) {
            return;
        }
        agregarSeccion(menu, tituloSeccion);
        for (OpcionMenu opcion : opciones) {
            if (paneles.containsKey(opcion.clave())) {
                agregarOpcion(menu, opcion);
            }
        }
    }

    private void agregarSeccion(JPanel menu, String texto) {
        JLabel etiqueta = new JLabel(texto);
        etiqueta.setForeground(ColoresBlaugrana.DORADO);
        etiqueta.setFont(Tipografia.ETIQUETA);
        etiqueta.setBorder(BorderFactory.createEmptyBorder(16, 20, 6, 16));
        etiqueta.setAlignmentX(Component.LEFT_ALIGNMENT);
        menu.add(etiqueta);
    }

    private void agregarOpcion(JPanel menu, OpcionMenu opcion) {
        BotonMenuLateral boton = new BotonMenuLateral(opcion.etiqueta(), new IconoMenu(opcion.icono(), 18));
        boton.addActionListener(e -> mostrarPanel(opcion.clave()));
        botonesMenu.put(opcion.clave(), boton);
        menu.add(boton);
    }

    private void cerrarSesion() {
        SesionUsuario.cerrar();
        dispose();
        new FrmLogin().setVisible(true);
    }

    /** Boton de navegacion del sidebar, con estado normal / hover / seleccionado. */
    private static final class BotonMenuLateral extends JButton {

        private boolean seleccionado;

        BotonMenuLateral(String texto, Icon icono) {
            super(texto, icono);
            setHorizontalAlignment(SwingConstants.LEFT);
            setIconTextGap(14);
            setAlignmentX(Component.LEFT_ALIGNMENT);
            setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
            setBorder(BorderFactory.createEmptyBorder(8, 20, 8, 12));
            setFont(Tipografia.CUERPO_NEGRITA);
            setForeground(ColoresBlaugrana.BLANCO);
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setOpaque(false);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        }

        void setSeleccionado(boolean seleccionado) {
            this.seleccionado = seleccionado;
            setForeground(seleccionado ? ColoresBlaugrana.DORADO : ColoresBlaugrana.BLANCO);
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            if (seleccionado) {
                g2.setColor(ColoresBlaugrana.AZUL_OSCURO);
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.setColor(ColoresBlaugrana.DORADO);
                g2.fillRect(0, 0, 4, getHeight());
            } else if (getModel().isRollover()) {
                g2.setColor(ColoresBlaugrana.GRANATE_OSCURO);
                g2.fillRect(0, 0, getWidth(), getHeight());
            }
            g2.dispose();
            super.paintComponent(g);
        }
    }
}
