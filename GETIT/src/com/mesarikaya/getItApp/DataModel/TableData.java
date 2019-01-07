package com.mesarikaya.getItApp.DataModel;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Alert;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import java.io.InputStream;
import java.io.InputStreamReader;


public class TableData {

    private InputStream TABLELIST_FILE;
    // =  "src/com/mesarikaya/project/sample/resources/tables.xml"
    private static final String TABLE = "table";
    private static final String NAME = "name";
    private static final String SYNONYM = "synonym";

    private ObservableList<Table> tables;

    public TableData() {


        tables = FXCollections.observableArrayList();

        try{
            TABLELIST_FILE = Thread.currentThread().getContextClassLoader().getResourceAsStream("com/mesarikaya/getItApp/resources/tables.xml");
        }catch (Exception e){
            // System.out.println("error");
        }
    }

    public ObservableList<Table> getTables() {
        return tables;
    }


    public void addTable(Table item) {
        tables.add(item);
    }

    public void loadTables() {
        try {
            // First, create a new XMLInputFactory
            XMLInputFactory inputFactory = XMLInputFactory.newInstance();

            // Setup a new eventReader
            if(TABLELIST_FILE != null){
                //InputStream in = new FileInputStream(TABLELIST_FILE);
                XMLEventReader eventReader = inputFactory.createXMLEventReader(new InputStreamReader(TABLELIST_FILE));

                // read the XML document
                Table table = null;

                while (eventReader.hasNext()) {
                    XMLEvent event = eventReader.nextEvent();
                    if (event.isStartElement()) {
                        StartElement startElement = event.asStartElement();

                        // If we have a table item, we create a new table
                        if (startElement.getName().getLocalPart().equals(TABLE)) {
                            table = new Table();
                            continue;
                        }

                        // Get the table name
                        if (event.isStartElement()) {
                            if (event.asStartElement().getName().getLocalPart()
                                    .equals(NAME)) {
                                event = eventReader.nextEvent();
                                table.setTableName(event.asCharacters().getData());
                                continue;
                            }
                        }

                        // Get the table synonym name
                        if (event.asStartElement().getName().getLocalPart()
                                .equals(SYNONYM)) {
                            event = eventReader.nextEvent();
                            table.setSynonym(event.asCharacters().getData());
                            continue;
                        }
                    }

                    // If we reach the end of a table element, we add it to the list
                    if (event.isEndElement()) {
                        EndElement endElement = event.asEndElement();
                        if (endElement.getName().getLocalPart().equals(TABLE)) {
                            // System.out.println("Adding table name: " + table.getTableName() + " Synonym: " + table.getSynonym());
                            tables.add(table);
                            continue;
                        }
                    }
                }
            }
            else{
                System.out.println("Table file is: " + TABLELIST_FILE);
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("input file isssue again" );
                alert.setHeaderText("input file isssue again!");
                alert.setContentText("Error Details: " + TABLELIST_FILE);

                alert.showAndWait();
            }

        }
        catch (XMLStreamException e) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("input file isssue again" );
            alert.setHeaderText("input file isssue again!");
            alert.setContentText("Error Details: " + e);

            alert.showAndWait();
            e.printStackTrace();
        }

    }


}
