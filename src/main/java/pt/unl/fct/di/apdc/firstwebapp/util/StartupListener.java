package pt.unl.fct.di.apdc.firstwebapp.util;

import com.google.cloud.Timestamp;
import com.google.cloud.datastore.*;

import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;

import org.apache.commons.codec.digest.DigestUtils;

@WebListener
public class StartupListener implements ServletContextListener {

    private final Datastore datastore = DatastoreOptions.getDefaultInstance().getService();
    private final KeyFactory userKeyFactory = datastore.newKeyFactory().setKind("User");

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        String defaultUsername = "admin";
        String defaultPassword = "admin123"; // Should be stronger in real apps

        Key adminKey = userKeyFactory.newKey(defaultUsername);
        Entity user = datastore.get(adminKey);

        if (user == null) {
            Entity adminUser = Entity.newBuilder(adminKey)
                    .set("user_pwd", DigestUtils.sha512Hex(defaultPassword))
                    .set("user_role", "ADMIN")
                    .set("user_creation_time", Timestamp.now())
                    .build();

            datastore.put(adminUser);
            System.out.println("Default admin user created.");
        } else {
            System.out.println("Admin user already exists.");
        }
    }

   /** @Override
    public void contextDestroyed(ServletContextEvent sce) {
        // Cleanup if needed
    }*/
}
