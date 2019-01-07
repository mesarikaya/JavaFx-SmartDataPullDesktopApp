package com.mesarikaya.getItApp.Controllers;

import com.mesarikaya.getItApp.DataModel.Table;
import com.mesarikaya.getItApp.DataModel.TableData;
import com.mesarikaya.getItApp.Services.DataService;
import com.mesarikaya.getItApp.Services.DataServiceImpl;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Paint;
import javafx.scene.text.Font;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import org.controlsfx.control.CheckComboBox;

import java.io.File;
import java.sql.Connection;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

public class UserPageController {

    private Scene firstScene;

    // Get Data Service
    private DataService dataService;

    // Set the connected driver connection
    private Connection conn;

    // Table List
    private TableData tableData;

    @FXML
    private ComboBox<String> tableList;

    @FXML
    private VBox vBox;

    @FXML
    private GridPane userPageView;

    private HashMap<String,String> synonymNameMatch = new HashMap<>();

    private ObservableList<String> tableColumns;

    @FXML
    private CheckComboBox<String> checkComboBox;

    @FXML
    private CheckBox selectAllCheckBox;

    @FXML
    private Button helpButton;

    @FXML
    private HBox filterConditionsHBOX;

    @FXML
    private ComboBox<String> filterColumn;

    @FXML
    private ComboBox<String> filterType;

    @FXML
    private Button closeButton;

    private Task copyWorker;

    // Create a VBox for further use after filter type selections
    private final VBox filterColumnVBox = new VBox();
    // Create an HBox to put the new search condition parameters
    private final HBox searchConditionHBox = new HBox();

    // create the parameter to filter a certain column
    private String filterParameter1;
    private String filterParameter2;

    // initialize the User page
    public void initialize(){
        // Set default Tables for the table selection filter
        setTableData();

        // Set the Combo box content
        setComboBoxContent();

        // Create the event Listener for table selection
        tableList.getSelectionModel().selectedItemProperty()
                .addListener(new ChangeListener<String>() {
                    public void changed(ObservableValue<? extends String> observable,
                                        String oldValue, String newValue) {
                        createListViewWithStringAndCheckBox(conn, newValue);
                    }
                });

        // SET the DEFAULT VALUES AND STYLE PARAMETERS
        // Add Select All CheckBox
        selectAllCheckBox.setPrefHeight(25);
        selectAllCheckBox.setPrefWidth(100);
        selectAllCheckBox.setSelected(true);

        // Add checkComboBox
        checkComboBox.setPrefHeight(25);
        checkComboBox.setPrefWidth(200);

        // Set the filter column default
        filterColumn.setValue("Column");
        filterColumn.setVisibleRowCount(5);

        // Set the filter data type default options
        ObservableList<String> dataTypes = FXCollections.observableArrayList(Arrays.asList(new String[]{"Date", "Number", "Text"}));
        filterType.setItems(dataTypes);

        // Add event Listener to the Data Type Selection
        filterType.getSelectionModel().selectedItemProperty()
                .addListener(new ChangeListener<String>() {
                    @Override
                    public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                        // If the value has changed, check the type and populate the screen with relevant items
                        createSearchFilterMenu(oldValue, newValue);
                    }
                });

