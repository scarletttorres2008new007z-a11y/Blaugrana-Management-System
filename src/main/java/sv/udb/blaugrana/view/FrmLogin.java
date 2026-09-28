package sv.udb.blaugrana.view;

import sv.udb.blaugrana.model.Usuario;
import sv.udb.blaugrana.service.LoginService;
import sv.udb.blaugrana.session.SesionUsuario;
import sv.udb.blaugrana.util.ColoresBlaugrana;
import sv.udb.blaugrana.util.Mensajes;
import sv.udb.blaugrana.util.Tipografia;
import sv.udb.blaugrana.view.componentes.EscudoEquipo;
import sv.udb.blaugrana.view.componentes.PanelDegradado;

import javax.swing.*;
import java.awt.*;
import java.sql.SQLException;
import java.util.Optional;

public class FrmLogin extends JFrame {

    private final JTextField txtUsuario = new JTextField(18);
    private final JPasswordField txtContrasena = new JPasswordField(18);
    private final LoginService loginService = new LoginService();

    public FrmLogin() {
        super("Blaugrana Management - Iniciar sesion");
        construirInterfaz();
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(420, 460);
        setLocationRelativeTo(null);
        setResizable(false);
    }

    private void construirInterfaz() {
        JPanel panelRaiz = new JPanel(new BorderLayout());
        panelRaiz.setBackground(ColoresBlaugrana.AZUL_OSCURO);

        PanelDegradado panelSuperior = new PanelDegradado(new BorderLayout(),
                ColoresBlaugrana.AZUL_OSCURO, ColoresBlaugrana.AZUL_MEDIO, true);

        JPanel bloqueTitulo = new JPanel();
        bloqueTitulo.setOpaque(false);
        bloqueTitulo.setLayout(new BoxLayout(bloqueTitulo, BoxLayout.Y_AXIS));
        bloqueTitulo.setBorder(BorderFactory.createEmptyBorder(24, 10, 20, 10));

        EscudoEquipo escudo = new EscudoEquipo("FC Barcelona", true, 56);
        escudo.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel titulo = new JLabel("BLAUGRANA MANAGEMENT", SwingConstants.CENTER);
        titulo.setFont(Tipografia.DISPLAY);
        titulo.setForeground(ColoresBlaugrana.DORADO);
        titulo.setAlignmentX(Component.CENTER_ALIGNMENT);
        titulo.setBorder(BorderFactory.createEmptyBorder(10, 10, 4, 10));

        JLabel subtitulo = new JLabel("<html><div style='text-align:center;'>Sistema Integral de Gestión Deportiva,<br>"
                + "Financiera y Administrativa</div></html>", SwingConstants.CENTER);
        subtitulo.setFont(Tipografia.NOTA);
        subtitulo.setForeground(ColoresBlaugrana.DORADO_SUAVE);
        subtitulo.setAlignmentX(Component.CENTER_ALIGNMENT);

        bloqueTitulo.add(escudo);
        bloqueTitulo.add(titulo);
        bloqueTitulo.add(subtitulo);
        panelSuperior.add(bloqueTitulo, BorderLayout.CENTER);

        JPanel panelFormulario = new JPanel();
        panelFormulario.setBackground(ColoresBlaugrana.BLANCO);
        panelFormulario.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));
        panelFormulario.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;

        JLabel lblUsuario = new JLabel("Usuario:");
        lblUsuario.setFont(Tipografia.CUERPO);
        JLabel lblContrasena = new JLabel("Contrasena:");
        lblContrasena.setFont(Tipografia.CUERPO);
        JButton btnIngresar = new JButton("Ingresar al sistema");
        btnIngresar.setBackground(ColoresBlaugrana.GRANATE);
        btnIngresar.setForeground(ColoresBlaugrana.BLANCO);
        btnIngresar.setFont(Tipografia.CUERPO_NEGRITA);
        btnIngresar.setFocusPainted(false);
        btnIngresar.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        panelFormulario.add(lblUsuario, gbc);
        gbc.gridx = 1;
        panelFormulario.add(txtUsuario, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        panelFormulario.add(lblContrasena, gbc);
        gbc.gridx = 1;
        panelFormulario.add(txtContrasena, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.insets = new Insets(20, 8, 8, 8);
        panelFormulario.add(btnIngresar, gbc);

        JLabel lblAyuda = new JLabel("Usuario de demostracion: admin / admin123", SwingConstants.CENTER);
        lblAyuda.setFont(Tipografia.NOTA);
        lblAyuda.setForeground(ColoresBlaugrana.GRIS_TEXTO_SUAVE);
        gbc.gridy = 3;
        gbc.insets = new Insets(0, 8, 0, 8);
        panelFormulario.add(lblAyuda, gbc);

        JLabel pie = new JLabel("Proyecto academico - Universidad Don Bosco", SwingConstants.CENTER);
        pie.setFont(Tipografia.NOTA);
        pie.setForeground(ColoresBlaugrana.GRIS_CLARO);
        pie.setBorder(BorderFactory.createEmptyBorder(5, 5, 15, 5));

        panelRaiz.add(panelSuperior, BorderLayout.NORTH);
        panelRaiz.add(panelFormulario, BorderLayout.CENTER);
        panelRaiz.add(pie, BorderLayout.SOUTH);

        btnIngresar.addActionListener(e -> intentarIngreso());
        txtContrasena.addActionListener(e -> intentarIngreso());

        setContentPane(panelRaiz);
    }

    private void intentarIngreso() {
        String usuario = txtUsuario.getText().trim();
        String contrasena = new String(txtContrasena.getPassword());

        if (usuario.isEmpty() || contrasena.isEmpty()) {
            Mensajes.error(this, "Debe ingresar usuario y contrasena.");
            return;
        }

        try {
            Optional<Usuario> resultado = loginService.autenticar(usuario, contrasena);
            if (resultado.isEmpty()) {
                Mensajes.error(this, "Usuario o contrasena incorrectos.");
                return;
            }
            SesionUsuario.iniciar(resultado.get());
            new FrmPrincipal().setVisible(true);
            dispose();
        } catch (SQLException ex) {
            Mensajes.error(this, "No fue posible conectar con la base de datos", ex);
        }
    }
}
