import org.eclipse.jetty.websocket.api.*;
import org.json.*;
import spark.Spark;

import java.io.File;
import java.text.*;
import java.util.*;
import static j2html.TagCreator.*;
import static spark.Spark.*;

public class Chat {

    static Map<Session, String> userUsernameMap = new HashMap<>();
    static int nextUserNumber = 1; //Assign to username for next connecting user

    public static void main(String[] args) {
        new Chat().run();
    }

    //Sends a message from one user to all users, along with a list of current usernames
    static void broadcastMessage(String sender, String message) {
        userUsernameMap.keySet().stream().filter(Session::isOpen).forEach(session -> {
            try {
                session.getRemote().sendString(String.valueOf(new JSONObject()
                        .put("userMessage", createHtmlMessageFromSender(sender, message))
                        .put("userName", userUsernameMap.get(session))
                        .put("userlist", userUsernameMap.values())
                ));
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    private void run() {
        port(determinePort());
        if(System.getProperty("secure")!=null || System.getenv("secure")!=null) {
            DummySecurity sec=DummySecurity.instance();
            if(sec!=null) {
                System.out.println("Running secure");
                secure(sec.keystoreFile(), sec.keystorePassword(), sec.truststoreFile(), sec.truststorePassword());
            }
        }
        staticFileLocation("public"); //index.html is served at localhost:4567 (default port)
        webSocket("/chat", ChatWebSocketHandler.class);
        webSocketIdleTimeoutMillis(3600*1000);
  	
        //https://groups.google.com/forum/#!topic/sparkjava/GoOR5o0LllQ
        Spark.before((request, response) -> {
            response.header("Access-Control-Allow-Origin", "*");
        });

        Spark.options("/*", (request, response) -> {
            String accessControlRequestHeaders = request.headers("Access-Control-Request-Headers");
            if (accessControlRequestHeaders != null) {
                response.header("Access-Control-Allow-Headers", accessControlRequestHeaders);
            }

            String accessControlRequestMethod = request.headers("Access-Control-Request-Method");
            if (accessControlRequestMethod != null) {
                response.header("Access-Control-Allow-Methods", accessControlRequestMethod);
            }
            return "OK";
        });

        init();
    }

    //Builds a HTML element with a sender-name, a message, and a timestamp,
    private static String createHtmlMessageFromSender(String sender, String message) {
        return article().with(
                b(sender + " says:"),
                p(message),
                span().withClass("timestamp").withText(new SimpleDateFormat("HH:mm:ss").format(new Date()))
        ).render();
    }

    private int determinePort() {
        ProcessBuilder processBuilder = new ProcessBuilder();
        System.out.println(processBuilder.environment().entrySet());
        if (processBuilder.environment().get("PORT") != null) { //Heroku
            return Integer.parseInt(processBuilder.environment().get("PORT"));
        } else if(new File("manifest.yml").exists() || new File("/var/www").exists() ) {
        		//ipAddress("127.0.0.1");
        		return 18080;
        }
        //return 4567; //return default port 4567 port isn't set (i.e. on localhost)
        return 4443; // return default port 4443 for cloudfoundry
    }
}
