//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package ser;

import com.ser.blueline.*;
import com.ser.blueline.metaDataComponents.*;
import de.ser.doxis4.agentserver.UnifiedAgent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;

public class UpdateRights extends UnifiedAgent {
    Logger log = LogManager.getLogger(this.getClass().getName());
    ISession ses = null;
    IDocumentServer srv = null;
    String prjCode = "";
    String compShortName = "";

    public UpdateRights() {
    }

    protected Object execute() {
        this.log.info("Initiate the agent");
        if (this.getEventDocument() == null) {
            return this.resultError("Null Document object.");
        } else {
            ses = getSes();
            srv = ses.getDocumentServer();
            IDocument engDocument = this.getEventDocument();
            try {
                this.log.info("Update Right started for:" + engDocument.getID());
                prjCode = engDocument.getDescriptorValue("ccmPRJCard_code");

                String fromCode = engDocument.getDescriptorValue("ccmSenderCode");
                String receiverCode = engDocument.getDescriptorValue("ccmReceiverCode");

                boolean isMainCompFrom = isMainCompGVList("CCM_PARAM_CONTRACTOR-MEMBERS", fromCode);
                boolean isMainCompTo = isMainCompGVList("CCM_PARAM_CONTRACTOR-MEMBERS", receiverCode);

                if(!isMainCompFrom){compShortName = fromCode;}
                if(!isMainCompTo){compShortName = receiverCode;}
                log.info("Ubdate Rights...not main company :" + compShortName);

                String unitName = prjCode + "_" + compShortName;
                log.info("Ubdate Rights...unit name :" + unitName);
                IUnit unit = getDocumentServer().getUnitByName(getSes(), unitName);
                if(unit!=null){
                    engDocument.setDescriptorValue("AbacOrgaRead",unit.getID());
                    engDocument.commit();
                    log.info("Ubdate Rights..rights set for the unit:" + unit.getName());
                }else {
                    log.info("Ubdate Rights...unit is null :" + unitName);
                    return this.resultError("Ubdate Rights...unit null :" + unitName);
                }
            }catch (Exception e) {
                log.error("Exception Caught");
                log.error(e.getMessage());
                return resultError(e.getMessage());
            }
            this.log.info("Update Right finished for:" + engDocument.getID());
            return this.resultSuccess("Ended successfully");
        }
    }
    public boolean isMainCompGVList(String paramName, String compSName) {
        boolean rtrn = false;
        IStringMatrix settingsMatrix = getDocumentServer().getStringMatrix(paramName, getSes());
        String rowValuePrjCode = "";
        String rowValueParamDCC = "";
        String rowValueParamCompSName = "";
        String rowValueParamMainComp = "";
        for(int i = 0; i < settingsMatrix.getRowCount(); i++) {
            //rowValuePrjCode = settingsMatrix.getValue(i, 0);
            //rowValueParamDCC = settingsMatrix.getValue(i, 6);
            rowValueParamCompSName = settingsMatrix.getValue(i, 1);
            rowValueParamMainComp = settingsMatrix.getValue(i, 7);

            //if (!Objects.equals(rowValuePrjCode, prjCode)){continue;}
            //if (!Objects.equals(rowValueParamDCC, key1)){continue;}
            if (!Objects.equals(rowValueParamCompSName, compSName)){continue;}
            if (!Objects.equals(rowValueParamMainComp, "1")){continue;}

            return true;
        }
        return rtrn;
    }
    public IQueryDlg findQueryDlgForQueryClass(IQueryClass queryClass) {
        IQueryDlg dlg = null;
        if (queryClass != null) {
            dlg = queryClass.getQueryDlg("default");
        }

        return dlg;
    }
    public IQueryParameter query(ISession session, IQueryDlg queryDlg, Map<String, String> descriptorValues) {
        IDocumentServer documentServer = session.getDocumentServer();
        ISerClassFactory classFactory = documentServer.getClassFactory();
        IQueryParameter queryParameter = null;
        IQueryExpression expression = null;
        IComponent[] components = queryDlg.getComponents();

        for(int i = 0; i < components.length; ++i) {
            if (components[i].getType() == IMaskedEdit.TYPE) {
                IControl control = (IControl)components[i];
                String descriptorId = control.getDescriptorID();
                String value = (String)descriptorValues.get(descriptorId);
                if (value != null && value.trim().length() > 0) {
                    IDescriptor descriptor = documentServer.getDescriptor(descriptorId, session);
                    IQueryValueDescriptor queryValueDescriptor = classFactory.getQueryValueDescriptorInstance(descriptor);
                    queryValueDescriptor.addValue(value);
                    IQueryExpression expr = queryValueDescriptor.getExpression();
                    if (expression != null) {
                        expression = classFactory.getExpressionInstance(expression, expr, 0);
                    } else {
                        expression = expr;
                    }
                }
            }
        }

        if (expression != null) {
            queryParameter = classFactory.getQueryParameterInstance(session, queryDlg, expression);
        }

        return queryParameter;
    }
    public IDocumentHitList executeQuery(ISession session, IQueryParameter queryParameter) {
        IDocumentServer documentServer = session.getDocumentServer();
        return documentServer.query(queryParameter, session);
    }
}