        filterType.setValue("Data Type");
        filterType.setValue("Text");

    }

    // Convert the date to the provided format
    public LocalDate setLocalDate(LocalDate inputDate, String dateFormat){

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(dateFormat);
        return LocalDate.parse(formatter.format(inputDate));
    }

    // Create setters for dependency injection
    public void setFirstScene(Scene scene) {
        firstScene = scene;
    }

    // Method that opens the scene on call from the login page
    public void openFirstScene(ActionEvent actionEvent) {
        Stage primaryStage = (Stage)((Node)actionEvent.getSource()).getScene().getWindow();
        primaryStage.setScene(firstScene);
    }

    public DataService getDataService() {
        return dataService;
    }

    public void setDataService(DataService dataService) {
        this.dataService = dataService;
    }

    public Scene getFirstScene() {
        return firstScene;
    }

    public TableData getTableData() {
        return tableData;
    }

    public Connection getConn() {
        return conn;
    }

    public void setConn(Connection conn) {
        this.conn = conn;
    }

    public HashMap<String, String> getSynonymNameMatch() {
        return synonymNameMatch;
    }

    public void setSynonymNameMatch(HashMap<String, String> synonymNameMatch) {
        this.synonymNameMatch = synonymNameMatch;
    }

    // This sets the available table names for the further use in the combo box
    public void setTableData() {
        if(this.dataService==null){
            dataService = new DataServiceImpl();
            this.setDataService(dataService);
        }

        this.tableData = dataService.setAvailableTables();
    }

    // Set the Table selection combo box
    public void setComboBoxContent(){
        // System.out.println(" Inside the combo function");
        tableList.getItems().clear();

        for(Table t: tableData.getTables()){
            // System.out.println("****************Opening: " + t.getTableName());
            String synonym = t.getSynonym();
            String tableName = t.getTableName();
            tableList.getItems().add(t.getSynonym());
            synonymNameMatch.put(synonym,tableName);
        }
        this.setSynonymNameMatch(synonymNameMatch);
    }

    // Create the CheckComboBox box to enable filtering of available Table columns
    public void createListViewWithStringAndCheckBox(Connection conn, String tableName) {

        // Get the data Service or create one in case missing
        if(this.dataService==null){
            dataService = new DataServiceImpl();
            this.setDataService(dataService);
        }

        // Get the real Table name
        String realTableName = synonymNameMatch.get(tableName);

        // Get the available columns in that table
        try{
            tableColumns = FXCollections.observableArrayList(dataService.getAvailableColumnsInTable(conn, realTableName));
        }catch(Exception e){
            e.printStackTrace();
        }

        // Clear the combobox content and  vertical box that holds it and then repopulate
        checkComboBox.getItems().clear();
        checkComboBox.getItems().addAll(tableColumns);

        // By default, set all the items checked at the initiation if SELECT ALL is selected
        selectAllCheckBox.setAllowIndeterminate(false);
        selectAllCheckBox.setSelected(true);
        if (selectAllCheckBox.isSelected()){
            checkComboBox.getCheckModel().checkAll();
        }

        // Add event listener to update teh checkboxes ticks
        selectAllCheckBox.indeterminateProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                if (selectAllCheckBox.isIndeterminate()==false){
                    selectAllCheckBox.setSelected(false);
                    selectAllCheckBox.setSelected(false);
                }
            }
        });

        // Add event listener to the SELECT ALL field
        selectAllCheckBox.selectedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                if (selectAllCheckBox.isSelected()) {
                    selectAllCheckBox.setAllowIndeterminate(true);
                    checkComboBox.getCheckModel().checkAll();
                    selectAllCheckBox.setAllowIndeterminate(false);
                } else{
                    selectAllCheckBox.setAllowIndeterminate(true);
                    checkComboBox.getCheckModel().clearChecks();
                    selectAllCheckBox.setAllowIndeterminate(false);
                }
            }
        });

        // Add event listener to comboBox checkboxes
        checkComboBox.getCheckModel().getCheckedItems().addListener(new ListChangeListener<String>() {
            public void onChanged(ListChangeListener.Change<? extends String> c) {

                // If user clicks to any checkbox, then the default SELECT ALL is cancelled
                if(selectAllCheckBox.isAllowIndeterminate()==false & selectAllCheckBox.isIndeterminate()==false){
                    selectAllCheckBox.setAllowIndeterminate(true);
                    selectAllCheckBox.setIndeterminate(true);
                }
            }
        });

        //Set the available filter columns
        this.setFilterColumnData();
    }

    // This sets the available table names for the further use in the combo box
    public void setFilterColumnData() {

        ObservableList<String> filterColumns = FXCollections.observableArrayList(Arrays.asList(new String[]{"No Filter"}));
        filterColumns.addAll(tableColumns.stream().collect(Collectors.toList()));

        // Set the available filter columns
        filterColumn.setItems(filterColumns);

    }

    // Main Method that calls either create NumberorTextPArameterField or DatePicker Parameter field creation methods
    private void createSearchFilterMenu(String oldValue, String newValue) {
        if (oldValue != newValue){

            // First delete all child of the top component
            filterConditionsHBOX.getChildren().clear();

            // First delete all child elements
            filterColumnVBox.getChildren().clear();

            // Clean the HBox
            searchConditionHBox.getChildren().clear();

            // Create a Label
            Label filterInputLabel = new Label();
            filterInputLabel.setText("Enter Search Condition");
            filterInputLabel.setFont(Font.font(" Tahoma", 14));
            filterInputLabel.setTextFill(Paint.valueOf("#23d619"));
            Insets labelInset = new Insets(5, 0, 0, 0);
            filterColumnVBox.setMargin(filterInputLabel, labelInset);

            // If the item is not date, then set a search input field with a contain/ALL dropdown
            if(newValue != "Date"){
                createNumberOrTextParameterField(newValue, filterInputLabel);
            }else{// If the item is DATE, let the user select from a date period
                createDatePickerParameterFields();
            }
        }
    }

    private void createDatePickerParameterFields() {
        // Create a VBox for the date picker
        HBox dateConditionHBox = new HBox();

        // Create a VBox for the date picker
        VBox startDateConditionVBox = new VBox();

        // Create a VBox for the date picker
        VBox endDateConditionVBox = new VBox();

        // Create a start date filter
        DatePicker startDatePicker = new DatePicker();
        startDatePicker.setPrefHeight(25.0);
        startDatePicker.setPrefWidth(100);

        // Create a end date filter
        DatePicker endDatePicker = new DatePicker();
        endDatePicker.setPrefHeight(25.0);
        endDatePicker.setPrefWidth(100);

        // Add event Listener to the Format Type Selection
        filterType.getSelectionModel().selectedItemProperty()
                .addListener(new ChangeListener<String>() {
                    @Override
                    public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {

                        // Set the formatter for start date
                        startDatePicker.setValue( startDatePicker.getValue());

                        // Set the formatter for end date
                        endDatePicker.setValue( endDatePicker.getValue());
                    }
                });

        // Add all to the top level HBox
        searchConditionHBox.getChildren().addAll(dateConditionHBox);
        searchConditionHBox.setMargin(dateConditionHBox,new Insets(5, 5, 5, 0));

        // Add all to the Filter Column Box
        filterColumnVBox.getChildren().add(searchConditionHBox);

        // Add the final container to the top level HBox
        filterConditionsHBOX.getChildren()
                .addAll(filterColumnVBox);

        // Add styles to the labels and the date pickers
        // Create a Labels for Date Pickers
        // Set their margins
        Label startDateLabel = new Label();
        startDateLabel.setText("Start Date");
        startDateLabel.setFont(Font.font(" Tahoma", 14));
        startDateLabel.setTextFill(Paint.valueOf("#23d619"));
        Insets startLabelInset = new Insets(5, 0, 5, 0);
        startDateConditionVBox.setMargin(startDateLabel, startLabelInset);

        Label endDateLabel = new Label();
        endDateLabel.setText("End Date");
        endDateLabel.setFont(Font.font(" Tahoma", 14));
        endDateLabel.setTextFill(Paint.valueOf("#23d619"));
        Insets endLabelInset = new Insets(5, 0, 5, 0);
        endDateConditionVBox.setMargin(endDateLabel, startLabelInset);

        // Set margins for datepickers
        Insets startDatePickerInset = new Insets(0, 0, 0, 0);
        startDateConditionVBox.setMargin(startDatePicker, startDatePickerInset);
        Insets endDatePickerInset = new Insets(0, 0, 0, 0);
        endDateConditionVBox.setMargin(endDatePicker, endDatePickerInset);

        // Set margins for the left VBOX that holds the start date
        Insets startDateVBoxInset = new Insets(0, 5, 0, 0);
        Insets endDateVBoxInset = new Insets(0, 5, 0, 0);
        dateConditionHBox.setMargin(startDateConditionVBox, startDateVBoxInset);
        dateConditionHBox.setMargin(endDateConditionVBox, endDateVBoxInset);

        // Initiate the start and end dates
        startDatePicker.setValue(LocalDate.now().minusDays(90));
        endDatePicker.setValue(LocalDate.now());

        // Add the items to the VBox groups of datepicker's
        startDateConditionVBox.getChildren().addAll(startDateLabel, startDatePicker);
        endDateConditionVBox.getChildren().addAll(endDateLabel, endDatePicker);

        // Add items to the higher VBox for the date picking condition
        dateConditionHBox.getChildren().addAll(startDateConditionVBox, endDateConditionVBox);

        // Set margins for the box that holds the date pickers and labels
        Insets dateConditionBoxInset = new Insets(5, 5, 5, 0);
        searchConditionHBox.setMargin(dateConditionHBox, dateConditionBoxInset);
    }

    private void createNumberOrTextParameterField(String newValue, Label filterInputLabel) {

        // Create ComboBox for Search Type
        ComboBox<String> searchTypeComboBox = new ComboBox<>();

        // SEt the searchType combo box differently depends on the data type
        if(newValue == "Number"){
            // Set the filter data type default options
            ObservableList<String> searchTypes = FXCollections.observableArrayList(Arrays.asList(new String[]{"<=", ">=", "="}));
            searchTypeComboBox.setItems(searchTypes);
            searchTypeComboBox.setValue("=");
        }else{
            // Set the filter data type default options
            ObservableList<String> searchTypes = FXCollections.observableArrayList(Arrays.asList(new String[]{"contains", "equals"}));
            searchTypeComboBox.setItems(searchTypes);
            searchTypeComboBox.setValue("equals");
        }

        // Create the input field for search term entry
        TextField searchTerm = new TextField();
        searchTerm.setPromptText("Type search term");

        // Add the items to the HBox
        searchConditionHBox.getChildren()
                .addAll(searchTypeComboBox,searchTerm);

        // Add the HBOX and the label to the default VBox
        filterColumnVBox.getChildren()
                .addAll(filterInputLabel, searchConditionHBox);

        // Add the final container to the top level HBox
        filterConditionsHBOX.getChildren()
                .addAll(filterColumnVBox);

        // Set margins
        Insets comboBoxInset = new Insets(0, 0, 5, 0);
        filterConditionsHBOX.setMargin(searchTypeComboBox, comboBoxInset);

    }

    // Event Listener for Export data Click
    @FXML
    public void onExportDataClick(ActionEvent e){
        // Set the parameter to allow export or not
        Boolean isExecutable = true;

        // set boolean to see if the filter column is activated
        boolean isFilterActive = filterColumn.getValue()!="No Filter" && filterColumn.getValue()!="Column" &&
                !filterColumn.getValue().isEmpty() && filterColumn.getValue() != null;

        // filter where clause that needs to be shared with the data service to send the SQL
        String filterWhereClause = "";

        isExecutable = areFiltersCorrectlySetForExport(isExecutable, isFilterActive);

        if (isExecutable){
            // Create the where clause if and only if the the filter is active and no errors are found(isExecutable==true)
            if(isFilterActive){
                // Set the where clause
                if(filterType.getValue() == "Text"){
                    if(filterParameter1=="LIKE "){
                        filterWhereClause += "WHERE " + filterColumn.getValue() +
                                " " + filterParameter1 + "'%"+ filterParameter2+ "%'";
                    }else{
                        filterWhereClause += "WHERE " + filterColumn.getValue() +
                                " " + filterParameter1 + "'"+ filterParameter2 + "'";
                    }
                }else if(filterType.getValue() == "Number"){
                    filterWhereClause += "WHERE " + filterColumn.getValue() +
                            " " + filterParameter1 + " "+ filterParameter2;
                }else{
                    filterWhereClause += "WHERE " + filterColumn.getValue() +
                            ">=" + "'"+ filterParameter1 + "'" + " AND "+
                            filterColumn.getValue() + "<=" + "'"+ filterParameter2 + "'";
                }
            }

            // Open the Directory Choose
            DirectoryChooser chooser = new DirectoryChooser();
            File file = chooser.showDialog(userPageView.getScene().getWindow());

            // If a selection is made, get the selected columns to get the data and write the file
            if(file != null){
                // System.out.println(file.getPath());
                StringBuilder sb = new StringBuilder();
                List<String> checkedItems = checkComboBox.getCheckModel().getCheckedItems();

                for(int i=0; i<checkedItems.size(); i++){
                    // System.out.println("Checked items are: " + checkedItems.get(i) + "\n" );
                    if (tableColumns.indexOf(checkedItems.get(i))>0) {
                        // System.out.println("Item: " + checkedItems.get(i) + " is in");

                        if (checkedItems.get(i) != null && !checkedItems.get(i).isEmpty()) {
                            sb.append(checkedItems.get(i));
                           // if (i != checkedItems.size() - 1) {
                                sb.append(", ");
                            //}
                        }
                    }
                }
                // Remove the comma if the last item is comma
                if(sb.lastIndexOf(",")>=0){
                    sb.deleteCharAt(sb.lastIndexOf(","));
                }

                String selectedColumns = sb.toString();
                // System.out.println("Selected columns" + selectedColumns);

                // Set the file name to write the results to the selected folder
                String timeStamp = new SimpleDateFormat("MM_dd_YYYY_HHmmss").format(Calendar.getInstance().getTime());
                String fileLocation = file.getPath().toString() + "/" + tableList.getValue() + "_" + timeStamp + ".csv";

                // Send the query to the database
                String selectedTable = synonymNameMatch.get(tableList.getValue());
                List<String> data = null;
                try{
                    data = dataService.executeQuery(conn, selectedColumns, filterWhereClause, synonymNameMatch.get(tableList.getValue()));
                }catch(Exception exc){
                    exc.printStackTrace();
                }

                // Write the data to the provided file location
                if (data!=null || !data.isEmpty() || data.size()==0){
                    try{
                        dataService.writeToExcel(data, fileLocation, copyWorker);
                    }catch(Exception ex){
                        ex.printStackTrace();
                    }
                }else{
                    dataService.showMessage("NO DATA is found with the provided parameters.");
                }

            }else{
                System.out.println("Chooser was cancelled");
            }
        }

    }

    private Boolean areFiltersCorrectlySetForExport(Boolean isExecutable, Boolean isFilterActive) {

        Boolean result = isExecutable;

        // Set the filter parameters
        if (filterType.getValue() != "Date"){
            if(searchConditionHBox != null){
                // Get the Contains or equal setting
                ComboBox<String> textfield1 = (ComboBox<String>) searchConditionHBox.getChildren().get(0);
                filterParameter1 = textfield1.getValue();
                if (filterParameter1 == "contains"){
                    filterParameter1 = "LIKE ";
                }else if(filterParameter1 == "equals"){
                    filterParameter1 = "=";
                }

                // Get the text value
                TextField textfield2 = (TextField) searchConditionHBox.getChildren().get(1);
                filterParameter2 = textfield2.getText();

                // If the value is expected to be numeric field and the entered value is not then set to null
                // This null will later be used for user information prompts
                boolean isNumeric = filterParameter2.chars().allMatch( Character::isDigit );
                if (isNumeric == false && filterType.getValue() == "Number"){
                    filterParameter2 = null;
                }
            }
        }else{
            if(searchConditionHBox != null){

                HBox hBoxChild = (HBox) searchConditionHBox.getChildren().get(0);

                // Get the Contains or equal setting
                VBox dateFieldVBOX = (VBox) hBoxChild.getChildren().get(0);
                DatePicker datePickerField1 = (DatePicker) dateFieldVBOX.getChildren().get(1);
                filterParameter1 = datePickerField1.getValue().toString();

                // Get the date picker value
                VBox dateFieldVBOX2 = (VBox) hBoxChild.getChildren().get(1);
                DatePicker datePickerField2 = (DatePicker) dateFieldVBOX2.getChildren().get(1);
                filterParameter2 = datePickerField2.getValue().toString();
            }
        }

        if (filterParameter2 != null && !filterParameter2.isEmpty() && !filterParameter2.equals("")){
            if(isFilterActive == false){
                sendWrongFilterParameterError("Column to filter on is not entered.\n" +
                        "Please enter a column to proceed with the export");
                // Set the state as not executable export
                result = false;
            }
        }else if(filterParameter2 == null || filterParameter2.isEmpty() || filterParameter2.equals("")){
            if(isFilterActive == true){
                sendWrongFilterParameterError("Please enter the appropriate search parameters.\n" +
                        "Either no value entered or for a number field a non-numeric value is entered.");
                // Set the state as not executable export
                result = false;
            }
        }

        return result;
    }

    // Event Listener for Help Button
    @FXML
    public void onHelpButtonClick(ActionEvent e){

        if(e.getSource().equals(helpButton)){//Action: Show information  dialog on About Button Click
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("About GetIt");
            alert.setHeaderText("Created By: Ergin Sarikaya");
            alert.setWidth(100);
            alert.setContentText("This program enables data download. It allows\nthe user to filter and download the data that\nthey have access rights." +
                    "\n\n" + "Please contact with Ergin Sarikaya in case of help need.");
            alert.showAndWait();
        }
    }

    public void sendWrongFilterParameterError(String message){
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Wrong filter parameters given");
        alert.setHeaderText("Please review the detailed filtering parameters!");
        alert.setWidth(100);
        alert.setContentText(message.toString());
        alert.showAndWait();
    }

    // Close the App on Close(X) button click
    @FXML
    public void closeApp(ActionEvent e){
        closeButton.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent actionEvent) {
                Platform.exit();
            }
        });
    }

}
