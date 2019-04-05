package com.heibaiying.dao;

import com.heibaiying.bean.USPopulation;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;

@RunWith(SpringRunner.class)
@ContextConfiguration({"classpath:springApplication.xml"})
public class PopulationDaoTest {

    @Autowired
    private PopulationDao populationDao;

    @Test
    public void queryAll() {
        List<USPopulation> USPopulationList = populationDao.queryAll();
        if (USPopulationList != null) {
            for (USPopulation USPopulation : USPopulationList) {
                System.out.println(USPopulation.getCity() + " " + USPopulation.getPopulation());
            }
        }
    }

    @Test
    public void save() {
        populationDao.save(new USPopulation("TX", "Dallas", 66666));
        USPopulation usPopulation = populationDao.queryByStateAndCity("TX", "Dallas");
        System.out.println(usPopulation);
    }

    @Test
    public void update() {
        populationDao.save(new USPopulation("TX", "Dallas", 99999));
        USPopulation usPopulation = populationDao.queryByStateAndCity("TX", "Dallas");
        System.out.println(usPopulation);
    }


    @Test
    public void delete() {
        populationDao.deleteByStateAndCity("TX", "Dallas");
        USPopulation usPopulation = populationDao.queryByStateAndCity("TX", "Dallas");
        System.out.println(usPopulation);
    }
}
