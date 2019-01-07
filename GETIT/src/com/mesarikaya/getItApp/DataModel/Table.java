package com.mesarikaya.getItApp.DataModel;


public class Table {

    private String tableName;
    private String data;
    private String synonym;

    public Table(){}

    public Table(String tableName, String data) {
        this.tableName = tableName;
        this.data = data;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public String getSynonym() {
        return synonym;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public void setSynonym(String synonym) {
        this.synonym = synonym;
    }

    @Override
    public String toString() {
        return "Table{" +
                "tableName='" + tableName + '\'' +
                ", data='" + data + '\'' +
                ", synonym='" + synonym + '\'' +
                '}';
    }
}
