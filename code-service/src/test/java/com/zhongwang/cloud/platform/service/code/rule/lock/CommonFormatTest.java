package com.zhongwang.cloud.platform.service.code.rule.lock;

import com.zhongwang.cloud.platform.service.code.exception.CodeInvalidParametersException;
import com.zhongwang.cloud.platform.service.code.rule.entity.CodeRuleDetail;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class CommonFormatTest {

    @Test
    public void test_date_format_with_correct_format_should_be_right() throws CodeInvalidParametersException {
        CodeRuleDetail codeRuleDetail = new CodeRuleDetail();
        codeRuleDetail.setDateFormat("HH/mm/ss yyyy/MM/dd");

        Object formatDate = new DateFormat().format("2016-01-05T15:09:54.000Z", codeRuleDetail, null);
        assertNotNull(formatDate);
        assertEquals(formatDate, "15/09/54 2016/01/05");
    }

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Test
    public void test_date_format_with_wrong_format_should_be_failure() throws CodeInvalidParametersException {
        expectedException.expect(CodeInvalidParametersException.class);

        CodeRuleDetail codeRuleDetail = new CodeRuleDetail();
        codeRuleDetail.setDateFormat("HH/mm/ss yyyy/MM/dd");

        Object formatDate = new DateFormat().format("2016-01-05 15:09:54", codeRuleDetail, null);
        assertNotNull(formatDate);
        assertEquals(formatDate, "15/09/54 2016/01/05");
    }

}
