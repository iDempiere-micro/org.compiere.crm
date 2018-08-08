package org.compiere.crm

import org.compiere.process.SvrProcess
import java.io.Serializable

class Import : SvrProcessBase() {
    override fun getResult(): Serializable {
        return "OK"
    }

    var i_bpartner_id: Int = 0

    override fun prepare() {
        super.prepare()
        for (para in parameter) {
            if (para.parameterName == "i_bpartner_id") {
                i_bpartner_id = para.parameterAsInt
            }
        }
    }

}