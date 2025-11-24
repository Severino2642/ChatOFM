package connexion;

import java.sql.*;

public class Base {

    public static Connection PsqlConnect(){
        Connection c = null;
        try
        {
            Class.forName("org.postgresql.Driver");
            c = DriverManager.getConnection("jdbc:postgresql://localhost:5432/liveconnecteo","postgres","samsoudine");
        }
        catch(Exception e){
            System.out.println("Erreur de connexion");
            e.printStackTrace();
        }
        return c;
    }

    public static Date getDate() throws Exception{
        Date result = null;
        boolean isclosed = true;
        Connection cnx = Base.PsqlConnect();
        String sql = "SELECT NOW() as T";
        Statement stmt = cnx.createStatement();
        try(ResultSet res = stmt.executeQuery(sql);){
            if(res.next()){
                result=res.getDate("T");
            }
        }

        if(isclosed){
            cnx.close();
        }
        return result;
    }

    public static String createId(Connection cnx, String nomsequence, String prefixe) throws Exception
    {
        String id = "";
        boolean closed = false;
        if(cnx.isClosed())
        {
            cnx = Base.PsqlConnect();
            closed = true;
        }

        int seq = -1;
        String sql = "SELECT nextval('"+nomsequence+"') as sequence";
        System.out.println("SQL >>>>> "+sql);
        Statement stmt = cnx.createStatement();
        ResultSet res = stmt.executeQuery(sql);
        while(res.next()){
            seq = res.getInt(1);
        }
        res.close();
        stmt.close();

        if (seq == -1)
        {
            if (closed)
            {
                cnx.close();
            }
            throw new Exception("Erreur lors de la recuperation de la valeur de la sequence: seq = -1");
        }
        else
        {
            id = prefixe + seq;
        }

        if (closed)
        {
            cnx.close();
        }
        return id;
    }
}
