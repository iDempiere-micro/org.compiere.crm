package org.compiere.crm

import org.compiere.orm.DefaultModelFactory
import org.compiere.orm.IModelFactory
import org.idempiere.common.util.DB
import org.compiere.model.I_C_BPartner
import org.compiere.model.I_C_ContactActivity
import java.io.Serializable
import java.math.BigDecimal
import java.sql.Connection

data class BPartnerFindResult(val id: Int, val name: String, val searchName: String, val taxid: String?)

data class FindResult(val rows: List<Any>) : java.io.Serializable

data class BPartnerWithActivity(val BPartner: I_C_BPartner, val ContactActivity: I_C_ContactActivity?, val BPartner_Category: String?)

class Find : SvrProcessBaseSql() {
    override val isRO: Boolean
        get() = true
    var search: String = ""
    var full: Boolean = false
    var opensearch: Boolean = false

    override fun prepare() {
        super.prepare()
        for (para in parameter) {
            if (para.parameterName == "Search") {
                search = para.parameterAsString
            } else if (para.parameterName == "OpenSearch") {
                opensearch = para.parameterAsBoolean
            } else if (para.parameterName == "Full") {
                full = para.parameterAsBoolean
            }
        }
    }

    override fun getSqlResult(cnn: Connection): Serializable {
        val columns =
            if (full) { "*, C_ContactActivity_ID as activity_C_ContactActivity_ID" } else { "c_bpartner_id,name,taxid" }

        val sql =
            """
select $columns from adempiere.bpartner_v
where (value ilike ? or name ilike ? or referenceno ilike ? or duns ilike ? or taxid ilike ?) -- params 1..5
and iscustomer = 'Y' and ad_client_id IN (0, ?) and ( ad_org_id IN (0,?) or ? = 0) and isactive = 'Y' -- params 6..8
order by 1 desc
                """.trimIndent()

        val sqlSearch = "%$search%".toLowerCase()

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

        val modelFactory: IModelFactory = DefaultModelFactory()
        val result = mutableListOf<Any>()

        while (rs.next()) {
            if (full) {
                val bpartner: I_C_BPartner = modelFactory.getPO("C_BPartner", rs, "pokus") as I_C_BPartner
                val c_contactactivity_id = rs.getObject("c_contactactivity_id") as BigDecimal?
                val row = BPartnerWithActivity(bpartner,
                    if (c_contactactivity_id == null) { null } else {
                        modelFactory.getPO("C_ContactActivity", rs, "pokus", "activity_") as I_C_ContactActivity
                    },
                    rs.getString("category_name")
                )
                result.add(row)
            } else {
                val name = rs.getString("name")
                val foundIdx = name.toLowerCase().indexOf(search.toLowerCase())
                val subName = if (foundIdx > 0) { name.substring(foundIdx) } else { name }
                val keyName = BPartnerFindResult(rs.getInt("c_bpartner_id"), name, subName, rs.getString("taxid"))
                result.add(keyName)
            }
        }

        return if (full) {
            FindResult(result)
        } else {
            if (opensearch) {
                arrayOf<Any>(search, result.map { it as BPartnerFindResult }.map { it.name }.toTypedArray())
            } else {
                FindResult(result)
            }
        }
    }

}