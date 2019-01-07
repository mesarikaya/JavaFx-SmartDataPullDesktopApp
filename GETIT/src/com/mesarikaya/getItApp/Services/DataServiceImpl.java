package com.mesarikaya.getItApp.Services;

import com.cloudera.impala.jdbc41.DataSource;
import com.mesarikaya.getItApp.DataModel.TableData;
import javafx.concurrent.Task;
import javafx.geometry.Pos;
import javafx.util.Duration;
import org.controlsfx.control.Notifications;
import org.controlsfx.dialog.ProgressDialog;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class DataServiceImpl implements DataService {

    @Override
    public Connection setConnection(String connURL) throws Exception{
        Connection connection = null;
        // Initiate the connection
        try {
            // Enable the driver
            // Class.forName("com.cloudera.impala.jdbc41.Driver");

            // Show user the loading notification
            this.showLoadingNotification();

            // Connect to the datasource
            DataSource ds = new com.cloudera.impala.jdbc41.DataSource();
            ds.setURL(connURL);
            connection = ds.getConnection();

        }catch (Exception e){
            this.showCredentialsErrorNotification(e.getMessage());
            // e.printStackTrace();
        }

        return connection;
    }

    @Override
    public TableData setAvailableTables() {
        // LOAD THE AVAILABLE TABLE LIST DATA
        TableData tableData = new TableData();
        tableData.loadTables();

        return tableData;
    }

    @Override
    public List<String> getAvailableColumnsInTable(Connection connection, String table) throws Exception {

        List<String> result= new ArrayList<>();

        try{
            Statement stmt = connection.createStatement();
            String sql = "DESCRIBE " + table + ""; //dev_internal_ctl_container.booking_data_kudu";
            // System.out.println("Running: " + sql);
            ResultSet res = stmt.executeQuery(sql);
            while (res.next()) {
                result.add(res.getString(1));
            }
        }catch(Exception exc){
            exc.printStackTrace();
        }

        return result;
    }

    @Override
    public void setQuery() {

    }

    @Override
    public List<String> executeQuery(Connection connection, String columns, String filterWhereClause, String table)
            throws Exception{
        boolean approveRequest = this.confirmDownloadRequest();

        List<String> result= new ArrayList<>();

        if (approveRequest==true){
            Task copyWorkerReader = this.createReadWorker(result, connection, columns, filterWhereClause, table);
            this.handleProgressBarWorker(copyWorkerReader, "read");

            if(copyWorkerReader.getMessage().startsWith("Error:")){
                showMessage("Details: " + copyWorkerReader.getMessage());
            }
        }

        return result;
    }

    @Override
    public void writeToExcel(List<String> data, String location, Task copyWorker) throws Exception{
        // WRITE THE CONTENT TO
        // First create a worker to track the progress
        copyWorker = this.createWriteWorker(data, location);
        if (data.size()>0){
            this.handleProgressBarWorker(copyWorker, "write");
        }
    }

    @Override
    public void showLoadingNotification(){
        Notifications.create()
                .title("Establishing connection to database!")
                .text("Checking the credentials. This process takes about 5 seconds!")
                .position(Pos.CENTER)
                .hideAfter(Duration.seconds(5))
                .darkStyle()
                .show();
    }

    @Override
    public void showPreparingData(){
        Notifications.create()
                .title("Establishing connection to database to write data!")
                .text("Waiting for data retrieval from the database BEFORE start of writing")
                .position(Pos.CENTER)
                .hideAfter(Duration.seconds(10))
                .darkStyle()
                .show();
    }

    @Override
    public void showCredentialsErrorNotification(String errorMessage){
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Unsuccessful Login" );
        alert.setHeaderText("Credentials rejected by server!");
        alert.setContentText("Error Details: " + errorMessage);

        alert.showAndWait();
    }

    @Override
    public void showMessage(String errorMessage){
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Issue" );
        alert.getDialogPane().setMinHeight(400);
        alert.setHeaderText("Issue with request");
        alert.setContentText(errorMessage);
        alert.showAndWait();
    }

    @Override
    public boolean confirmDownloadRequest(){
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setHeaderText("Would you like to start the saving process?");
        alert.setHeight(200);
        alert.setContentText("Please note this process can take several minutes" +
                " and duration vary depending on the data size of the request!");

        Optional<ButtonType> result = alert.showAndWait();
        if(result.isPresent() && result.get() == ButtonType.OK){
            return true;
        }else{
            return false;
        }
    }

    public void showSaveSuccessful(){
        Notifications.create()
                .title("SUCCESS on File save")
                .text("File is saved successfully!")
                .position(Pos.CENTER)
                .hideAfter(Duration.seconds(5))
                .darkStyle()
                .show();
    }

    private void handleProgressBarWorker(Task copyWorker, String actionType){
        ProgressDialog dialog = new ProgressDialog(copyWorker);
        dialog.setTitle("Extracting Data");
        if (actionType == "read"){
            dialog.setHeaderText("Reading data...");
        }else{
            dialog.setHeaderText("Saving data...");
        }
        ButtonType buttonTypeCancel = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
        dialog.getDialogPane().getButtonTypes().add(buttonTypeCancel);

        Thread th = new Thread(copyWorker);
        th.start();

        dialog.setOnCloseRequest(event -> {
            th.interrupt();
        });

        dialog.showAndWait();

    }

    private Task createReadWorker(List<String> result, Connection connection, String columns, String filterWhereClause, String table) {
        return new Task(){
            @Override
            protected Object call() throws Exception {
                String sqlSuffix = "";

                if(!filterWhereClause.equals("") || filterWhereClause != null){
                    sqlSuffix += " " + filterWhereClause;
                }

                try{
                    //this.showPreparingData();
                    Statement stmt = connection.createStatement();
                    String sql = "SELECT " +  columns + " FROM " + table + sqlSuffix;
                    System.out.println("Running: " + sql);
                    connection.setAutoCommit(true);
                    ResultSet res = stmt.executeQuery(sql);

                    // Get the data size
                    int dataSize = getDataSize(sqlSuffix);

                    // Add the column names
                    result.add(columns + "," + "\n");

                    int numCols = res.getMetaData().getColumnCount();
                    int iterations = 0;
                    int max = dataSize*numCols;
                    while (res.next()) {
                        StringBuilder sb = new StringBuilder();
                        // String.format(String.valueOf(
                        for (int i = 1; i <= numCols; i++) {
                            sb.append(res.getString(i) + ",");
                            if(i==numCols){
                                sb.append("\n");
                            }

                            iterations++;
                            if (iterations%1000==0){
                                Thread.sleep(1);
                            }

                            updateProgress(iterations, max);
                            updateMessage((int)((double) iterations/(max)*100) + "% of data has been read");
                        }
                        result.add(sb.toString());
                    }
                }catch(SQLException sqlExec){
                    updateMessage("Error:" + sqlExec.getMessage());
                }catch(Exception exc){
                    //exc.printStackTrace();
                    //showMessage("Error in filter parameters data type match with actual database data types.\n" +"Error details:" + sqlExec )
                    updateMessage("Error:" +exc.getMessage());
                }

                return true;
            }

            private int getDataSize(String sqlSuffix) {
                int dataSize= 0;
                try{
                    Statement stmt2 = connection.createStatement();
                    String sql2 = "SELECT COUNT(*) as RECORDCOUNT FROM (SELECT " +  columns + " FROM " + table + sqlSuffix + ") source"; //dev_internal_ctl_container.booking_data_kudu";
                    // System.out.println(" Data size sql is: " + sql2);
                    ResultSet res2 = stmt2.executeQuery(sql2);
                    if (res2.next()) {
                        dataSize = res2.getInt("RECORDCOUNT");
                        // System.out.println("Record count: "+dataSize);
                    }
                }catch(SQLException sqlExec){
                    updateMessage("Error:" + sqlExec.getMessage());
                }
                catch(Exception exc){
                    updateMessage("Error:" +exc.getMessage());
                }

                return dataSize;
            }
        };
    }

    private Task createWriteWorker(List<String> data, String location) {
        return new Task(){
            @Override
            protected Object call() throws Exception {
                //System.out.println("File Location is: " + location);
                Writer writer = null;
                try {
                    File file = new File(location);
                    writer = new BufferedWriter(new FileWriter(file));
                    int iterations=0;
                    for (String text : data) {
                        if (iterations%100==0){
                            Thread.sleep(1);
                        }

                        writer.write(text);
                        iterations ++;
                        updateProgress(iterations, data.size());
                        updateMessage(iterations + " items is processed" + " within " + data.size());
                    }
                    System.out.println("File is successfully saved");
                    //showSaveSuccessful();
                }catch (Exception ex) {
                    ex.printStackTrace();
                }
                finally {
                    writer.flush();
                    writer.close();
                }
                return true;
            }
        };
    }

}
