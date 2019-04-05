package com.heibaiying.dao;

import com.heibaiying.bean.USPopulation;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface PopulationDao {

    List<USPopulation> queryAll();

    void save(USPopulation USPopulation);

    USPopulation queryByStateAndCity(@Param("state") String state, @Param("city") String city);

    void deleteByStateAndCity(@Param("state") String state, @Param("city") String city);
}
