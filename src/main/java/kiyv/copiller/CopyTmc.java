package kiyv.copiller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import kiyv.domain.dao.UtilDao;
import kiyv.domain.dao.TmcDao;
import kiyv.domain.dao.TmcDaoJdbc;
import kiyv.domain.dbf.TmcDbf;
import kiyv.domain.dbf.TmcDbfReader;
import kiyv.domain.model.Tmc;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static kiyv.log.ClassNameUtil.getCurrentClassName;

public class CopyTmc {

    private static final CopyTmc copyTmc = new CopyTmc();
    private static final TmcDao tmcDao = new TmcDaoJdbc(UtilDao.getConnPostgres());
    private static final TmcDbf tmcDbfReader = new TmcDbfReader();
    private static final Logger log = LoggerFactory.getLogger(getCurrentClassName());

    private CopyTmc() {
    }

    public static CopyTmc getInstance() {
        return copyTmc;
    }

    public static void main(String[] args) {

        CopyTmc.getInstance().doCopyNewRecord();

    }

    public void doCopyNewRecord() {
        long start = System.currentTimeMillis();
        log.info("Start writing 'T M C'.");

        List<Tmc> listNewTmc = new ArrayList<>();
        List<Tmc> listUpdatingTmc = new ArrayList<>();

        Map<String, Tmc> oldTmc = tmcDao.getAll()
                .stream()
                .collect(Collectors.toMap(Tmc::getId, Tmc::getTmc));

        List<Tmc> listTmcFrom1C = tmcDbfReader.getAll();

        for (Tmc tmc : listTmcFrom1C) {
            String idComparedTmc = tmc.getId();
            if (!oldTmc.containsKey(idComparedTmc)) {
                listNewTmc.add(tmc);
            } else if (!oldTmc.get(idComparedTmc).equals(tmc)) {
                listUpdatingTmc.add(tmc);
                oldTmc.remove(idComparedTmc);
            }
            else {
                oldTmc.remove(idComparedTmc);
            }
        }

        if (listNewTmc.size() > 0) {
            log.info("Save to DataBase. Must be added {} new TMC.", listNewTmc.size());
            tmcDao.saveAll(listNewTmc);
        }
        if (listUpdatingTmc.size() > 0) {
            log.info("Write change to DataBase. Must be updated {} TMC.", listUpdatingTmc.size());
            tmcDao.updateAll(listUpdatingTmc);
        }
        if (oldTmc.size() > 0) {
            log.info("Delete old TMC from DataBase. Must be deleted {} TMC.", oldTmc.size());
            tmcDao.deleteAll(oldTmc.keySet());
        }

        long end = System.currentTimeMillis();
        log.info("End writing 'T M C'. Time = {} c.", (double)(end-start)/1000);
    }
}

