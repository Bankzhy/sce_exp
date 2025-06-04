package net.codeoasis.sce_exp;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.project.Project;
import org.apache.http.client.utils.URIBuilder;

import javax.swing.*;
import javax.swing.event.TableModelEvent;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class LCPluginPanel extends JPanel {
    private final Project project;
    private JTable table;
    private DefaultTableModel tableModel;
    private JButton refreshButton;
    private JButton postButton;

    private final String[] columnNames = {"Project", "ClassName", "Label", "Extract Methods","Path"};
    private ArrayList<LCDataItem> dataItems = new ArrayList<>();


    public LCPluginPanel(Project project) {
        this.project = project;
        setLayout(new BorderLayout());

        // Table setup
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                // Only allow editing for "Label" column
//                return getColumnName(column).equals("Label");
                if (getColumnName(column).equals("Label") || getColumnName(column).equals("Extract Methods")) {
                    return true;
                } else {
                    return false;
                }
            }
        };
        table = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(table);
        table.setCellSelectionEnabled(true); // allow cell-level selection

        table.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {

                int row = table.rowAtPoint(e.getPoint());
                int col = table.columnAtPoint(e.getPoint());

                if (e.getClickCount() == 2 && row != -1) {
                    if (row >= 0) {
                        // Get the DataItem for the clicked row
                        LCDataItem clickedItem = dataItems.get(row);

                        // Example: show a message dialog

                        System.out.println(
                                "Clicked Row:\n" +
                                        "Project: " + clickedItem.project + "\n" +
                                        "Class: " + clickedItem.className + "\n" +
                                        "Label: " + clickedItem.label);

                        // You can also trigger any custom logic here
                    }
                }
            }
        });

        tableModel.addTableModelListener(e -> {
            if (e.getType() == TableModelEvent.UPDATE) {
                int row = e.getFirstRow();
                int column = e.getColumn();
                Object newValue = tableModel.getValueAt(row, column);
                System.out.printf("Cell changed at row %d, column %d: %s%n", row, column, newValue);

                if (column == 2) {
                    dataItems.get(row).label = Integer.parseInt(newValue.toString());
                } else if (column == 3) {
                    dataItems.get(row).extractMethods = newValue.toString();
                }
            }
        });


        // Button
        refreshButton = new JButton("Refresh");
        postButton = new JButton("Post");

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.add(refreshButton);
        buttonPanel.add(postButton);

        // Add components
        add(scrollPane, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);

        // Load initial data
        refreshDataList();

        // Button action
        refreshButton.addActionListener((ActionEvent e) -> refreshDataList());
        postButton.addActionListener((ActionEvent e) -> updateItem());
    }
    private void showNotification(String content) {
        Notification notification = new Notification(
                "Snippet Save Notification",  // Notification Group ID (can be used to categorize notifications)
                "SCE",               // Title of the notification
                content,                      // Content of the notification
                NotificationType.INFORMATION  // Type of notification (e.g., INFORMATION, WARNING, ERROR)
        );
        Notifications.Bus.notify(notification);
    }


    public void updateItem() {
        ArrayList<Map<String, Object>> updateList = new ArrayList();
        Map<String, Object> postData = new HashMap<>();
        for (int i=0; i<dataItems.size(); i++) {

            if (dataItems.get(i).label != 9) {
                Map<String, Object> update = new HashMap();
                update.put("lc_id", dataItems.get(i).lcId);
                update.put("label", dataItems.get(i).label);
                update.put("extract_methods", dataItems.get(i).extractMethods);
                updateList.add(update);
            }
        }
        postData.put("result", updateList);
        try {
            Gson gson = new Gson();
            String json = gson.toJson(postData);
            HttpClient client = HttpClient.newHttpClient();
            System.out.println(json);

            // Build the HTTP POST request
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("http://192.168.11.5:8000/api/lc/"))  // Replace with your server's URL
                    .header("Content-Type", "application/json")  // Set Content-Type header
                    .header("Authorization", "Bearer " + LoginManager.getAccess())
                    .POST(HttpRequest.BodyPublishers.ofString(json))       // Attach the request body
                    .build();

            HttpResponse response = client.send(request, HttpResponse.BodyHandlers.ofString());
            System.out.println(response.statusCode());
            if (response.statusCode() == 201) {
                Map<String, Object> result = gson.fromJson(response.body().toString(), Map.class);
                System.out.println(result);
                showNotification("Sample Saved !");
                refreshDataList();
            } else if (response.statusCode() == 401) {
                OasisActivator.showUnLoginNotification();
            } else {
                showNotification("Save failed! The error code:" + response.statusCode());
            }
        } catch (Exception xe) {
            System.out.println(xe);
        }
    }

    public void refreshDataList() {
        try {

            String url = "http://192.168.11.5:8000/api/lc/";
            URI uri = new URIBuilder(url)
                    .addParameter("project", project.getName())
                    .build();

            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(uri)
                    .header("Content-Type", "application/json")  // Set Content-Type header
                    .header("Authorization", "Bearer " + LoginManager.getAccess())
                    .GET()       // Attach the request body
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            // Parse JSON array
            String json = response.body();
            Gson gson = new Gson();

            if (response.statusCode() == 200) {
                dataItems = gson.fromJson(json, new TypeToken<ArrayList<LCDataItem>>() {}.getType());
            } else if(response.statusCode() == 401) {
                OasisActivator.showUnLoginNotification();
            } else {
                showNotification("Fetch data failed! The error code:" + response.statusCode());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        tableModel.setRowCount(0); // clear existing rows
        for (LCDataItem item : dataItems) {
            tableModel.addRow(item.toTableRow());
        }
    }
}
