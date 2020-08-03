package kiyv.copiller;

import java.io.*;
import com.linuxense.javadbf.*;

import kiyv.domain.dao.TmcDao;
import kiyv.domain.dao.TmcDaoJdbc;
import kiyv.domain.dao.UtilDao;
import kiyv.domain.dbf.TmcDbf;
import kiyv.domain.dbf.TmcDbfReader;
import kiyv.domain.model.Tmc;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static kiyv.log.ClassNameUtil.getCurrentClassName;

public class CopyTmc2 {

    private static final Logger log = LoggerFactory.getLogger(getCurrentClassName());

    public static void main(String[] args) {
//        new CopyTmc2().doCopyNewRecord();

        DBFReader reader = null;
        try {

            reader = new DBFReader(new FileInputStream("D:\\KiyV management2\\DB_copy\\SC302.DBF"));

            int numberOfFields = reader.getFieldCount();

/*
            for (int i = 0; i < numberOfFields; i++) {
                DBFField field = reader.getField(i);

                System.out.print(field.getName() + ", ");
                System.out.print(field.getType()+ ", ");
                System.out.println(field.getLength());
            }
*/
            List<Tmc> listTmc = new ArrayList<>();
            DBFRow row;

            while ((row = reader.nextRow()) != null) {
                String id = row.getString("ID");
                String parentId = row.getString("PARENTID");
                String code = row.getString("CODE");
                String descr = new String(row.getString("DESCR").getBytes("ISO-8859-1"), "Windows-1251");
                int isFolder = row.getInt("ISFOLDER");
                String descrAll = new String(row.getString("SP276").getBytes("ISO-8859-1"), "Windows-1251");
                String type = row.getString("SP277");

                listTmc.add(new Tmc(id, parentId, code, descr, isFolder, descrAll, type));


//                System.out.println(id + ", " + parentId + ", " + code + ", " + descr + ", " + isFolder + ", " + descrAll + ", " + type);
            }

        } catch (DBFException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        finally {
            DBFUtils.close(reader);
        }
    }



    public void doCopyNewRecord() {
        long start = System.currentTimeMillis();
        log.info("Start writing 'T M C'.");

        UtilDao utilDao = new UtilDao();
        Connection connPostgres = utilDao.getConnPostgres();
        Connection connDbf = utilDao.getConnDbf();

        TmcDao tmcDao = new TmcDaoJdbc(connPostgres);
        TmcDbf tmcDbfReader = new TmcDbfReader(connDbf);

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

        utilDao.closeConnection(connPostgres);
        utilDao.closeConnection(connDbf);
    }
}

