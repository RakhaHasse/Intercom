import java.sql.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Objects;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class DataManager {
    private static DataManager instance = null;
    private final ArrayList<String[]> actualTables = new ArrayList<String[]>();

    public void addToActualTables(String[] table) {
        actualTables.add(table);
    }

    public void removeFromActualTables(String tableName) {
        for (int i = 0; i < actualTables.size(); i++) {
            if (actualTables.get(i)[0].equals(tableName)) {
                actualTables.remove(i);
                break;
            }

        }
    }

    public void removeFromActualTables(String[] table) {
        actualTables.remove(table);
    }

    public String[] getActualTable(String tableName) {
        String[] result = null;
        for (String[] actualTable : actualTables) {
            if (actualTable[0].equals(tableName)) {
                result = actualTable;
                break;
            }
        }
        return result;
    }

    //singleton class
    public static synchronized DataManager getInstance() {
        if (instance == null) instance = new DataManager();
        return instance;
    }
    private DataManager() {
        actualTables.add(new String[]{"Community", "UserID", "Username", "Status"});
        actualTables.add(new String[]{"Black List", "UserID", "Reason"});
        actualTables.add(new String[]{"Staff", "UserID", "Status"});
        DBTypes.add(SQLite); DBTypes.add(PostgreSQl);
    }

    // перечисление доступных типов баз данных
    private final String[] SQLite = new String[]{"sqlite", "jdbc:sqlite:"},
            PostgreSQl = new String[]{"postgresql", "org:posgresql:"};
    private final ArrayList<String[]> DBTypes = new ArrayList<String[]>();

    private final ArrayList<DBConnect> Connections = new ArrayList<DBConnect>();
    private DBConnect Current;

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
            source = SQLite[1]+"Intercom.db";
            type = SQLite[0];
            try {
                this.connect = DriverManager.getConnection(source);
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }

        }

        //Full version. For any type DataBase.
        public DBConnect(@NotNull String source, @NotNull String type) {
            Connections.add(this);
            this.source = source;
            this.type = type;
            //Для/For SQLite
            if (type.equalsIgnoreCase(SQLite[0])) {
                // Регистрируем драйвер, с которым будем работать
                // в нашем случае Sqlite
                try {
                    DriverManager.registerDriver(new org.sqlite.JDBC());
                } catch (SQLException throwables) {
                    throwables.printStackTrace();
                }
                // Выполняем подключение к базе данных
                try {
                    this.connect = DriverManager.getConnection(source);
                } catch (SQLException throwables) {
                    throwables.printStackTrace();
                }
            }
            //Для/For PostgreSQL
            if (type.equalsIgnoreCase(PostgreSQl[0])) {
                // Регистрируем драйвер, с которым будем работать
                // в нашем случае PostgreSQL
                try {
                    DriverManager.registerDriver(new org.postgresql.Driver());
                } catch (SQLException throwables) {
                    throwables.printStackTrace();
                }
                // Выполняем подключение к базе данных
                try {
                    this.connect = DriverManager.getConnection(source);
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
                this.connect = DriverManager.getConnection(source);
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

    @Nullable
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
        }
        return false;

    }

    public boolean addRowIntoCommunity (@NotNull String UserID, @NotNull String Username, String Status){
        return addRow(getActualTable("Community"),new String[]{UserID,Username,
                (Status==null)?"":Status});
    }

    public boolean addRowIntoBlackList (@NotNull String UserID, String Reason){
        return addRow(getActualTable("Black List"),new String[]{UserID,
                (Reason==null)?"":Reason});
    }

    public boolean addRowIntoStaff (@NotNull String UserID, String Status){
        return addRow(getActualTable("Staff"),new String[]{UserID,
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
        for (String [] example: DBTypes)
            if (example[0].equalsIgnoreCase(type))
                return example[1]+address;
        return null;
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

}
