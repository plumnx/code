package com.zhongwang.cloud.platform.service.code.rule.service;

import com.github.springtestdbunit.TransactionDbUnitTestExecutionListener;
import com.github.springtestdbunit.annotation.DatabaseSetup;
import com.zhongwang.cloud.platform.service.code.common.service.RedisService;
import com.zhongwang.cloud.platform.service.code.config.bean.CodeSerialPolicy;
import com.zhongwang.cloud.platform.service.code.exception.CodePersistentException;
import com.zhongwang.cloud.platform.service.code.rule.entity.CodeRuleSerial;
import com.zhongwang.cloud.platform.service.code.rule.repository.CodeRuleSerialRepository;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.transaction.TransactionalTestExecutionListener;

import static com.zhongwang.cloud.platform.service.code.common.CodeConst.RULE_TAG.EMPTY_SHOW_UNION_VALUE;
import static com.zhongwang.cloud.platform.service.code.common.CodeConst.RULE_TAG.EMPTY_SHOW_UNION_VALUE_FORMAT;
import static com.zhongwang.cloud.platform.service.code.rule.entity.MakeEntityForTest.buildCodeQuery;
import static org.junit.Assert.assertEquals;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.MOCK;

@SpringBootTest(webEnvironment = MOCK, properties = {
        "spring.cloud.config.enabled:false",
        "spring.config.name:unit"
})
@RunWith(SpringJUnit4ClassRunner.class)
@TestExecutionListeners({DependencyInjectionTestExecutionListener.class, TransactionalTestExecutionListener.class, TransactionDbUnitTestExecutionListener.class})
@EnableConfigurationProperties({CodeSerialPolicy.class})
public class GenerateDBSerialServiceWithDbUnitTest {

    @Autowired
    private RedisService redisService;

    @Autowired
    private CodeRuleSerialRepository codeRuleSerialRepository;

    @Autowired
    private GenerateDBSerialService generateDBSerialService;

    @Autowired
    private GenerateDBSerialUpdateService generateDBSerialUpdateService;

    @Test
    @DatabaseSetup({"/dbunit/CODE_UNIT_TEST_007.xml"})
    public void test_make_serial_no_to_db() throws CodePersistentException {
        int dbSerialNo = generateDBSerialUpdateService.makeSerialNoToDb(
                buildCodeQuery().withPkid(RULE_PKID),
                SERIAL_UNION_VALUE_FORMAT, SERIAL_UNION_VALUE, EMPTY_SHOW_UNION_VALUE_FORMAT, EMPTY_SHOW_UNION_VALUE,
                1, 1);
        assertEquals(1, dbSerialNo);

        CodeRuleSerial codeRuleSerial = codeRuleSerialRepository.findOne(SERIAL_PKID);
        assertEquals(2, codeRuleSerial.getCodeMaxValue().intValue());
    }

    private static final String RULE_PKID = "c3a1104097a9450aaf957c5865b7282g";
    private static final String SERIAL_PKID = "8a80cb816270b79b016270b7ba6d000g";
    private static final String SERIAL_UNION_VALUE = "Const-Fiel-<SERIAL_CODE>";
    private static final String SERIAL_UNION_VALUE_FORMAT = "CONST_VARIABLE,FIELD_VARIABLE,SERIAL_VARIABLE";

    @After
    public void clearRedisCache() {
        redisService.flushDb();
    }

}
