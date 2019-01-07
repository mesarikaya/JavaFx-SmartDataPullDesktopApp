package com.mesarikaya.getItApp.Services;

import com.mesarikaya.getItApp.DataModel.TableData;
import javafx.concurrent.Task;

import java.util.List;

public interface DataService {

    public Connection setConnection(String connURL) throws Exception;
    public void setQuery();
    public List<String> executeQuery(Connection connection, String columns, String filterWhereClause, String table) throws Exception;
    public void writeToExcel(List<String> data, String location, Task copyWorker) throws Exception;
    public TableData setAvailableTables();
    public List<String> getAvailableColumnsInTable(Connection connection, String table) throws Exception;
    public void showLoadingNotification();
    public void showCredentialsErrorNotification(String showMessage);
    public void showSaveSuccessful();
    public void showPreparingData();
    public boolean confirmDownloadRequest();
    public void showMessage(String errorMessage);
}
