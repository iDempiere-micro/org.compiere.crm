package org.compiere.crm

import org.idempiere.common.util.DB
import java.sql.Connection

abstract class SvrProcessBaseSql : SvrProcessBase() {
    protected abstract fun getSqlResult(cnn: Connection): java.io.Serializable
    protected abstract val isRO : Boolean

    override fun getResult(): java.io.Serializable {
        val cnn = if(isRO) {
            DB.getConnectionRO()} else {DB.getConnectionRW()}
        try {
            val result = getSqlResult(cnn)
            return result
        } finally {
            cnn.close()
        }
    }
}