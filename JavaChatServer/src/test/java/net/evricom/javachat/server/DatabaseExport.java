package net.evricom.javachat.server;

import org.dbunit.DatabaseUnitException;
import org.dbunit.database.DatabaseConfig;
import org.dbunit.database.DatabaseConnection;
import org.dbunit.database.IDatabaseConnection;
import org.dbunit.database.QueryDataSet;
import org.dbunit.database.search.TablesDependencyHelper;
import org.dbunit.dataset.DataSetException;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.xml.FlatDtdDataSet;
import org.dbunit.dataset.xml.FlatXmlDataSet;
import org.dbunit.util.search.SearchException;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.*;

/**
 * created by dima on 23.09.2019 22:13
 */
public class DatabaseExport {


    public static void main(String[] args) throws SQLException, DatabaseUnitException, IOException {

        Connection jdbcConnection = null;
        // database connection
        DriverManager.registerDriver(new org.sqlite.JDBC());
        jdbcConnection = DriverManager.getConnection("jdbc:sqlite:userDB.db");
        IDatabaseConnection iDatabaseConnection = new DatabaseConnection(jdbcConnection);
        // partial database export
//        QueryDataSet partialDataSet = new QueryDataSet(connection);
//        partialDataSet.addTable("main", "SELECT * FROM main");
//        partialDataSet.addTable("BAR");
//        FlatXmlDataSet.write(partialDataSet, new FileOutputStream("partial.xml"));

        // full database export
        IDataSet fullDataSet = iDatabaseConnection.createDataSet();
        //FlatXmlDataSet.write(fullDataSet, new FileOutputStream("full_DB.xml"));

        // dependent tables database export: export table X and all tables that
        // have a PK which is a FK on X, in the right order for insertion
        String[] depTableNames = TablesDependencyHelper.getAllDependentTables(iDatabaseConnection, "main");
        IDataSet depDataset = iDatabaseConnection.createDataSet(depTableNames);
        //        FlatXmlDataSet.write(depDataset, new FileOutputStream("JavaChatServer/src/test/resources/dependents.xml"));
        FlatXmlDataSet.write(depDataset, new FileOutputStream("JavaChatServer/src/test/resources/dataForTestDB_new.xml"));

        // write DTD file
        FlatDtdDataSet.write(depDataset, new FileOutputStream("JavaChatServer/src/test/resources/dtdForAuthServiceTest.dtd"));
        iDatabaseConnection.close();
        jdbcConnection.close();
    }
}
