package com.theapache64.dbbase;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import java.sql.SQLException;

/**
 * Created by theapache64 on 10/13/2015.
 */
public class Connection {

    private static DataSource ds;

    public static java.sql.Connection getConnection() {

        try {

            if (ds == null) {
                final Context initContext = new InitialContext();
                Context envContext = (Context) initContext.lookup("java:/comp/env");
                ds = (DataSource) envContext.lookup("jdbc/flare");
            }

            return ds.getConnection();

        } catch (NamingException | SQLException e) {
            e.printStackTrace();
            throw new IllegalArgumentException("com.theapache64.dbbase.Connection error : " + e.getMessage());
        }
    }

}
