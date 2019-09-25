package net.evricom.javachat.server;

import org.dbunit.Assertion;
import org.dbunit.DBTestCase;
import org.dbunit.PropertiesBasedJdbcDatabaseTester;
import org.dbunit.database.DatabaseConfig;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.ITable;
import org.dbunit.dataset.xml.FlatXmlDataSetBuilder;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.*;
import java.util.Properties;

/**
 * created by dima on 24.09.2019 0:58
 */

public class AuthServiceTest extends DBTestCase {

    private final static String nameTestDB = "testDB.db";
    private final static String pathDataModel = Paths.get(
            "src", "test", "resources", "create-data-model.sql").toFile().getAbsolutePath();
    private final static String pathDataXML = Paths.get(
            "src", "test", "resources", "dataForTestDB.xml").toFile().getAbsolutePath();
    private final static String pathDataDTD = Paths.get(
            "src", "test", "resources", "dataForTestDB.dtd").toFile().getAbsolutePath();

    private static Logger logger = LoggerFactory.getLogger(AuthServiceTest.class);

    static {
        // read test properties DB
        InputStream inputStream = ClassLoader.getSystemClassLoader().getResourceAsStream("db.properties");
        Properties props = new Properties();
        try {
            props.load(inputStream);
        } catch (IOException e) {
            logger.error("Не найдено db.properties",e);
            fail(e.getLocalizedMessage());
        }
        String driverDB = props.getProperty("db.driver");
        String addressDB = props.getProperty("db.address");
        logger.debug(driverDB);
        logger.debug(addressDB);

        // create database for this Test
        String sql = "";
        try {
            Connection connectionTestDB = DriverManager.getConnection("jdbc:sqlite:" + nameTestDB);
            sql = new String(Files.readAllBytes(Paths.get(pathDataModel)));
            Statement statement = connectionTestDB.createStatement();
            statement.executeUpdate(sql);
            statement.close();
            connectionTestDB.close();
        } catch (SQLException e) {
            logger.error("SQLException",e);
            fail(e.getLocalizedMessage());
        } catch (IOException e) {
            logger.error("IOException",e);
            fail(e.getLocalizedMessage());
        }
    }

    public AuthServiceTest(String name) {
        super(name);
        System.setProperty(PropertiesBasedJdbcDatabaseTester.DBUNIT_DRIVER_CLASS, "org.sqlite.JDBC");
        System.setProperty(PropertiesBasedJdbcDatabaseTester.DBUNIT_CONNECTION_URL, "jdbc:sqlite:" + nameTestDB);
//        System.setProperty(PropertiesBasedJdbcDatabaseTester.DBUNIT_USERNAME, "sa");
//        System.setProperty(PropertiesBasedJdbcDatabaseTester.DBUNIT_PASSWORD, "");
//        System.setProperty(PropertiesBasedJdbcDatabaseTester.DBUNIT_SCHEMA, "");
        logger.debug("AuthServiceTest NEW!!! name: " + name);
    }

    @Override
    protected IDataSet getDataSet() throws Exception {
        logger.debug("getDataSet()");

        FlatXmlDataSetBuilder builder = new FlatXmlDataSetBuilder();
        builder.setMetaDataSetFromDtd(new FileInputStream(pathDataDTD));
        builder.setColumnSensing(false);
        return builder.build(new FileInputStream(pathDataXML));
    }

    @Override
    protected void setUpDatabaseConfig(DatabaseConfig config) {
//        logger.debug("setUpDatabaseConfig");
        //config.setProperty(DatabaseConfig.FEATURE_DATATYPE_WARNING,false);
    }

    @Test
    public void test1_GetNickByLoginAndPass() throws Exception {
        AuthService.connect();
        Assert.assertNotNull(AuthService.getNickByLoginAndPass("login1",106438208));
        AuthService.disconnect();
    }

    @Test
    public void test2_GetNickByLoginAndPass() throws Exception {
        AuthService.connect();
        Assert.assertNotNull(AuthService.getNickByLoginAndPass("vasya2",106445665));
        AuthService.disconnect();
   }


    @Test
    public void testSave3() throws Exception {
        Assert.assertTrue(true);
    }


}
