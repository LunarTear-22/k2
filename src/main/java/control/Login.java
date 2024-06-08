package control;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import model.DriverManagerConnectionPool;
import model.OrderModel;
import model.UserBean;

/**
 * Servlet implementation class Login
 */
@WebServlet("/Login")
public class Login extends HttpServlet {
    private static final long serialVersionUID = 1L;

    /**
     * @see HttpServlet#HttpServlet()
     */
    public Login() {
        super();
        // TODO Auto-generated constructor stub
    }

    /**
     * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
     */
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // TODO Auto-generated method stub
        doPost(request, response);
    }

    /**
     * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
     */
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String email = request.getParameter("j_email");
        String password = request.getParameter("j_password");
        String redirectedPage = "/loginPage.jsp";
        Boolean control = false;
        try {
            Connection con = DriverManagerConnectionPool.getConnection();
            String sql = "SELECT email, passwordUser, ruolo, nome, cognome, indirizzo, telefono, numero, intestatario, CVV FROM UserAccount WHERE email = ?";
            PreparedStatement statement = con.prepareStatement(sql);
            statement.setString(1, email);
            ResultSet rs = statement.executeQuery();

            while (rs.next()) {
                String hashedPassword = hashPassword(password); // Hashing the entered password
                if (hashedPassword.equals(rs.getString("passwordUser"))) {
                    control = true;
                    UserBean registeredUser = new UserBean();
                    registeredUser.setEmail(rs.getString("email"));
                    registeredUser.setNome(rs.getString("nome"));
                    registeredUser.setCognome(rs.getString("cognome"));
                    registeredUser.setIndirizzo(rs.getString("indirizzo"));
                    registeredUser.setTelefono(rs.getString("telefono"));
                    registeredUser.setNumero(rs.getString("numero"));
                    registeredUser.setIntestatario(rs.getString("intestatario"));
                    registeredUser.setCvv(rs.getString("CVV"));
                    registeredUser.setRole(rs.getString("ruolo"));
                    request.getSession().setAttribute("registeredUser", registeredUser);
                    request.getSession().setAttribute("role", registeredUser.getRole());
                    request.getSession().setAttribute("email", rs.getString("email"));
                    request.getSession().setAttribute("nome", rs.getString("indirizzo"));

                    OrderModel model = new OrderModel();
                    request.getSession().setAttribute("listaOrdini", model.getOrders(rs.getString("email")));

                    redirectedPage = "/index.jsp";
                    DriverManagerConnectionPool.releaseConnection(con);
                }
            }
        } catch (Exception e) {
            redirectedPage = "/loginPage.jsp";
        }

        if (!control) {
            request.getSession().setAttribute("login-error", true);
        } else {
            request.getSession().setAttribute("login-error", false);
        }
        response.sendRedirect(request.getContextPath() + redirectedPage);
    }

    private String hashPassword(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hashedBytes = md.digest(password.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : hashedBytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        }
    }
}
