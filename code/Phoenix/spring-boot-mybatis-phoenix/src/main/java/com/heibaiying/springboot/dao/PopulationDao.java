package com.heibaiying.springboot.dao;

import com.heibaiying.springboot.bean.USPopulation;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface PopulationDao {

    @Select("SELECT * from us_population")
    List<USPopulation> queryAll();

    @Insert("UPSERT INTO us_population VALUES( #{state}, #{city}, #{population} )")
    void save(USPopulation USPopulation);

    @Select("SELECT * FROM us_population WHERE state=#{state} AND city = #{city}")
    USPopulation queryByStateAndCity(String state, String city);


    @Delete("DELETE FROM us_population WHERE state=#{state} AND city = #{city}")
    void deleteByStateAndCity(String state, String city);

}
