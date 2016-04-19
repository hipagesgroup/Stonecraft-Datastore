package com.stonecraft.datastore;

import com.stonecraft.datastore.exceptions.DatabaseException;
import com.stonecraft.datastore.interaction.Join;
import com.stonecraft.datastore.interaction.Query;
import com.stonecraft.datastore.utils.StringUtils;
import com.stonecraft.datastore.view.DatabaseColumn;
import com.stonecraft.datastore.view.DatabaseTable;

import java.util.Map;

/**
 * This class
 * <p/>
 * Author: michaeldelaney
 * Created: 18/04/16
 */
public class AndroidQueryCreator {

    private static final String JOIN_CROSS_STRING = " CROSS JOIN ";
    private static final String JOIN_INNER_STRING = " INNER JOIN ";
    private static final String JOIN_LEFT_OUTER_STRING = " LEFT OUTER JOIN ";

    private DbSchemaModel myDbSchema;

    public AndroidQueryCreator(DbSchemaModel schemaModel) {
        myDbSchema = schemaModel;
    }

    /**
     * This method
     *
     * @param query
     * @return
     */
    public String getSQLJoinQuery(Query query) throws DatabaseException {
        StringBuilder statementBuilder = new StringBuilder();

        statementBuilder.append(DBConstants.SELECT);
        StringBuilder colBuilder = new StringBuilder();
        if(query.getColumns() == null || query.getColumns().length == 0){
            colBuilder.append(getColumnClause(query));
//            colBuilder.append("*");
        } else {

            for(String column : query.getColumns()){
                if(colBuilder.length() > 0){
                    colBuilder.append(",");
                }
                colBuilder.append(getColumnClause(column));
            }
        }

        if(query.isdistinct()){
            statementBuilder.append(" " + DBConstants.DISTINCT).append(DBConstants.OPEN_BRACKET);
            statementBuilder.append(colBuilder).append(DBConstants.CLOSE_BRACKET + " ");
        } else {
            statementBuilder.append(" " + colBuilder + " ");
        }
        statementBuilder.append(DBConstants.FROM).append(getJoinClause(query.getTable(), query));

        if(!StringUtils.isEmpty(query.getWhereClause())){
            statementBuilder.append(" " + DBConstants.WHERE + " " + query.getWhereClause());
        }
        if(!StringUtils.isEmpty(query.getOrderBy())){
            statementBuilder.append(" " + DBConstants.ORDER_BY + " " + query.getOrderBy());
        }
        if(!StringUtils.isEmpty(query.getGroupBy())){
            statementBuilder.append(" " + DBConstants.GROUP_BY + " " + query.getGroupBy());
        }
        if(!StringUtils.isEmpty(query.getHaving())){
            statementBuilder.append(" " + DBConstants.HAVING + " " + query.getHaving());
        }
        if(query.getLimit() > 0){
            statementBuilder.append(" " + DBConstants.LIMIT + " " + query.getLimit());
        }
        if(query.getLimit() > 0 && query.getOffset() > 0) {
            statementBuilder.append(" " + DBConstants.OFFSET + " " + query.getOffset());
        }

        return statementBuilder.toString();
    }

    /**
     * This method returns the column clause in the format of "table.columnName AS table.columnName"
     *
     * This method will also add the joined table columns if the query contains any joins.
     *
     * NOTE: If the query contains it's own columns it is expected the query will have the columns
     * in the correct format of table.column name for joins.
     *
     * @param query
     * @return
     */
    public String getColumnClause(Query query) {
        StringBuilder columnClause = new StringBuilder();

        String[] columns = query.getColumns();
        if(columns != null && columns.length > 0) {
            return getColumnClause(query.getTable(), columns);
        }

        String mainTable = getColumnClause(query.getTable(), null);
        if(!query.getJoins().isEmpty()) {
            for(Join join : query.getJoins()){
                columnClause.append(", ");
                columnClause.append(getColumnClause(join.getTable(), null));
            }
        }
        return mainTable + " " + columnClause.toString();
    }

    public String getColumnClause(String delimitedTableColumnName) throws DatabaseException {
        String[] tableColumnPair = delimitedTableColumnName.split("\\.");
        int count = tableColumnPair.length;
        try {
            return delimitedTableColumnName + " AS " +
                    DatabaseUtils.getDatabaseAsName(tableColumnPair[0], tableColumnPair[1]);
        } catch (ArrayIndexOutOfBoundsException e) {
            throw new DatabaseException("Column names must be delimited with the table name in " +
                    "a joined query");
        }
    }

    /**
     * This method returns the column clause in the format of "table.columnName AS table.columnName
     *
     * This method is mainly used for raw sql queries where a join is present so that the column can
     * be referenced after the query has been executed.
     *
     * @param tableName
     * @return
     */
    public String getColumnClause(String tableName, String[] columns) {
        DatabaseTable table = myDbSchema.getTable(tableName);
        StringBuilder columnClause = new StringBuilder();
        if(columns != null && columns.length > 0) {
            for(String column : columns) {
                columnClause.append(column + " AS " + column);
            }
        }
        for(Map.Entry<String, DatabaseColumn> entry : table.getColumns().entrySet()) {
            if(columnClause.length() > 0) {
                columnClause.append(", ");
            }

            String key = tableName + "." + entry.getKey();
            columnClause.append(key + " AS " +
                    DatabaseUtils.getDatabaseAsName(tableName, entry.getKey()));
        }

        return columnClause.toString();
    }

    private String getJoinClause(String table, Query statement) {
        StringBuilder joinBuilder = new StringBuilder(" ");
        joinBuilder.append(table);

        for(Join join : statement.getJoins()){
            switch(join.getJoinType()) {
                case Join.JOIN_CROSS : {
                    joinBuilder.append(JOIN_CROSS_STRING);
                    break;
                }
                case Join.JOIN_INNER : {
                    joinBuilder.append(JOIN_INNER_STRING);
                    break;
                }
                case Join.JOIN_LEFT_OUTER : {
                    joinBuilder.append(JOIN_LEFT_OUTER_STRING);
                    break;
                }
            }
            joinBuilder.append(join.getTable());

            if(join.getJoinType() != Join.JOIN_CROSS){
                joinBuilder.append(" ").append(DBConstants.ON).append(" ");

                StringBuilder joinExpressionBuilder = new StringBuilder();
                for(Join.JoinExpression expression : join.getJoinExpressions()){
                    if(joinExpressionBuilder.length() > 0){
                        joinExpressionBuilder.append(" ").append(DBConstants.AND).append(" ");
                    }

                    joinExpressionBuilder.append(expression.getLeftColumn().first).append(".");
                    joinExpressionBuilder.append(expression.getLeftColumn().second);

                    joinExpressionBuilder.append(" = ");

                    joinExpressionBuilder.append(expression.getRightColumn().first).append(".");
                    joinExpressionBuilder.append(expression.getRightColumn().second);
                }

                joinBuilder.append(joinExpressionBuilder);
            }
        }

        return joinBuilder.toString();
    }
}

