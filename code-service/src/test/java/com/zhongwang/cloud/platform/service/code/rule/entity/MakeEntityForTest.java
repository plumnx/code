package com.zhongwang.cloud.platform.service.code.rule.entity;

import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.zhongwang.cloud.platform.bamboo.common.json.Json;
import com.zhongwang.cloud.platform.security.common.UserInfo;
import com.zhongwang.cloud.platform.service.code.common.CodeConst;
import com.zhongwang.cloud.platform.service.code.common.util.Dates;
import com.zhongwang.cloud.platform.service.code.rule.entity.vo.CodeMultiplyQuery;
import com.zhongwang.cloud.platform.service.code.rule.entity.vo.CodeQuery;
import com.zhongwang.cloud.platform.service.code.rule.entity.vo.CodeResult;
import com.zhongwang.cloud.platform.service.code.rule.lock.SegmentType;

import java.text.ParseException;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

import static java.util.UUID.randomUUID;

public class MakeEntityForTest {

    public static CodeQuery buildCodeQuery() {
        CodeQuery codeQuery = new CodeQuery();
        codeQuery.setPkid("8a80cb81608cf90001608cf90ceb0000");
        codeQuery.setCompPkid("ZhongWang");
        codeQuery.setSystemCode("MES");
        codeQuery.setModuleCode("code");
        codeQuery.setOperator("040011");

        Map<String, String> parameters = Maps.newHashMap();
        parameters.put("Field", "Field");
        parameters.put("SysVariable", "SysVariable");
        parameters.put("FieldDate", "2017-01-01T00:00:00.000Z");
        parameters.put("SysVariableDate", "2017-01-01T00:00:00.000Z");
        codeQuery.setParameters(parameters);

        return codeQuery;
    }

    public static Map<String, String> changeCodeQueryParameters() {
        Map<String, String> parameters = Maps.newHashMap();
        parameters.put("Field", "Fiela");
        parameters.put("SysVariable", "SysVariabla");
        parameters.put("FieldDate", "2017-02-01T00:00:00.000Z");
        parameters.put("SysVariableDate", "2017-02-01T00:00:00.000Z");
        return parameters;
    }

    public static CodeMultiplyQuery buildCodeMultiplyQuery() {
        CodeMultiplyQuery codeMultiplyQuery = new CodeMultiplyQuery();
        codeMultiplyQuery.setPkid("8a80cb81608cf90001608cf90ceb0000");
        codeMultiplyQuery.setSystemCode("MES");
        codeMultiplyQuery.setModuleCode("some-bussiness-code");
        codeMultiplyQuery.setOperator("040011");
        codeMultiplyQuery.setCompPkid("ZhongWang");
        codeMultiplyQuery.setSize(10);

        Map<String, String> parameters = Maps.newHashMap();
        parameters.put(CodeConst.SysVariableEnum.CURRENT_DATE.getCode(), "2018-1-1");
        parameters.put("Field", "Field");
        codeMultiplyQuery.setParameters(parameters);

        return codeMultiplyQuery;
    }

    public static CodeRule buildCodeRule() throws ParseException {
        CodeRule codeRule = new CodeRule();
        codeRule.setPkid("8a80cb816053f336016053f33d170000");
        codeRule.setRuleCode("test-code");
        codeRule.setRuleName("test-name");
        codeRule.setBelongBusiness("1");
        codeRule.setCodeType("1");
        codeRule.setBelongOrgPkid("010011");
        codeRule.setEffectiveDate(Dates.parseDate("2017-01-01T00:00:00Z", "yyyy-MM-dd'T'HH:mm:ss'Z'"));
        codeRule.setExpirationDate(Dates.parseDate("2099-12-31T00:00:00Z", "yyyy-MM-dd'T'HH:mm:ss'Z'"));
        codeRule.addCodeRuleDetail(buildConstRule());
        codeRule.addCodeRuleDetail(buildSysRule());
        codeRule.addCodeRuleDetail(buildFieldRule());
        codeRule.addCodeRuleDetail(buildSerialRule());
        codeRule.setFlagDelete((short) 0);
        codeRule.setSystemCode("MES");
        codeRule.setModuleCode("Module");
        codeRule.setCompPkid("CompoPkid");
        codeRule.setOperator("Operator");

        return codeRule;
    }

