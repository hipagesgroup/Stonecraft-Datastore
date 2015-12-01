package au.com.stonecraft.common.database.interaction;

/**
 * Created by michaeldelaney on 18/11/15.
 */
public class RawStatement extends Statement {
    private String myRawStatement;

    public RawStatement(String table, String rawStatement) {
        super(table);
        myRawStatement = rawStatement;
    }

    public String getRawStatement() {
        return myRawStatement;
    }
}
