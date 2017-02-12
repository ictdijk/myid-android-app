package in.yagnyam.myid;

import java.io.BufferedReader;
import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import lombok.extern.slf4j.Slf4j;

import static com.googlecode.objectify.ObjectifyService.ofy;

@Slf4j
public class LoginServlet extends HttpServlet {

    static {
        StaticRegistrar.register();
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String session = req.getParameter("session");
        log.info("doPost({})", session);
        /*
        String[] tokens = req.getRequestURI().split("/");
        String session = tokens[tokens.length-1];
        */
        StringBuilder buffer = new StringBuilder();
        BufferedReader reader = req.getReader();
        String line;
        while ((line = reader.readLine()) != null) {
            buffer.append(line);
        }
        String data = buffer.toString();
        log.info("LoginServlet.doPost => {} {}", session, data);
        ofy().save().entity(LoginEntity.of(session, data)).now();
    }

}
