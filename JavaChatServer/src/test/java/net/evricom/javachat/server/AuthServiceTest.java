package net.evricom.javachat.server;

import org.apache.commons.lang.StringUtils;
import org.dbunit.Assertion;
import org.dbunit.DBTestCase;
import org.dbunit.PropertiesBasedJdbcDatabaseTester;
import org.dbunit.database.DatabaseConfig;
import org.dbunit.dataset.DataSetException;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.xml.FlatXmlDataSetBuilder;
import org.junit.*;
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Properties;

/**
 * created by dima on 24.09.2019 0:58
 * Класс наследуется от DBTestCase, что не позволяет использовать аннотации
 * типа @Test или @Before, т.е. используется синтаксис JUnit3, где каждый тест
 * должен начинаться со слова "test" и проч.
 */

public class AuthServiceTest extends DBTestCase {

    private static ClassLoader classLoader = AuthServiceTest.class.getClassLoader();
    //
    private static Logger logger = LoggerFactory.getLogger(AuthServiceTest.class);
    private static Properties propertiesTestDB;
    private static FlatXmlDataSetBuilder builder;

    static {
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

        // init Builder
        builder = new FlatXmlDataSetBuilder();
        try {
            builder.setMetaDataSetFromDtd(classLoader.getResourceAsStream("dtdForAuthServiceTest.dtd"));
        } catch (DataSetException | IOException e) {
            logger.error(" init Builder error", e);
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
     * @throws DataSetException ошибка
     */
    @Override
    protected IDataSet getDataSet() throws DataSetException {
        return builder.build(classLoader.getResourceAsStream("dataAuthServiceTest_start.xml"));
    }

    /**
     * изменгение конфигурации DBUNIT
     *
     * @param config конфигурация DBUNIT
     */
    @Override
    protected void setUpDatabaseConfig(DatabaseConfig config) {
//        logger.debug("setUpDatabaseConfig");
//        config.setProperty(DatabaseConfig.FEATURE_DATATYPE_WARNING,false);
//        config.setProperty(DatabaseConfig.PROPERTY_DATATYPE_FACTORY, new HsqldbDataTypeFactory());
    }

    /**
     * выполняется перед каждым тестом
     *
     * @throws Exception ошибка
     */
    @Override
    public void setUp() throws Exception {
        super.setUp();
        AuthService.connect();
    }

    /**
     * выполняется после каждого теста
     *
     * @throws Exception ошибка
     */
    @Override
    public void tearDown() throws Exception {
        super.tearDown();
        AuthService.disconnect();
    }

    public void test_connect_Check_URL() throws Exception {
        String urlAuthService = AuthService.connection.getMetaData().getURL();
        String urlTest = getConnection().getConnection().getMetaData().getURL();
        Assert.assertEquals(urlAuthService, urlTest);
    }

    public void test_GetHistory_For_UnknownUser_GET_Only_Share_Msg() {
        String history = AuthService.getHistory("Unknown_User", new ArrayList<>());
        Assert.assertEquals(3, StringUtils.countMatches(history, "ообщение"));
        Assert.assertEquals(0, StringUtils.countMatches(history, "только для"));
    }

    public void test_GetHistory_For_NormUser_GET_Share_And_Privat_Msg() {
        String history = AuthService.getHistory("Петя", new ArrayList<>());
        Assert.assertEquals(5, StringUtils.countMatches(history, "ообщение"));
        Assert.assertEquals(2, StringUtils.countMatches(history, "только для"));
    }

    public void test_GetHistory_For_NormUser_GET_Share_And_Privat_Msg_Except_Blacklist() {
        String history;
        history = AuthService.getHistory("Петя", new ArrayList<>(Arrays.asList("Вася")));
        Assert.assertEquals(4, StringUtils.countMatches(history, "ообщение"));
        Assert.assertEquals(2, StringUtils.countMatches(history, "только для"));
    }

    public void test_AddHistory_add_1_msg_from_normUser() throws Exception {
        // вызываем тестируемый функционал
        AuthService.addHistory(new Date(1566047640841L), "nick1", null, "Сообщение add_1_Msg от nick1 для всех");
        // в history теперь +1 строка т.е. стало 7
        IDataSet actualDataSet = getConnection().createDataSet();
        // получим ожидаемый набор данных из XML файла - там уже 7 строк в таблице history
        IDataSet expectedDataSet = builder.build(classLoader.getResourceAsStream(
                "dataAuthServiceTest_AddHistory_add_1_Msg_From_Norm_User.xml"));
        // сравнение одной таблицы в датасетах, исключая столбец ID
        Assertion.assertEqualsIgnoreCols(expectedDataSet, actualDataSet, "history", new String[]{"id"});
    }

    //TODO исключения не отлавливаются таким образом, нужно переписать с поддержкой аннотаций
//    public void test_AddHistory_add_1_msg_from_UnknownUser() {
//        // вызываем тестируемый функционал
//        try {
//            AuthService.addHistory(new Date(1566047640841L),"UnknownUser",null, "");
//        } catch (Exception e){
//            Assert.assertFalse(true);
//        }
//    }

    public void test_AddHistory_add_3_Privat_Msg_from_normUser() throws Exception {
        AuthService.addHistory(new Date(1566047759401L), "Петя", "Вася", "Сообщение add_3_Msg от Петя только для Вася");
        AuthService.addHistory(new Date(1566047759402L), "Вася", "Петя", "Сообщение add_3_Msg от Вася только для Петя");
        AuthService.addHistory(new Date(1566047759403L), "nick1", "Вася", "Сообщение add_3_Msg от nick1 только для Вася");
        IDataSet actualDataSet = getConnection().createDataSet();
        IDataSet expectedDataSet = builder.build(classLoader.getResourceAsStream(
                "dataAuthServiceTest_AddHistory_add_3_Privat_Msg_From_Norm_User.xml"));
        Assertion.assertEqualsIgnoreCols(expectedDataSet, actualDataSet, "history", new String[]{"id"});
    }


    public void test_GetNickByLoginAndPass_For_NormUser_And_NormPassword() {
        String nick = AuthService.getNickByLoginAndPass("vasya2", 106445665);
        Assert.assertEquals("Вася", nick);
    }

    public void test_GetNickByLoginAndPass_For_NormUser_And_WrongPassword() {
        String nick = AuthService.getNickByLoginAndPass("vasya2", 106445666);
        Assert.assertNull(nick);
    }

    public void test_GetNickByLoginAndPass_For_WrongUser_And_NormPassword() {
        String nick = AuthService.getNickByLoginAndPass("vasya1", 106445665);
        Assert.assertNull(nick);
    }

    public void testGetBlackListForUser_1() {
        String blacklist = AuthService.getBlackListForUser("nick1");
        Assert.assertEquals("Вася Петя",blacklist);
    }

    public void testGetBlackListForUser_2() {
        String blacklist = AuthService.getBlackListForUser("Вася");
        Assert.assertEquals("Петя",blacklist);
    }

    public void testDeleteItemForBlackList_Norm_BlackList() throws Exception {
        AuthService.deleteItemForBlackList("Вася","Петя");
        IDataSet actualDataSet = getConnection().createDataSet();
        IDataSet expectedDataSet = builder.build(classLoader.getResourceAsStream(
                "dataAuthServiceTest_DeleteItemForBlackList.xml"));
        Assertion.assertEqualsIgnoreCols(expectedDataSet, actualDataSet, "blacklist", new String[]{"id"});
    }

    public void testDeleteItemForBlackList_Not_Exist_Item() throws Exception {
        AuthService.deleteItemForBlackList("Петя","Вася");
        IDataSet actualDataSet = getConnection().createDataSet();
        IDataSet expectedDataSet = builder.build(classLoader.getResourceAsStream(
                "dataAuthServiceTest_start.xml"));
        Assertion.assertEqualsIgnoreCols(expectedDataSet, actualDataSet, "blacklist", new String[]{"id"});
    }


    public void testAddItemForBlackList() throws Exception {
        AuthService.addItemForBlackList("Петя","Вася");
//        AuthService.addItemForBlackList("Петя","Вася");
        IDataSet actualDataSet = getConnection().createDataSet();
        IDataSet expectedDataSet = builder.build(classLoader.getResourceAsStream(
                "dataAuthServiceTest_AddItemForBlackList.xml"));
        Assertion.assertEqualsIgnoreCols(expectedDataSet, actualDataSet, "blacklist", new String[]{"id"});
    }

    public void testRegNewUser() throws Exception {
        AuthService.regNewUser("NewUserLogin",123456789,"NewUserNick");
        IDataSet actualDataSet = getConnection().createDataSet();
        IDataSet expectedDataSet = builder.build(classLoader.getResourceAsStream(
                "dataAuthServiceTest_RegNewUser.xml"));
        Assertion.assertEqualsIgnoreCols(expectedDataSet, actualDataSet, "main", new String[]{"id"});
    }

}
