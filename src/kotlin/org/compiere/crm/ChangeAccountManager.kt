package org.compiere.crm

import java.io.Serializable
import java.sql.Connection

class ChangeAccountManager : SvrProcessBaseSql() {
    override val isRO: Boolean
        get() = false
    var businessPartnerId: Int = 0
    var salesRepId: Int = 0

    override fun prepare() {
        super.prepare()
        for (para in parameter) {
            if (para.parameterName == "BusinessPartnerId") {
                businessPartnerId = para.parameterAsInt
            } else if (para.parameterName == "SalesRepId") {
                salesRepId = para.parameterAsInt
            }
        }
    }

    override fun getSqlResult(cnn: Connection): Serializable {
        val sql =
            """
update c_bpartner set salesrep_id = ? where c_bpartner_id = ?
and ad_client_id IN (0, ?) and ( ad_org_id IN (0,?) or ? = 0) and isactive = 'Y'
""".trimIndent()

        val statement = cnn.prepareStatement(sql)

        println( "!!!!!! sql: $sql")
        println( "!!!!!! salesRepId: $salesRepId")
        println( "!!!!!! businessPartnerId: $businessPartnerId")
        println( "!!!!!! AD_CLIENT_ID: $AD_CLIENT_ID")
        println( "!!!!!! AD_ORG_ID: $AD_ORG_ID")
        statement.setInt(1, salesRepId)
        statement.setInt(2, businessPartnerId)
        statement.setInt(3, AD_CLIENT_ID)
        statement.setInt(4, AD_ORG_ID)
        statement.setInt(5, AD_ORG_ID)

        val result = statement.executeUpdate()
        return result
    }
}