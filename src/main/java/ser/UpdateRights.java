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
    boolean isExternal = false;
    String unitName = "";
    public UpdateRights() {
    }

    protected Object execute() {
        this.log.info("Initiate the agent");
        if (this.getEventDocument() == null) {
            return this.resultError("Update Right...: Document object is null");
        } else {
            ses = getSes();
            srv = ses.getDocumentServer();
            IDocument engDocument = this.getEventDocument();
            try {
                this.log.info("Update Right started for:" + engDocument.getID());
                owner = getDocumentServer().getUser(getSes() , engDocument.getOwnerID());
                boolean isAdmin = isAdmin(owner);
                prjCode = engDocument.getDescriptorValue("ccmPRJCard_code");
                if(prjCode == null){
                    return this.resultError("Ubdate Rights...Project Code is null:" + prjCode);
                }
                mainCompSName = getMainCompGVList("CCM_PARAM_CONTRACTOR-MEMBERS");
                if(Objects.equals(mainCompSName, "")){
                    return this.resultError("Ubdate Rights...Main Comp Name is null:" + mainCompSName);
                }

                log.info("Update Rights..Main CompShortName:" + mainCompSName);
                String originator = engDocument.getDescriptorValue("ccmPrjDocOiginator");
                String sender = engDocument.getDescriptorValue("ccmSenderCode");
                String receiver = engDocument.getDescriptorValue("ccmReceiverCode");
                log.info("update rights...originator:" + originator);
                log.info("update rights...sender:" + sender + " /// and...recevier:" + receiver);

                if(receiver != null && sender != null){///sender ve receiver transmittal dan dolu geldiğinde bu alanlara bakılıyor.
                    if(!receiver.equals(mainCompSName)){
                        unitName = prjCode + "_" + receiver;
                        log.info("Ubdate Rights..Is External..unit name from receiver :" + unitName);
                    }else if(!sender.equals(mainCompSName)){
                        unitName = prjCode + "_" + sender;
                        log.info("Ubdate Rights..Is Internal..unit name from receiver :" + unitName);
                    }
                }else { ///değilse owner a bakılıyor.
                    IUnit extUnit = ses.getDocumentServer().getUnitByName(getSes(), "ExternalReader");
                    if (extUnit != null) {
                        List<String> units = Arrays.asList(owner.getUnitIDs());
                        if (units.contains(extUnit.getID())) {
                            isExternal = true;
                        }
                    }
                    log.info("Update Rights..Check...Is External :" + isExternal);
                    originator = originator.toUpperCase();
                    ownerCompSName = (originator != null ? originator : ownerCompSName);

                    if (Objects.equals(ownerCompSName, "")) {
                        IDocument ownerContactFile = getContactFolder(owner.getEMailAddress());
                        if (ownerContactFile != null) {
                            IDocument ownerContractorFile = getContractorFolder(ownerContactFile.getDescriptorValue("ObjectNumber"));
                            ownerCompSName = (ownerContractorFile != null ? ownerContractorFile.getDescriptorValue("ContactShortName") : "");
                        }
                        log.info("Update Rights..Is External...Owner CompShortName:" + ownerCompSName);
                    }

                    isExternal = (!Objects.equals(ownerCompSName, "") && !ownerCompSName.equals(mainCompSName) && !isAdmin || isExternal);
                    log.info("Update Rights..Is External2 :" + isExternal);
                    if (isExternal) {
                        compShortName = ownerCompSName;
                        engDocument.setDescriptorValue("ccmSenderCode", ownerCompSName);
                        engDocument.setDescriptorValue("ccmReceiverCode", mainCompSName);
                        unitName = prjCode + "_" + compShortName;
                    }else {
                        compShortName = mainCompSName;
                        engDocument.setDescriptorValue("ccmSenderCode", mainCompSName);
                        unitName = prjCode;
                    }
                }
                try {
                    log.info("Ubdate Rights....unit name :" + unitName);
                    IUnit unit = getDocumentServer().getUnitByName(getSes(), unitName);
                    if(unit!=null){
                        engDocument.setDescriptorValue("AbacOrgaRead",unit.getID());
                        log.info("Ubdate Rights..rights set for the unit:" + unit.getName());
                    }else {
                        log.info("Ubdate Rights...unit is null :" + unitName);
                        return this.resultError("Ubdate Rights..IsInternal..unit is null :" + unitName);
                    }
                    engDocument.commit();
                }catch (Exception e){
                    log.info("UpdateRights...commit error:" + e);
                    log.info("restarting agent....");
                    return resultRestart("Restarting Agent for UpdateRights");
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
    public boolean isAdmin(IUser user) throws Exception {
        try {
            boolean rtrn = false;
            IRole admRole = getSes().getDocumentServer().getRoleByName(getSes(),"admins");
            String[] roleIDs = (user != null ? user.getRoleIDs() : null);
            rtrn = Arrays.asList(roleIDs).contains(admRole.getID());
            return rtrn;
        }catch (Exception e){
            throw new Exception("Exeption Caught..isAdmin: " + e);
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
            rowValuePrjCode = settingsMatrix.getValue(i, 0);
            //rowValueParamDCC = settingsMatrix.getValue(i, 6);
            rowValueParamCompSName = settingsMatrix.getValue(i, 1);
            rowValueParamMainComp = settingsMatrix.getValue(i, 7);

            if (!Objects.equals(rowValuePrjCode, prjCode)){continue;}
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
            rowValuePrjCode = settingsMatrix.getValue(i, 0);
            rowValueParamCompSName = settingsMatrix.getValue(i, 1);
            rowValueParamMainComp = settingsMatrix.getValue(i, 7);

            if (!Objects.equals(rowValuePrjCode, prjCode)){continue;}
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
    public IDocument getContractorFolder(String compSCode)  {
        StringBuilder builder = new StringBuilder();
        builder.append("TYPE = '").append(ClassIDs.InvolveParty).append("'")
                .append(" AND ")
                .append("ccmPRJCard_code").append(" = '").append(prjCode).append("'")
                .append(" AND ")
                .append("ObjectNumber").append(" = '").append(compSCode).append("'");
        String whereClause = builder.toString();
        System.out.println("Where Clause: " + whereClause);

        IInformationObject[] informationObjects = createQuery(new String[]{"PRJ_FOLDER"} , whereClause , 1);
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
