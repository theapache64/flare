package com.theapache64.dbbase.querybuilders;


import com.theapache64.dbbase.Connection;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by theapache64 on 9/10/17.
 */
public class AddQueryBuilder {

    public static class Builder {

        private final String tableName;
        private final Map<String, String> map;

        public Builder(String tableName) {
            this.tableName = tableName;
            this.map = new HashMap<>();
        }

        public Builder add(String column, String value) {
            map.put(column, value);
            return this;
        }

        public Builder add(String column, boolean value) {
            return add(column, value ? "1" : "0");
        }

        public boolean done() throws SQLException, QueryBuilderException {
            return doneAndReturn() != -1;
        }


        public long doneAndReturn() throws SQLException, QueryBuilderException {

            long rowId = -1;
            if (map.isEmpty()) {
                throw new QueryBuilderException("No data in insert query");
            }

            //Building query
            String error = null;
            final StringBuilder builder = new StringBuilder("INSERT INTO ").append(tableName).append("(");
            final List<String> columns = new ArrayList<>(map.keySet());
            final int totalColumns = columns.size();

            for (final String column : columns) {
                builder.append(column).append(",");
            }

            builder.append(") VALUES (").append(String.format("%0" + totalColumns + "d", 0).replace("0", "?,")).append(")");

            final String query = builder.toString().replaceAll(",\\)", "\\)");


            final java.sql.Connection con = Connection.getConnection();
            try {
                final PreparedStatement ps = con.prepareStatement(query, PreparedStatement.RETURN_GENERATED_KEYS);
                int i = 1;
                for (final Map.Entry<String, String> entry : map.entrySet()) {
                    ps.setString(i++, entry.getValue());
                }
                ps.executeUpdate();
                final ResultSet rs = ps.getGeneratedKeys();

                if (rs.first()) {
                    rowId = rs.getLong(1);
                }

                rs.close();
                ps.close();

            } catch (SQLException e) {
                e.printStackTrace();
                error = e.getMessage();
            } finally {
                try {
                    con.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }

            manageError(error);
            return rowId;
        }

        private void manageError(String error) throws SQLException {
            if (error != null) {
                throw new SQLException(error);
            }
        }
    }
}
