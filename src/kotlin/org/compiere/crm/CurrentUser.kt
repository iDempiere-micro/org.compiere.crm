package org.compiere.crm

import org.compiere.process.SvrProcess
import java.io.Serializable

class CurrentUser : SvrProcessBase() {
    override fun getResult(): Serializable {
        return AD_USER_ID
    }
}