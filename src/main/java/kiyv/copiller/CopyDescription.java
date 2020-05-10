package kiyv.copiller;

import kiyv.domain.dao.*;
import kiyv.domain.dbf.*;
import kiyv.domain.model.Description;
import kiyv.domain.model.Status;
import kiyv.domain.model.Tmc;
import kiyv.domain.tools.DateConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static kiyv.log.ClassNameUtil.getCurrentClassName;

public class CopyDescription {

    private static final CopyDescription copyDescription = new CopyDescription();
    private static final OrderDao orderDao = new OrderDaoJdbc(UtilDao.getConnPostgres());
    private static final TmcDao tmcDao = new TmcDaoJdbc(UtilDao.getConnPostgres());
    private static final TmcDao tmcDaoTechno = new TmcDaoTechnoJdbc(UtilDao.getConnPostgres());
    private static final DescriptionDao descriptionDao = new DescriptionDaoJdbc(UtilDao.getConnPostgres());
    private static final StatusDao statusDao = new StatusDaoJdbc(UtilDao.getConnPostgres());
    private static final DescriptionDbf descriptionDbfReader = new DescriptionDbfReader();
    private static final EmbodimentDbf embodimentDbfReader = new EmbodimentDbfReader();
    private static final Logger log = LoggerFactory.getLogger(getCurrentClassName());

    private CopyDescription() {
    }

    public static CopyDescription getInstance() {
        return copyDescription;
    }

    public static void main(String[] args) {

        CopyDescription.getInstance().doCopyNewRecord();

    }

    public void doCopyNewRecord() {
        long start = System.currentTimeMillis();
        log.info("Start writing 'D E S C R I P T I O N'.");

        Map<String, String> mapEmbodiment = embodimentDbfReader.getAll();
//        List<String> idDocList = orderDao.getAllId();
        Map<String, Timestamp> mapDateToFactory = orderDao.getAllDateToFactory();
        Map<String, Tmc> mapTmc = tmcDao.getAll()
                .stream()
                .collect(Collectors.toMap(Tmc::getId, Tmc::getTmc));

        List<String> listIdTmcTechno = new ArrayList<>();
        for (Tmc tmc : tmcDaoTechno.getAll()){
            listIdTmcTechno.add(tmc.getId());
        }

        List<Description> listDescription = descriptionDbfReader.getAll();

        List<Description> listNewDescription = new ArrayList<>();
        List<Description> listUpdatingDescription = new ArrayList<>();
        List<Status> listNewStatuses = new ArrayList<>();

        Map<String, Description> mapOldDescription = descriptionDao.getAll()
                .stream()
                .collect(Collectors.toMap(Description::getId, Description::getDescription));

        for (Description newDescription : listDescription) {
            String newDescriptionCode = newDescription.getId();
            String idDoc = newDescriptionCode.split("-")[0];

            if (mapDateToFactory.containsKey(idDoc)) {
                //change {description.embodiment}: from 'code' to its 'description'
                String codeEmbodiment = newDescription.getEmbodiment();
                if ( mapEmbodiment.get(codeEmbodiment) != null ) {
                    newDescription.setEmbodiment(mapEmbodiment.get(codeEmbodiment));
                }
                else {
                    newDescription.setEmbodiment("-");
                }

                long[] statusTimeList = new long[25];
                for (int i = 0; i < 25; i++) {
                    statusTimeList[i] = 0L;
                }
                statusTimeList[0] = mapDateToFactory.get(newDescription.getIdDoc()).getTime();
                statusTimeList[1] = DateConverter.getNowDate();

//                String embodiment = newDescription.getEmbodiment();

                String descrFirst = mapTmc.get(newDescription.getIdTmc()).getDescr();

                if ( ! codeEmbodiment.trim().equals("-")) {
                    descrFirst += newDescription.getEmbodiment() + " ";
                }
                int typeIndex = 0;
                int statusIndex = 0;
                int isTechnologichka = 0;
                if (listIdTmcTechno.contains(newDescription.getIdTmc()) ){
                    isTechnologichka = 1;
                    typeIndex = 3;
//                        statusIndex = 7;
                }

                Status status = new Status(newDescriptionCode, idDoc, typeIndex, statusIndex, null, descrFirst,
                        isTechnologichka, 0, statusTimeList);
                newDescription.setStatus(status);

                if (!mapOldDescription.containsKey(newDescriptionCode)) {
                    listNewDescription.add(newDescription);
                    listNewStatuses.add(newDescription.getStatus());
                } else if (!mapOldDescription.get(newDescriptionCode).equals(newDescription)) {
                    log.info("UPDATE Description with code '{}'. Different fields: {}.",
                            newDescriptionCode,
                            newDescription.getDifferences(mapOldDescription.get(newDescriptionCode))
                    );
                    listUpdatingDescription.add(newDescription);
                    mapOldDescription.remove(newDescriptionCode);
                }
                else {
                    mapOldDescription.remove(newDescriptionCode);
                }
            }
        }

        if (listNewDescription.size() > 0) {
            log.info("Save to DataBase. Must be added {} new Descriptions.", listNewDescription.size());
            descriptionDao.saveAll(listNewDescription);
            statusDao.saveBeginValues(listNewStatuses);
        }
        if (listUpdatingDescription.size() > 0) {
            log.info("Write change to DataBase. Must be updated {} Descriptions.", listUpdatingDescription.size());
            descriptionDao.updateAll(listUpdatingDescription);
        }
        if (mapOldDescription.size() > 0) {
            log.info("Delete old Description from DataBase. Must be deleted {} Description.", mapOldDescription.size());
            for (Description description : mapOldDescription.values()) {
                log.info("DELETE Description with Code '{}'.", description.getId());
            }
            descriptionDao.deleteAll(mapOldDescription.keySet());
        }

        long end = System.currentTimeMillis();
        log.info("End writing 'D E S C R I P T I O N'. Time = {} c.", (double)(end-start)/1000);
    }
}
