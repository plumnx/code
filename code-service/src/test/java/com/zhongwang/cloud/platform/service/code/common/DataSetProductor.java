package com.zhongwang.cloud.platform.service.code.common;

import org.apache.tomcat.jdbc.pool.DataSource;
import org.assertj.core.util.Lists;
import org.dbunit.DatabaseUnitException;
import org.dbunit.database.DatabaseConnection;
import org.dbunit.database.QueryDataSet;
import org.dbunit.dataset.csv.CsvDataSetWriter;
import org.dbunit.dataset.xml.FlatXmlDataSet;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

//@RunWith(SpringJUnit4ClassRunner.class)
//@SpringBootTest(webEnvironment = MOCK, properties = {
//        "spring.cloud.config.enabled:false",
//        "spring.config.name:deploy"
//})
public class DataSetProductor {

    @Autowired
    DataSource dataSource;

//    @Test
    public void productXmlDataSet() throws SQLException, DatabaseUnitException, IOException {
        QueryDataSet dataSet = new QueryDataSet(new DatabaseConnection(dataSource.getConnection()));
        for(String tableName: tableNames) {
            dataSet.addTable(tableName, "select * from " + tableName);
            FlatXmlDataSet.write(dataSet, new FileOutputStream(tableName + ".xml"));
        }
    }

    public void productCsvDataSet() throws SQLException, DatabaseUnitException, IOException {
        QueryDataSet dataSet = new QueryDataSet(new DatabaseConnection(dataSource.getConnection()));
        for(String tableName: tableNames) {
            dataSet.addTable(tableName, "select * from " + tableName);
            CsvDataSetWriter.write(dataSet, new File("/"));
        }
    }

    private List<String> tableNames = Lists.newArrayList(
            "PLT_BASE_CODE_RULE",
            "PLT_BASE_CODE_RULE_DETAIL",
            "PLT_BASE_CODE_RULE_SERIAL"
    );

}
