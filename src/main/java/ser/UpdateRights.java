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
                String fromCode = engDocument.getDescriptorValue("ccmSenderCode");
                String receiverCode = engDocument.getDescriptorValue("ccmReceiverCode");

                this.log.info("Update Last Version Finished");
                return this.resultSuccess("Ended successfully");
            } catch (Exception var15) {
                throw new RuntimeException(var15);
            }
        }
    }
    public boolean existDCCGVList(String paramName, String key1, String key2) {
        boolean rtrn = false;
        IStringMatrix settingsMatrix = getDocumentServer().getStringMatrix(paramName, getSes());
        String rowValuePrjCode = "";
        String rowValueParamUserID = "";
        String rowValueParamDCC = "";
        String rowValueParamMyComp = "";
        for(int i = 0; i < settingsMatrix.getRowCount(); i++) {
            rowValuePrjCode = settingsMatrix.getValue(i, 0);
            rowValueParamUserID = settingsMatrix.getValue(i, 5);
            rowValueParamDCC = settingsMatrix.getValue(i, 6);
            rowValueParamMyComp = settingsMatrix.getValue(i, 7);

            //if (!Objects.equals(rowValuePrjCode, prjCode)){continue;}
            if (!Objects.equals(rowValueParamDCC, key1)){continue;}
            if (!Objects.equals(rowValueParamUserID, key2)){continue;}
            if (!Objects.equals(rowValueParamMyComp, "1")){continue;}

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
