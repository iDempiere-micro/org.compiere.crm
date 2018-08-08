package org.compiere.crm

import org.compiere.process.SvrProcess
import org.idempiere.common.util.Env

abstract class SvrProcessBase : SvrProcess() {
    var AD_CLIENT_ID = 0 // AD_Client_ID
    var AD_ORG_ID = 0 // AD_Org_ID
    var AD_USER_ID = 0

    protected abstract fun getResult(): java.io.Serializable

    override fun prepare() {
        for (para in parameter) {
            if (para.parameterName == "AD_User_ID") {
                AD_USER_ID = para.parameterAsInt
            } else if (para.parameterName == "AD_Client_ID") {
                AD_CLIENT_ID = para.parameterAsInt
            } else if (para.parameterName == "AD_Org_ID") {
                AD_ORG_ID = para.parameterAsInt
            }
        }
    }

    override fun doIt(): String {
        val pi = processInfo
        Env.getCtx()

        pi.serializableObject = getResult()
        return "OK"
    }
}