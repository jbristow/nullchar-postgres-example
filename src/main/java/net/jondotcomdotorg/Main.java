package net.jondotcomdotorg;

import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.conf.ParamType;
import org.jooq.exception.DataAccessException;
import org.jooq.impl.DSL;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Arrays;

import static org.jooq.impl.DSL.*;

public class Main {
    private static final String url = "jdbc:postgresql://localhost:5432/jbristow?user=jbristow";

    private static final String dangerousName = "Strange *\u0000* Aeons";
    private static final byte[] badBytes = {
            0x53, 0x74, 0x72, 0x61, 0x6e, 0x67, 0x65, 0x20, 0x2a, 0x0, 0x2a, 0x20, 0x41, 0x65, 0x6f, 0x6e, 0x73
    };

    public static void main(String[] args) {
        System.setProperty("org.jooq.no-tips", "true");
        System.setProperty("org.jooq.no-logo", "true");

        try (Connection conn = DriverManager.getConnection(url)) {

            var context = DSL.using(conn, SQLDialect.POSTGRES);

            manualInsertString(conn);

            System.out.println();

            jooqInsertString(context);
            System.out.println();

            manualInsertBytes(conn);
            System.out.println();

            jooqInsertBytes(context);

        } catch (SQLException e) {
            System.err.println("unexpected sqlexception");
            e.printStackTrace();
        }
    }

    private static void manualInsertString(Connection conn) {
        try (PreparedStatement ps = conn.prepareStatement(
                "insert into book_store (name) values (?)")) {
            ps.setString(1, dangerousName);
            ps.execute();
        } catch (SQLException e) {
            System.out.println("Could not manually insert string with null zero char into postgres.");
        }
    }

    private static void jooqInsertString(DSLContext context) {
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
    }

    private static void manualInsertBytes(Connection conn) {
        try (PreparedStatement ps = conn.prepareStatement(
                "insert into book_store (name) values (convert_from(?::bytea, 'UTF-8'))")) {
            ps.setBytes(1, badBytes);
            ps.execute();
        } catch (SQLException e) {
            System.out.println("Could not manually insert binary into postgres.");
        }
    }

    private static void jooqInsertBytes(DSLContext context) {
        var insertStatement = context.insertInto(table("book_store"), field("name", String.class))
                                     .values(field("convert_from({0}, 'UTF-8')", String.class, badBytes));

        System.out.println(insertStatement.getSQL(ParamType.INLINED));

        try {
            var numInserted = insertStatement.execute();
            System.out.println("Inserted " + numInserted + " values");
        } catch (DataAccessException e) {
            System.out.println("Could not insert book_store.name=" + Arrays.toString(badBytes));
            System.out.println(e);
        }
    }
}
