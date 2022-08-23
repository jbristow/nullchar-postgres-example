package net.jondotcomdotorg;

import org.jooq.SQLDialect;
import org.jooq.conf.ParamType;
import org.jooq.exception.DataAccessException;
import org.jooq.impl.DSL;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import static org.jooq.impl.DSL.*;

public class Main {
    private static final String url = "jdbc:postgresql://localhost:5432/redacted?user=redacted";

    public static void main(String[] args) {
        System.setProperty("org.jooq.no-tips", "true");
        System.setProperty("org.jooq.no-logo", "true");

        var dangerousName = "Strange *\u0000* Aeons";

        try (Connection conn = DriverManager.getConnection(url)) {


            var context = DSL.using(conn, SQLDialect.POSTGRES);
            var insertStatement = context.insertInto(table("book_store"), field("name", String.class))
                                         .values(dangerousName);

            System.out.println(insertStatement.getSQL(ParamType.INLINED));

            try {
                var numInserted = insertStatement.execute();
                System.out.println("Inserted " + numInserted + " values");
            } catch (DataAccessException e) {
                System.out.println("Could not insert book_store.name=" + dangerousName);
                System.out.println(e);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
