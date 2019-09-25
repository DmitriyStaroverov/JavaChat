package net.evricom.javachat.server;

import org.dbunit.DBTestCase;
import org.dbunit.PropertiesBasedJdbcDatabaseTester;
import org.dbunit.database.DatabaseConfig;
import org.dbunit.dataset.DataSetException;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.xml.FlatXmlDataSetBuilder;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * created by dima on 24.09.2019 0:58
 */

public class AuthServiceTest extends DBTestCase {

    private static ClassLoader classLoader;
    private static Logger logger;
    private static Properties propertiesTestDB;

    static {
        //
        logger = LoggerFactory.getLogger(AuthServiceTest.class);
        classLoader = AuthServiceTest.class.getClassLoader();
        //
        // read test properties
        propertiesTestDB = new Properties();
        try {
            propertiesTestDB.load(classLoader.getResourceAsStream("db.properties"));
        } catch (IOException e) {
            logger.error("Не найдено db.properties", e);
            fail(e.getLocalizedMessage());
        }
        // получим DDL для создания БД
        String sql = null;
        try {
            Path path = Paths.get(classLoader.getResource("create-data-model.sql").toURI());
            sql = new String(Files.readAllBytes(path));
        } catch (URISyntaxException | IOException e) {
            logger.error("Ошибка инициализации - create-data-model.sql", e);
            fail(e.getLocalizedMessage());
        }
        //создаем БД
        try (Connection connectionTestDB = DriverManager.getConnection(propertiesTestDB.getProperty("db.address"))) {
             connectionTestDB.createStatement().executeUpdate(sql);
        } catch (SQLException e) {
            logger.error("Ошибка при создании БД!", e);
            fail(e.getLocalizedMessage());
        }
    }

/*
    private static String readInputStreamToString(InputStream inputStream) throws IOException {
        ByteArrayOutputStream result = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int length;
        while ((length = inputStream.read(buffer)) != -1) {
            result.write(buffer, 0, length);
        }
        return result.toString(StandardCharsets.UTF_8.name());
    }
*/

    /**
     * конструктор класса вызывается каждлый раз перед вызвом каждого теста
     *
     * @param name имя теста
     */
    public AuthServiceTest(String name) {
        super(name);
        System.setProperty(PropertiesBasedJdbcDatabaseTester.DBUNIT_DRIVER_CLASS, propertiesTestDB.getProperty("db.driver"));
        System.setProperty(PropertiesBasedJdbcDatabaseTester.DBUNIT_CONNECTION_URL, propertiesTestDB.getProperty("db.address"));
    }

    /**
     * после каждого теста DBUnit удаляет все данные из базы
     * и заполняет базу заново тестовым набором значений
     *
     * @return набор тестовых данных, который будет наполняться база перед каждым тестом
     * @throws Exception ошибка
     */
    @Override
    protected IDataSet getDataSet() throws IOException, DataSetException {
        FlatXmlDataSetBuilder builder = new FlatXmlDataSetBuilder();
        builder.setMetaDataSetFromDtd(classLoader.getResourceAsStream("dataForTestDB.dtd"));
        return builder.build(classLoader.getResourceAsStream("dataForTestDB.xml"));
    }

    /**
     * изменгение конфигурации DBUNIT
     * @param config конфигурация DBUNIT
     */
    @Override
    protected void setUpDatabaseConfig(DatabaseConfig config) {
//        logger.debug("setUpDatabaseConfig");
//        config.setProperty(DatabaseConfig.FEATURE_DATATYPE_WARNING,false);
//        config.setProperty(DatabaseConfig.PROPERTY_DATATYPE_FACTORY, new HsqldbDataTypeFactory());
    }

    @Test
    public void test1_GetNickByLoginAndPass() throws Exception {
        AuthService.connect();
        Assert.assertNotNull(AuthService.getNickByLoginAndPass("login1", 106438208));
        AuthService.disconnect();
    }

    @Test
    public void test2_GetNickByLoginAndPass() throws Exception {
        AuthService.connect();
        Assert.assertNotNull(AuthService.getNickByLoginAndPass("vasya2", 106445665));
        AuthService.disconnect();
    }

    @Test
    public void testSave3() throws Exception {
        Assert.assertTrue(true);
    }

}
