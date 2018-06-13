package org.compiere.crm

import org.compiere.orm.DefaultModelFactory
import org.compiere.orm.IModelFactory
import org.compiere.process.SvrProcess
import org.idempiere.common.util.DB
import org.idempiere.common.util.Env
import org.idempiere.common.util.KeyNamePair
import org.idempiere.icommon.model.IPO

data class BPartnerFindResult(val id:Int, val name:String, val searchName : String, val taxid : String? )

data class FindResult( val rows : List<Any> ) : java.io.Serializable {
}

class Find : SvrProcess() {
    var search : String = ""
    var full : Boolean = false
    var opensearch : Boolean = false
    var AD_CLIENT_ID = 0 //AD_Client_ID
    var AD_ORG_ID = 0 //AD_Org_ID

    override fun prepare() {
        for (para in parameter) {
            if ( para.parameterName == "Search" ) {
                search = para.parameterAsString
            } else if ( para.parameterName == "OpenSearch" ) {
                opensearch = para.parameterAsBoolean
            } else if ( para.parameterName == "Full" ) {
                full = para.parameterAsBoolean
            } else if ( para.parameterName == "AD_Client_ID" ) {
                AD_CLIENT_ID = para.parameterAsInt
            } else if ( para.parameterName == "AD_Org_ID" ) {
                AD_ORG_ID = para.parameterAsInt
            } else println( "unknown parameter ${para.parameterName}" )
        }
    }

    override fun doIt(): String {
        val pi = processInfo

        val ctx = Env.getCtx()

        val tableName = "C_BPartner";

        val columns =
                if ( full ) { "*" } else { "c_bpartner_id,name,taxid" }

        val sql =
                """
select $columns from adempiere.C_BPartner
where (value ilike ? or name ilike ? or referenceno ilike ? or duns ilike ? or taxid ilike ?) -- params 1..5
and iscustomer = 'Y' and ad_client_id IN (0, ?) and ( ad_org_id IN (0,?) or ? = 0) and isactive = 'Y' -- params 6..8
order by 1 desc
                """.trimIndent()

        val sqlSearch = "%$search%".toLowerCase()

        val cnn = DB.getConnectionRO()
        val statement = cnn.prepareStatement(sql)
        statement.setString(1, sqlSearch)
        statement.setString(2, sqlSearch)
        statement.setString(3, sqlSearch)
        statement.setString(4, sqlSearch)
        statement.setString(5, sqlSearch)



        statement.setInt(6, AD_CLIENT_ID)
        statement.setInt(7, AD_ORG_ID)
        statement.setInt(8, AD_ORG_ID)
        val rs = statement.executeQuery()

        val modelFactory : IModelFactory = DefaultModelFactory()
        var result = mutableListOf<Any>()

        while(rs.next()) {
            if ( full ) {
                val row = modelFactory.getPO( "C_BPartner", rs, "pokus")
                result.add(row)
            } else {
                val name = rs.getString( "name" )
                val foundIdx = name.toLowerCase().indexOf(search.toLowerCase())
                val keyName = BPartnerFindResult( rs.getInt("c_bpartner_id"), name, name.substring(foundIdx), rs.getString( "taxid" ) )
                result.add(keyName)
            }
        }

        pi.serializableObject =
                if (full ) {
                    FindResult(result)
                } else {
                    if (opensearch) {
                        arrayOf<Any>(search, result.map { it as BPartnerFindResult }.map { it.name }.toTypedArray())
                    } else {
                        FindResult(result)
                    }
                }

        return "<result>"
    }

}