import org.junit.Assert
import org.junit.Test
import org.compiere.crm.MBPartner
import org.idempiere.common.db.CConnection
import org.idempiere.common.db.Database
import org.idempiere.common.util.CLogger
import org.idempiere.common.util.DB
import org.idempiere.common.util.Env
import org.idempiere.common.util.Ini
import pg.org.compiere.db.DB_PostgreSQL

class BPartnerTests {
    @Test
    fun loading_saving_business_partner_work() {
        Ini.getIni().isClient = false
        CLogger.getCLogger(BPartnerTests::class.java)
        Ini.getIni().loadProperties(false)
        Ini.getIni().properties
        val db = Database()
        db.setDatabase(DB_PostgreSQL())
        DB.setDBTarget(CConnection.get(null))
        DB.isConnected()

        val ctx = Env.getCtx()
        val AD_CLIENT_ID = 11
        val AD_CLIENT_ID_s = AD_CLIENT_ID.toString()
        ctx.setProperty(Env.AD_CLIENT_ID, AD_CLIENT_ID_s )
        Env.setContext(ctx, Env.AD_CLIENT_ID, AD_CLIENT_ID_s )

        val id = 118
        val partner = MBPartner.get( Env.getCtx(), id )

        Assert.assertEquals( id, partner.c_BPartner_ID)
        Assert.assertEquals( "JoeBlock", partner.value)
        Assert.assertEquals( "Joe Block", partner.name)

        val partner2 : org.compiere.crm.MBPartner = partner as MBPartner

        val newValue = "JoeBlock*"
        partner2.setValue( newValue )
        partner2.save()

        val partner3 = MBPartner.get( Env.getCtx(), id )

        Assert.assertEquals( id, partner3.c_BPartner_ID)
        Assert.assertEquals( newValue, partner3.value)
        Assert.assertEquals( "Joe Block", partner3.name)

        partner2.setValue( "JoeBlock" )
        partner2.save()

        val newPartner = MBPartner.getTemplate(ctx, AD_CLIENT_ID)
        newPartner.setName("Test 123")
        newPartner.setValue("Test123")
        newPartner.save()
        newPartner.delete(true)
    }
}