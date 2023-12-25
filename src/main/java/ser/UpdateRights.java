//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package ser;

import ser.Constants.*;
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
    IUser owner = null;
    String prjCode = "";
    String mainCompSName = "";
    String compShortName = "";
    String ownerCompSName = "";

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
                owner = getDocumentServer().getUser(getSes() , engDocument.getOwnerID());
                prjCode = engDocument.getDescriptorValue("ccmPRJCard_code");
                mainCompSName = getMainCompGVList("CCM_PARAM_CONTRACTOR-MEMBERS");

                IDocument ownerContactFile = getContactFolder(owner.getEMailAddress());
                if(ownerContactFile != null){
                    ownerCompSName = ownerContactFile.getDescriptorValue("ContactShortName");
                }

                String fromCode = engDocument.getDescriptorValue("ccmSenderCode");
                String receiverCode = engDocument.getDescriptorValue("ccmReceiverCode");
                if(fromCode == null){
                    engDocument.setDescriptorValue("ccmSenderCode",ownerCompSName);
                }
                if(receiverCode == null && !Objects.equals(ownerCompSName, mainCompSName)){
                    engDocument.setDescriptorValue("ccmReceiverCode",mainCompSName);
                }
                engDocument.commit();

                fromCode = engDocument.getDescriptorValue("ccmSenderCode");
                receiverCode = engDocument.getDescriptorValue("ccmReceiverCode");

                if(!Objects.equals(mainCompSName, fromCode)){
                    compShortName = fromCode;
                }else {
                    compShortName = receiverCode;
                }
//                boolean isMainCompFrom = isMainCompGVList("CCM_PARAM_CONTRACTOR-MEMBERS", fromCode);
//                boolean isMainCompTo = isMainCompGVList("CCM_PARAM_CONTRACTOR-MEMBERS", receiverCode);
//                if(!isMainCompFrom){compShortName = fromCode;}
//                if(!isMainCompTo){compShortName = receiverCode;}
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
    public String getMainCompGVList(String paramName) {
        String rtrn = "";
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
            //if (!Objects.equals(rowValueParamCompSName, compSName)){continue;}
            if (!Objects.equals(rowValueParamMainComp, "1")){continue;}

            return rowValueParamCompSName;
        }
        return rtrn;
    }
    public IDocument getContactFolder(String eMail)  {
        StringBuilder builder = new StringBuilder();
        builder.append("TYPE = '").append(Constants.ClassIDs.SupplierContactWS).append("'")
                .append(" AND ")
                .append("PrimaryEMail").append(" = '").append(eMail).append("'");
        String whereClause = builder.toString();
        System.out.println("Where Clause: " + whereClause);

        IInformationObject[] informationObjects = createQuery(new String[]{Databases.BPWS} , whereClause , 1);
        if(informationObjects.length < 1) {return null;}
        return (IDocument) informationObjects[0];
    }
    public IInformationObject[] createQuery(String[] dbNames , String whereClause , int maxHits){
        String[] databaseNames = dbNames;

        ISerClassFactory fac = getSrv().getClassFactory();
        IQueryParameter que = fac.getQueryParameterInstance(
                getSes() ,
                databaseNames ,
                fac.getExpressionInstance(whereClause) ,
                null,null);
        if(maxHits > 0) {
            que.setMaxHits(maxHits);
            que.setHitLimit(maxHits + 1);
            que.setHitLimitThreshold(maxHits + 1);
        }
        IDocumentHitList hits = que.getSession() != null? que.getSession().getDocumentServer().query(que, que.getSession()):null;
        if(hits == null) return null;
        else return hits.getInformationObjects();
    }
}
