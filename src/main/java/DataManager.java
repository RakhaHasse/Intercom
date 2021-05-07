import org.telegram.telegrambots.meta.api.objects.Update;

import java.sql.*;
import java.util.*;
import java.util.Date;


public class DataManager {
    private static DataManager instance = null;

    public void addToActualTables(String source, String[] table) {
        try(Statement statement = Service.connect.createStatement()) {
            ResultSet resultSet = statement.executeQuery(
                    "SELECT type FROM connections WHERE source = " + source+ " ; "
            );
            resultSet.next();
            String type = resultSet.getString("type");
            resultSet.close();
            StringBuilder sql = new StringBuilder("INSERT INTO Tables (type,source,\"table\",name) VALUES ");
            for (int i = 1; table.length>i; i++){
                sql.append("(").append(type).append(", ").append(source).append(", ").append(table[0]).
                        append(", ").append(table[i]).append(")");
                if (table.length>i+1) sql.append(", ");
                else sql.append("; ");
            }
            statement.executeQuery(sql.toString());

        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }

    public void removeFromActualTables(String source, String tableName) {
        try(Statement statement = Service.connect.createStatement()) {
            ResultSet resultSet = statement.executeQuery(
                    "SELECT name INTO Tables WHERE source = "+
                            source+" AND \"table\" = \""+tableName+"\"; "
            );
            for (int i = 0; resultSet.next();i++){
                resultSet.deleteRow();
            }
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }

    public String[] getActualTable(String source, String tableName) {
        String[] result = null;
        try(Statement statement = Service.connect.createStatement()) {
            ResultSet resultSet = statement.executeQuery(
                    "SELECT name FROM Tables WHERE source = " + source+ " AND \"table\" = " + tableName
            );
            resultSet.next();
            ArrayList <String> array = new ArrayList<String>();
            array.add(resultSet.getString("table"));
            for (int i = 1; resultSet.next(); i++){
                array.add(i, resultSet.getString("name"));
            }
            result = new String[array.size()];
            array.toArray(result);
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return result;
    }

    //singleton class
    public static synchronized DataManager getInstance() {
        if (instance == null) instance = new DataManager();
        return instance;
    }

    private DataManager() {
        this.Service = new DBConnect("jdbc:sqlite:Service.db","SQLite");
        QuickStart();
    }

    private final ArrayList<DBConnect> Connections = new ArrayList<DBConnect>();
    private DBConnect Current;
    private final DBConnect Service;

    private String getServicePrefix(String type){

        try (Statement statement = Service.connect.createStatement()) {
            ResultSet resultSet = statement.executeQuery(
                    "SELECT prefix FROM types WHERE type = \""+type.toLowerCase(Locale.ROOT)+"\";"
            );
            resultSet.next();
            return resultSet.getString("prefix");
        } catch (SQLException throwables) {
            throwables.printStackTrace();
            return null;
        }

    }

    class DBConnect {
        String source;
        final String type;
        private Connection connect;

        public Connection getConnect() {
            return connect;
        }

        //Quick start use preinstalled SQLite DataBase
        public DBConnect() {
            // Регистрируем драйвер, с которым будем работать
            // в нашем случае Sqlite
            try {
                DriverManager.registerDriver(new org.sqlite.JDBC());
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }
            // Выполняем подключение к базе данных
            type = "SQLite";
            source = "Intercom.db";

            try {
                this.connect = DriverManager.getConnection(getServicePrefix(type)+source);
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }

        }

        //Full version. For any type DataBase.
        public DBConnect( String DriverPlusPath,  String type) {
            Connections.add(this);
            this.source = DriverPlusPath;
            this.type = type;
            //Для/For SQLite
            if (type.equalsIgnoreCase("SQLite")) {
                // Регистрируем драйвер, с которым будем работать
                // в нашем случае Sqlite
                try {
                    DriverManager.registerDriver(new org.sqlite.JDBC());
                } catch (SQLException throwables) {
                    throwables.printStackTrace();
                }
                // Выполняем подключение к базе данных
                try {
                    this.connect = DriverManager.getConnection(DriverPlusPath);
                } catch (SQLException throwables) {
                    throwables.printStackTrace();
                }
            }
            //Для/For PostgreSQL
            if (type.equalsIgnoreCase("PostgreSQl")) {
                // Регистрируем драйвер, с которым будем работать
                // в нашем случае PostgreSQL
                try {
                    DriverManager.registerDriver(new org.postgresql.Driver());
                } catch (SQLException throwables) {
                    throwables.printStackTrace();
                }
                // Выполняем подключение к базе данных
                try {
                    this.connect = DriverManager.getConnection(DriverPlusPath);
                } catch (SQLException throwables) {
                    throwables.printStackTrace();
                }
            }
            //По аналогии -- для кого-то ещё
            //Put under this comment your variant for your own DataBase


        }

        public void changeSource(String source) {
            this.source = source;
            try {
                this.connect.close();
                this.connect = DriverManager.getConnection(getServicePrefix(type)+source);
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }
        }
    }

    public void QuickStart() {
        Current = new DBConnect();
        Connections.add(Current);
    }

    public DBConnect findDB(String keyword) {
        DBConnect result = null;
        for (DBConnect connection : Connections) {
            if (connection.source.contains(keyword)) {
                result = connection;
                break;
            }
        }
        return result;
    }

    //Success -- true
    protected boolean addRow(String[] table, String[] rowValues) {
        boolean result = false;
        StringBuilder sqlQuery = new StringBuilder("INSERT INTO " + table[0] + " (");
        //+"(value1, valueN) VALUES (?,?);"
        for (int i = 0; i < rowValues.length; i++) {
            if (i > 0) sqlQuery.append(", ").append(table[i + 1]);
            else sqlQuery.append(table[i + 1]);
        }
        sqlQuery.append(") VALUES (");
        for (int i = 0; i < rowValues.length; i++) {
            if (i > 0) sqlQuery.append(", ?");
            else sqlQuery.append(" ?");
        }
        sqlQuery.append(") ;");
        try (PreparedStatement sql = Current.connect.prepareStatement(sqlQuery.toString())) {
            for (int i = 0; i < rowValues.length; i++) {
                sql.setObject(i + 1, rowValues[i]);
            }
            sql.execute();
            result = true;
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return result;
    }

    //Success -- true
    protected boolean addFewSameRows(String[] table, String[][] rowsValues) {
        boolean result = false;
        StringBuilder sqlQuery = new StringBuilder("INSERT INTO " + table[0] + " (");
        //+"(value1, valueN) VALUES (?,?),(?,?);"
        for (int i = 0; i < rowsValues[0].length; i++) {
            if (i > 0) sqlQuery.append(", ").append(table[i + 1]);
            else sqlQuery.append(table[i + 1]);
        }
        sqlQuery.append(") VALUES (");
        for (int j = 0; j < rowsValues.length; j++) {
            String[] row = rowsValues[j];
            for (int i = 0; i < row.length; i++) {
                if (i > 0) sqlQuery.append(", ?");
                else sqlQuery.append(" ?");
            }
            if (j < rowsValues.length - 1) sqlQuery.append("), (");
            else sqlQuery.append(")");
        }
        try (PreparedStatement sql = Current.connect.prepareStatement(sqlQuery.toString())) {
            for (int j = 0; j < rowsValues.length; j++) {
                String[] row = rowsValues[j];
                for (int i = 0; i < row.length; i++)
                    sql.setObject(j * row.length + i + 1, row[i]);
            }
            sql.execute();
            result = true;
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return result;
    }

    // boolean []
    protected void addFewDifferentRows(String[] table, String[][] rowsValues) {
        ArrayList<String[]> input = new ArrayList<String[]>();
        ArrayList<String[]> proces = new ArrayList<String[]>();

        Collections.addAll(input, rowsValues);

        for (String[] start : input) {
            String[][] checkResult = nullCheck(table, input.get(0));
            proces.add(checkResult[0]);
            proces.add(checkResult[1]);
            input.remove(0);
            for (String[] example : input) {
                String[][] prepared = nullCheck(table, example);
                if (sameCheck(checkResult, prepared)
                ) {
                    proces.add(prepared[1]);
                    input.remove(example);
                }
            }
            StringBuilder sqlQuery = new StringBuilder("INSERT INTO " + table[0] + " (");
            //+"(value1, valueN) VALUES (?,?),(?,?);"
            for (int i = 1; i < proces.get(0).length; i++) {
                if (i > 1) sqlQuery.append(", ").append(proces.get(0)[i]);
                else sqlQuery.append(proces.get(0)[i]);
            }
            proces.remove(0);
            sqlQuery.append(") VALUES (");
            for (int j = 0; j < proces.size(); j++) {
                String[] row = proces.get(j);
                for (int i = 0; i < row.length; i++) {
                    if (i > 0) sqlQuery.append(", ?");
                    else sqlQuery.append(" ?");
                }
                if (j < proces.size() - 1) sqlQuery.append("), (");
                else sqlQuery.append(")");
            }
            try (PreparedStatement sql = Current.connect.prepareStatement(sqlQuery.toString())) {
                for (int j = 0; j < proces.size(); j++) {
                    String[] row = proces.get(j);
                    for (int i = 0; i < row.length; i++)
                        sql.setObject(j * row.length + i + 1, row[i]);
                }
                sql.execute();
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }

        }

    }

    private String[][] nullCheck(String[] table, String[] row) {
        ArrayList<String> procesRow = new ArrayList<String>();
        ArrayList<String> procesTable = new ArrayList<String>();
        procesTable.add(table[0]);
        for (int i = 0; i < row.length; i++) {
            if (row[i] != null && !row[i].equalsIgnoreCase("null")) {
                procesRow.add(row[i]);
                procesTable.add(table[i + 1]);
            }
        }
        return new String[][]{(String[]) procesTable.toArray(), (String[]) procesRow.toArray()};
    }

    private boolean sameCheck(String[][] exampleA, String[][] exampleB) {
        if (exampleA[1].length == exampleB[1].length)
            for (int i = 0; i < exampleB[1].length; i++) {
                if (!exampleA[0][i + 1].equals(exampleB[0][i + 1]))
                    return false;
            }
        else return false;
        return true;
    }


    private String getToFind(String[] table, String[] criteria) {
        String toFind = null;
        for (int i = 0; i < criteria.length; i++) {
            if (criteria[i].equalsIgnoreCase("tofind")) {
                toFind = table[i + 1];
                break;
            }
        }
        return toFind;
    }

    /*
    Massive table present:
    zero value = table name
    other = column name. order of this values important for method in use criteria massive

    Massive criteria present:
    keyword "tofind" (in any case) -- what are you try find? It's define (by position)
    where you try find results.
    keyword "null" (in any case) -- we doesn't have this value
    If your table massive look like {staff, first name, last name, phone)
    And criteria massive look like              {null, Copperfield, tofind}
    For method it does mean:
    you don't know first name
    criteria to find is last name. You need Copperfield.
    you try found result as telephone number

     */
    protected String findFirstValue(String[] table, String[] criteria) {
        String result = null;
        String toFind = getToFind(table, criteria);
        String[][] checked = nullCheck(table,criteria);
        try {
            result = Objects.requireNonNull(onePointSearch(checked[0], checked[1])).getString(toFind);
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }


        return result;
    }

    private ResultSet onePointSearch(String[] table, String[] criteria) {
        String tableName = table[0];
        String toFind = null;
        ArrayList<String[]> condition = new ArrayList<String[]>();
        for (int i = 0; i < criteria.length; i++) {
            if (toFind == null && criteria[i].equalsIgnoreCase("tofind")) {
                toFind = table[i + 1];
                continue;
            }
            if (criteria[i] != null && !criteria[i].equalsIgnoreCase("null")) {
                String[] task = {table[i + 1], criteria[i]};
                condition.add(task);
            }
        }
        StringBuilder sqlQuery = new StringBuilder("SELECT " + toFind + " FROM " + tableName + " WHERE ");
        for (int i = 0; i < condition.size(); i++) {
            if (i < condition.size() - 1)
                sqlQuery.append(condition.get(i)[0]).append(" = ").append(condition.get(i)[1]).append(" AND ");
            else sqlQuery.append(condition.get(i)[0]).append(" = ").append(condition.get(i)[1]).append(" ;");
        }
        try (Statement statement = Current.connect.createStatement()) {
            ResultSet resultSet = statement.executeQuery(sqlQuery.toString());
            resultSet.next();
            return resultSet;
        } catch (SQLException throwables) {
            throwables.printStackTrace();
            return null;
        }
    }

    /*
    Massive table present:
    zero value = table name
    other = column name. order of this values important for method in use criteria massive

    Massive criteria present:
    keyword "tofind" (in any case) -- what are you try find? It's define (by position)
    where you try find results.
    keyword "null" (in any case) -- we doesn't have this value
    If your table massive look like {staff, first name, last name, phone)
    And criteria massive look like              {null, Copperfield, tofind}
    For method it does mean:
    you don't know first name
    criteria to find is last name. You need Copperfield.
    you try found result as telephone number

     */
    public String[] findOneColumnValuesPack(String[] table, String[] criteria) {
        String toFind = getToFind(table, criteria);
        String[][] checked = nullCheck(table,criteria);
        ResultSet Data = onePointSearch(checked[0], checked[1]);
        ArrayList<String> ResultData = new ArrayList<String>();
        try {
            do ResultData.add(Objects.requireNonNull(Data).getString(toFind)); while (Data.next());
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        String[] result = new String[ResultData.size()];
        for (int i = 0; i < result.length; i++) {
            result[i] = ResultData.get(i);
        }
        return result;
    }

    public boolean processQuery(String sql) {
        try (PreparedStatement statement = Current.connect.prepareStatement(sql)) {
            statement.executeQuery();
            return true;
        } catch (SQLException throwables) {
            throwables.printStackTrace();
            return false;
        }


    }

    public boolean addRowIntoCommunity ( String UserID, String Username, String Status){
        return addRow(getActualTable(Current.source,"Community"),new String[]{UserID,Username,
                (Status==null)?"":Status});
    }

    public boolean addRowIntoBlackList ( String UserID, String Reason){
        try (Statement statement = Current.getConnect().createStatement()) {
            statement.execute(
                    "UPDATE Community SET Status = \"blocked\" where UserID = "+ Long.parseLong(UserID)
            );
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return addRow(getActualTable(Current.source,"BlackList"),new String[]{UserID,
                (Reason==null)?"":Reason});
    }

    public boolean addRowIntoStaff ( String UserID, String Status){
        return addRow(getActualTable(Current.source,"Staff"),new String[]{UserID,
                (Status==null)?"":Status});
    }

    protected boolean updateFirstRow (String[] table,String[] rowToFind, String[] rowToUpdate){
        boolean result = false;
        String[][] checked = nullCheck(table,rowToFind);
        ResultSet Data = onePointSearch(checked[0],checked[1] );
        for (int i =0; i<rowToFind.length;i++) {
            if (!rowToFind[i].equals(rowToUpdate[i])) {
                try {
                    Objects.requireNonNull(Data).updateNString(table[i + 1], rowToUpdate[i]);
                    result = true;
                } catch (SQLException throwables) {
                    throwables.printStackTrace();
                    result = false;
                }
            }
        }
        return result;
    }

    public String getSource (String address, String type){
        try (Statement statement = Service.connect.createStatement()) {
            ResultSet resultSet = statement.executeQuery(
                    "SELECT prefix FROM types WHERE type = "+type.toLowerCase(Locale.ROOT)
            );
            resultSet.next();
                return resultSet.getString("prefix")+address;
        } catch (SQLException throwables) {
            throwables.printStackTrace();
            return null;
        }
    }

    protected boolean deleteFirstRow (String [] table, String [] rowToFind){
        String[][] checked = nullCheck(table,rowToFind);
        ResultSet resultSet = onePointSearch(checked[0], checked[1]);
        try {
            assert resultSet != null;
            resultSet.deleteRow();
            return true;
        } catch (SQLException throwables) {
            throwables.printStackTrace();
            return false;
        }

    }

    protected String getBotToken(){
        try (Statement statement = Service.connect.createStatement()) {
            ResultSet resultSet = statement.executeQuery(
                    "SELECT Parameter_value FROM Launch WHERE Parameter_name = \"BotToken\";"
            );
            resultSet.next();
            return resultSet.getString("Parameter_value");
        } catch (SQLException throwables) {
            throwables.printStackTrace();
            return null;
        }
    }

    protected String getOfficeID (){
        try (Statement statement = Service.connect.createStatement()) {
            ResultSet resultSet = statement.executeQuery(
                    "SELECT Parameter_value FROM Launch WHERE Parameter_name = \"OfficeID\";"
            );
            resultSet.next();
            return resultSet.getString("Parameter_value");
        } catch (SQLException throwables) {
            throwables.printStackTrace();
            return null;
        }
    }

    protected String getBotUsername (){
        try (Statement statement = Service.connect.createStatement()) {
            ResultSet resultSet = statement.executeQuery(
                    "SELECT Parameter_value FROM Launch WHERE Parameter_name = \"BotUsername\";"
            );
            resultSet.next();
            return resultSet.getString("Parameter_value");
        } catch (SQLException throwables) {
            throwables.printStackTrace();
            return null;
        }
    }

    protected String getOwnerID () {
        try (Statement statement = Service.connect.createStatement()) {
            ResultSet resultSet = statement.executeQuery(
                    "SELECT Parameter_value FROM Launch WHERE Parameter_name = \"OwnerID\";"
            );
            resultSet.next();
            return resultSet.getString("Parameter_value");
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    protected void setOwnerID(String OwnerID){
        try (Statement statement = Service.connect.createStatement()) {
            statement.execute("UPDATE Launch SET Parameter_value = \""+OwnerID+
                    "\" WHERE Parameter_name = \"OwnerID\";");
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }

    protected void setOfficeID (String OfficeID){
        try (Statement statement = Service.connect.createStatement()) {
            statement.execute("UPDATE Launch SET Parameter_value = \""+OfficeID+
                    "\" WHERE Parameter_name = \"OfficeID\";");
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }

    protected boolean checkUserID (Update update){
        try (Statement statement = Current.connect.createStatement()){
            ResultSet resultSet = statement.executeQuery(
                    "SELECT * From Community Where UserID = "+
                            update.getMessage().getFrom().getId()+"; "
            );
            if (!resultSet.next()) {
                addRowIntoCommunity(update.getMessage().getFrom().getId().toString(),
                        update.getMessage().getFrom().getUserName(),
                        "User");
                return true;
            }
            else {
                resultSet.close();
                resultSet = statement.executeQuery(
                        "SELECT * From BlackList Where UserID = "+
                                update.getMessage().getFrom().getId()+"; "
                );
                if (!resultSet.next()) return true;
                else return false;
            }
        } catch (SQLException throwables) {
            throwables.printStackTrace();
            return false;
        }
    }

    protected void addMessage (String messageText, Long UserID, int MessageID){
        try (Statement statement = Current.connect.createStatement()) {
            Date date = new Date();
            statement.execute(
                "INSERT INTO messages (UserID, message_id, message, upontime) Values ("
                        +UserID+", "+MessageID+ ", \""+messageText +"\","+ date.getTime() +")"
        );
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }

    protected int getMessageID (Long UserID, String MessageText){
        try (Statement statement = Current.connect.createStatement()) {
            ResultSet resultSet = statement.executeQuery(
                    "Select message_id from messages where UserID = "+
                            UserID+" AND message = \""+MessageText+"\"; "
            );
            if (resultSet.next()){
                int result = resultSet.getInt("message_id");
                do {
                 if (result<resultSet.getInt("message_id"))
                     result=resultSet.getInt("message_id");
                } while (resultSet.next())   ;
                return result;
            }
            else return -1;
        } catch (SQLException throwables) {
            throwables.printStackTrace();
            return -2;
        }
    }
}
