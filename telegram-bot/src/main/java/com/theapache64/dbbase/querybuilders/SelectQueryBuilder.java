package com.theapache64.dbbase.querybuilders;

import com.theapache64.dbbase.BaseTable;
import com.theapache64.dbbase.Connection;
import org.jetbrains.annotations.NotNull;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by theapache64 on 27/10/17.
 */
public class SelectQueryBuilder<T> {

    public static final String UNLIMITED = "NO_LIMIT";
    private final String tableName;
    private final Callback<T> callback;
    private String[] columns;
    private String limit;
    private String[] whereColumns;
    private String[] whereColumnValues;
    private String orderBy;
    private ResultSet rs;
    private Statement stmt;
    private String sqlError;
    private final String query;
    private final String[] queryParams;

    public SelectQueryBuilder(String tableName, Callback<T> callback, String[] columns, String[] whereColumns, String[] whereColumnValues, String limit, String orderBy, String query, String[] queryParams) {
        this.tableName = tableName;
        this.callback = callback;
        this.columns = columns;
        this.whereColumns = whereColumns;
        this.whereColumnValues = whereColumnValues;
        this.limit = limit;
        this.orderBy = orderBy;
        this.query = query;
        this.queryParams = queryParams;
    }

    public SelectQueryBuilder(String tableName, Callback<T> callback, String[] columns, String[] whereColumns, String[] whereColumnValues, String limit, String orderBy) {
        this(tableName, callback, columns, whereColumns, whereColumnValues, limit, orderBy, null, null);
    }

    private String getFullQuery() throws QueryBuilderException {

        if (query != null) {
            return query;
        } else {

            //Building custom query

            StringBuilder queryBuilder = new StringBuilder("SELECT ");
            if (columns == null) {
                throw new QueryBuilderException("No columns selected");
            }

            for (final String column : columns) {
                queryBuilder.append(column).append(",");
            }

            //Removing last comma
            queryBuilder = new StringBuilder(queryBuilder.substring(0, queryBuilder.length() - 1));

            queryBuilder.append(" FROM ").append(tableName);

            if (whereColumns != null && whereColumnValues != null) {

                if (whereColumns.length != whereColumnValues.length) {
                    throw new QueryBuilderException("Where section count doesn't match");
                }

                queryBuilder.append(" WHERE ");

                for (final String wColumn : whereColumns) {
                    queryBuilder.append(wColumn).append("= ? AND ");
                }

                //Removing last and
                //4  = AND .length()
                queryBuilder = new StringBuilder(queryBuilder.substring(0, queryBuilder.length() - 4));
            }

            if (orderBy != null) {
                queryBuilder.append(" ORDER BY ").append(orderBy);
            }

            if (limit != null && !limit.equals(UNLIMITED)) {
                queryBuilder.append(" LIMIT ").append(limit);
            }

            return queryBuilder.toString();
        }


    }


    public static class Builder<T> {

        private final String tableName;
        private final Callback<T> callback;
        private String[] columns;
        private String limit = null;
        private String[] whereColumns;
        private String[] whereColumnValues;
        private String orderBy;
        private String query;
        private String[] queryParams;

        public Builder(String tableName, @NotNull Callback<T> callback) {
            this.tableName = tableName;
            this.callback = callback;
        }

        public Builder<T> select(String[] columns) {
            this.columns = columns;
            return this;
        }

        public Builder<T> limit(String limit) {
            this.limit = limit;
            return this;
        }

        public Builder<T> limit(long limit) {
            return limit(String.valueOf(limit));
        }

        public SelectQueryBuilder<T> build() {
            return new SelectQueryBuilder<T>(tableName, callback, columns, whereColumns, whereColumnValues, limit, orderBy, query, queryParams);
        }

        public Builder<T> where(String whereColumn, String whereColumnValue) {
            this.whereColumns = new String[]{whereColumn};
            this.whereColumnValues = new String[]{whereColumnValue};
            return this;
        }

        public Builder<T> orderBy(String orderBy) {
            this.orderBy = orderBy;
            return this;
        }

        public Builder<T> where(String[] whereColumns, String[] whereColumnValues) {
            this.whereColumns = whereColumns;
            this.whereColumnValues = whereColumnValues;
            return this;
        }

        public Builder<T> query(String query) {
            this.query = query;
            return this;
        }

        public Builder<T> bind(String[] queryParams) {
            this.queryParams = queryParams;
            return this;
        }
    }

    public T get() throws QueryBuilderException, SQLException {


        final String fullQuery = getFullQuery();


        java.sql.Connection con = Connection.getConnection();
        this.rs = null;
        this.stmt = null;
        this.sqlError = null;

        doDuplicate(con, fullQuery);

        T t = null;


        try {

            if (rs != null && rs.first()) {
                t = callback.getNode(rs);
            }


            if (rs != null) {
                rs.close();
            }

            if (stmt != null) {
                stmt.close();
            }

        } catch (SQLException e) {
            e.printStackTrace();
            sqlError = e.getMessage();
        } finally {
            try {
                con.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        BaseTable.manageError(sqlError);

        return t;
    }

    private void doDuplicate(java.sql.Connection con, String fullQuery) throws SQLException {

        if (whereColumns != null) {

            try {
                final PreparedStatement ps = con.prepareStatement(fullQuery);
                for (int i = 1; i <= whereColumnValues.length; i++) {
                    ps.setString(i, whereColumnValues[i - 1]);
                }

                rs = ps.executeQuery();
                stmt = ps;

            } catch (SQLException e) {
                e.printStackTrace();
                sqlError = e.getMessage();
            }

            BaseTable.manageError(sqlError);

        } else {

            try {
                stmt = con.createStatement();
                rs = stmt.executeQuery(fullQuery);
            } catch (SQLException e) {
                e.printStackTrace();

                sqlError = e.getMessage();
            }

            BaseTable.manageError(sqlError);
        }

    }

    public List<T> getAll() throws SQLException, QueryBuilderException {


        final String fullQuery = getFullQuery();

        java.sql.Connection con = Connection.getConnection();
        this.rs = null;
        this.stmt = null;
        this.sqlError = null;

        doDuplicate(con, fullQuery);

        List<T> t = new ArrayList<T>();


        try {

            if (rs != null && rs.first()) {

                t = new ArrayList<T>();

                do {
                    t.add(callback.getNode(rs));
                } while (rs.next());
            }


            if (rs != null) {
                rs.close();
            }

            if (stmt != null) {
                stmt.close();
            }

        } catch (SQLException e) {
            e.printStackTrace();
            sqlError = e.getMessage();
        } finally {
            try {
                con.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        BaseTable.manageError(sqlError);

        return t;

    }

    public interface Callback<T> {
        T getNode(ResultSet rs) throws SQLException;
    }
}
