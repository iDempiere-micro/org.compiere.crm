package org.compiere.crm;

import org.idempiere.common.util.CLogger;

import javax.mail.internet.InternetAddress;
import java.io.Serializable;
import java.util.logging.Level;

public class EMail implements Serializable {
    protected transient static CLogger log = CLogger.getCLogger (EMail.class);

    /**
     * 	Validate format of an email address
     *  IDEMPIERE-1409
     *	@return true if email has proper format
     */
    public static boolean validate(final String email) {
        try
        {
            new InternetAddress(email, true);
        }
        catch (Exception e)
        {
            log.log(Level.WARNING, email + ": " + e.toString());
            return false;
        }
        return true;
    }
}
