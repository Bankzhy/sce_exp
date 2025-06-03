package net.codeoasis.sce_exp;

import com.google.gson.Gson;
import com.intellij.notification.*;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.Map;

public class OasisActivator {
    private OasisActivator() {
        init();
    }
    private void init() {

    }

    public static void doOKAction(String username, String password) {

        // Call the login method from LoginManager
        try {
            // Create an HttpClient instance
            HttpClient client = HttpClient.newHttpClient();

            // Prepare the data (URL encoding the snippet to be safely transmitted)
            Map<String, String> postData = new HashMap<>();
            postData.put("username", username);
            postData.put("password", password);
            Gson gson = new Gson();
            String json = gson.toJson(postData);

            // Build the HTTP POST request
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("http://www.codeoasis.net:8005/clogin/"))  // Replace with your server's URL
                    .header("Content-Type", "application/json")  // Set Content-Type header
                    .POST(HttpRequest.BodyPublishers.ofString(json))       // Attach the request body
                    .build();

            // Send the request asynchronously
            HttpResponse response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200) {
                Map<String, Object> result = gson.fromJson(response.body().toString(), Map.class);
//                System.out.println(String.valueOf((int)Math.round(result.get("user_id"))));
                double userId = (double)result.get("user_id");
                int userIDInt = (int)userId;
                String userIDStr = String.valueOf(userIDInt);

                LoginManager.saveUserDataLocally(
                        result.get("username").toString(),
                        result.get("access").toString(),
                        userIDStr,
                        result.get("refresh").toString()
                );
//                showNotification("Login successfully!");
                notifyError(getCurrentProject(), "Login successfully!");
            } else {
//                showNotification("login failed! The error code:" + response.statusCode());
                notifyError(getCurrentProject(), "Login failed! The error code:" + response.statusCode());
            }



        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void notifyError(Project project, String content) {
        NotificationGroupManager.getInstance()
                .getNotificationGroup("Snippet Save Notification")
                .createNotification("SCE", content, NotificationType.INFORMATION)
                .notify(project);
    }

    public static Project getCurrentProject() {
        Project[] openProjects = ProjectManager.getInstance().getOpenProjects();
        if (openProjects.length > 0) {
            return openProjects[0]; // Returns the first open project
        }
        return null;
    }

    public static void showUnLoginNotification() {
        Notification notification = new Notification(
                "Snippet Save Notification",  // Notification Group ID (can be used to categorize notifications)
                "SCE",               // Title of the notification
                "Your account is not login to the SCE.",                      // Content of the notification
                NotificationType.INFORMATION  // Type of notification (e.g., INFORMATION, WARNING, ERROR)
        );

        notification.addAction(new NotificationAction("Login") {
            @Override
            public void actionPerformed(AnActionEvent e, Notification notification) {
                notification.expire(); // Optionally close the notification
                LoginDialog loginDialog = new LoginDialog();
                if (loginDialog.showAndGet()) {  // If the user clicked "OK"
                    String username = loginDialog.getUsername();
                    String password = loginDialog.getPassword();
                    doOKAction(username, password);
                } else {
                    showNotification("Login canceled!");
                    return;  // Exit without sending the snippet
                }
            }
        });
        Notifications.Bus.notify(notification);
    }

    public static void showNotification(String content) {
//        Notification notification = new Notification(
//                "Snippet Save Notification",  // Notification Group ID (can be used to categorize notifications)
//                "SCE",               // Title of the notification
//                content,                      // Content of the notification
//                NotificationType.INFORMATION  // Type of notification (e.g., INFORMATION, WARNING, ERROR)
//        );
//        Notifications.Bus.notify(notification);
        NotificationGroupManager.getInstance()
                .getNotificationGroup("Snippet Save Notification")
                .createNotification("SCE", content, NotificationType.INFORMATION)
                .notify(getCurrentProject());
    }
}
