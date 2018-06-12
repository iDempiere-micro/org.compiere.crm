package org.compiere.crm

import org.compiere.orm.DefaultModelFactory
import org.compiere.orm.IModelFactory
import org.compiere.process.SvrProcess
import org.idempiere.common.util.DB
import org.idempiere.common.util.Env
import org.idempiere.common.util.KeyNamePair
import org.idempiere.icommon.model.IPO

data class FindResult( val rows : List<Any> ) : java.io.Serializable {
}

class Find : SvrProcess() {
    var search : String = ""
    var full : Boolean = false

    override fun prepare() {
        for (para in parameter) {
            println( "para:$para" )
            if ( para.parameterName == "Search" ) {
                search = para.parameterAsString
            } else if ( para.parameterName == "Full" ) {
                full = para.parameterAsBoolean
            } else println( "unknown parameter ${para.parameterName}" )
        }
    }

    override fun doIt(): String {
        val pi = processInfo

        val ctx = Env.getCtx()

        val tableName = "C_BPartner";

        val sql =
                """
select * from adempiere.C_BPartner
where (value like ? or name like ? or referenceno like ? or duns like ? or taxid like ?)
and iscustomer = 'Y' and ad_client_id IN (0, ?) and ad_org_id IN (0,?) and isactive = 'Y'
order by 1 desc
                """.trimIndent()

        search = "%$search%"

        println ( "SQL:$sql" )
        println ( "-- search:$search" )

        val cnn = DB.getConnectionRO()
        val statement = cnn.prepareStatement(sql)
        statement.setString(1, search)
        statement.setString(2, search)
        statement.setString(3, search)
        statement.setString(4, search)
        statement.setString(5, search)

        val AD_CLIENT_ID = ctx.getProperty(Env.AD_CLIENT_ID ).toInt()
        val AD_ORG_ID = ctx.getProperty(Env.AD_ORG_ID ).toInt()

        println ( "-- AD_CLIENT_ID:$AD_CLIENT_ID" )
        println ( "-- AD_ORG_ID:$AD_ORG_ID" )

        statement.setInt(6, AD_CLIENT_ID)
        statement.setInt(7, AD_ORG_ID)
        val rs = statement.executeQuery()

        val modelFactory : IModelFactory = DefaultModelFactory()
        var result = mutableListOf<Any>()

        while(rs.next()) {
            if ( full ) {
                val row = modelFactory.getPO( "C_BPartner", rs, "pokus")
                result.add(row)
            } else {
                val keyName = KeyNamePair( rs.getInt("c_bpartner_id"), rs.getString( "name" ) )
                result.add(keyName)
            }
        }

        pi.serializableObject = FindResult( result )

        return "<result>"
    }

}