    public static CodeRuleDetail buildConstRule() {
        CodeRuleDetail codeRuleDetail = new CodeRuleDetail();
        codeRuleDetail.setSectionType(SegmentType.CONST_VARIABLE.getType().toString());
        codeRuleDetail.setSectionValue("Const");
        codeRuleDetail.setSectionLength(null);
        codeRuleDetail.setSupplyType("0");
        codeRuleDetail.setSupplyChar(null);
        codeRuleDetail.setDateFormat(null);
        codeRuleDetail.setFlagSerial(1);
        codeRuleDetail.setFlagShow(1);
        codeRuleDetail.setSectionSeparator("-");
        codeRuleDetail.setFlagSort(1L);
        codeRuleDetail.setFlagDelete((short) 0);

        return codeRuleDetail;
    }

    public static CodeRuleDetail buildSysRule() {
        CodeRuleDetail codeRuleDetail = new CodeRuleDetail();
        codeRuleDetail.setSectionType(SegmentType.SYS_VARIABLE.getType().toString());
        codeRuleDetail.setSectionValue("SysVariableDate");
        codeRuleDetail.setSectionLength(6);
        codeRuleDetail.setSupplyType("0");
        codeRuleDetail.setSupplyChar(null);
        codeRuleDetail.setDateFormat("yyyy-MM");
        codeRuleDetail.setFlagSerial(1);
        codeRuleDetail.setFlagShow(1);
        codeRuleDetail.setSectionSeparator("-");
        codeRuleDetail.setFlagSort(2L);
        codeRuleDetail.setFlagDelete((short) 1);

        return codeRuleDetail;
    }

    public static CodeRuleDetail buildFieldRule() {
        CodeRuleDetail codeRuleDetail = new CodeRuleDetail();
        codeRuleDetail.setSectionType(SegmentType.FIELD_VARIABLE.getType().toString());
        codeRuleDetail.setSectionValue("Field");
        codeRuleDetail.setSectionLength(4);
        codeRuleDetail.setSupplyType("1");
        codeRuleDetail.setSupplyChar(null);
        codeRuleDetail.setDateFormat(null);
        codeRuleDetail.setFlagSerial(1);
        codeRuleDetail.setFlagShow(1);
        codeRuleDetail.setSectionSeparator("-");
        codeRuleDetail.setFlagSort(3L);
        codeRuleDetail.setFlagDelete((short) 0);

        return codeRuleDetail;
    }

    public static CodeRuleDetail buildSerialRule() {
        CodeRuleDetail codeRuleDetail = new CodeRuleDetail();
        codeRuleDetail.setSectionType(SegmentType.SERIAL_VARIABLE.getType().toString());
        codeRuleDetail.setSectionValue("1");
        codeRuleDetail.setSectionLength(null);
        codeRuleDetail.setSupplyType("0");
        codeRuleDetail.setSupplyChar(null);
        codeRuleDetail.setDateFormat(null);
        codeRuleDetail.setFlagSerial(0);
        codeRuleDetail.setFlagShow(1);
        codeRuleDetail.setSectionSeparator(null);
        codeRuleDetail.setFlagSort(4L);
        codeRuleDetail.setFlagDelete((short) 0);

        return codeRuleDetail;
    }

    public static CodeResult buildCodeResult() {
        CodeResult codeResult = new CodeResult();
        codeResult.setCode("SCDD-201812-1");

        return codeResult;
    }

    public static UserInfo buildUserInfo() {
        UserInfo userInfo = new UserInfo();
        userInfo.setPkid(UUID.randomUUID().toString().replaceAll("-", ""));
        userInfo.setUsername("测试用户" + new Random().nextInt(100));
        return userInfo;
    }

    public static String buildPkid() {
        return UUID.randomUUID().toString().replaceAll("-", "");
    }

    public static void main(String[] args) throws ParseException {
        for(int i = 0; i < 10; i++) {
            System.out.println(buildPkid());
        }
    }

}
