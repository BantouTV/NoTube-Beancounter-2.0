package io.beancounter.platform;

import org.testng.annotations.Test;

import java.util.ArrayList;

import static org.testng.Assert.assertEquals;

/**
 *
 * @author Enrico Candino ( enrico.candino @ gmail.com )
 */
public class ServiceTest {

    @Test(expectedExceptions = ServiceException.class)
    public void testCheckNotExistingMethod() throws ServiceException {
        Object[] params = new Object[]{};
        Service.check(UserService.class, "notExist", params);
    }

    @Test(expectedExceptions = ServiceException.class)
    public void testCheckMissingParameters() throws ServiceException {
        Object[] params = new Object[]{""};
        Service.check(UserService.class, "signUp", params);
    }

    @Test
    public void testCheckTypeParameters() throws ServiceException {
        Object[] params = new Object[]{4, "", "", "", ""};
        try {
            Service.check(UserService.class, "signUp", params);
        } catch (ServiceException e) {
            assertEquals(e.getMessage(), "Parameter [name] doesn't match the type. Found class java.lang.Integer instead of class java.lang.String");
        }
    }

    @Test
    public void testCheckOrderParameters() throws ServiceException {
        Object[] realParams = new Object[]{"name", "surname", "username", "password", "apikey"};
        Object[] params = new Object[]{"", "", "", "", ""};
        for(int i = 0; i<params.length; i++) {
            try {
                Service.check(UserService.class, "signUp", params);
            } catch (ServiceException e) {
                assertEquals(e.getMessage(), "Parameter [" + realParams[i] + "] cannot be empty string");
            }
            params[i] = realParams[i];
        }
        Service.check(UserService.class, "signUp", params);
    }

    @Test
    public void singlePathParamPassesCheck() throws Exception {
        Object[] params = new Object[] { "123456" };
        Service.check(FakeService.class, "getWithOnePathParam", params);
    }

    @Test(expectedExceptions = ServiceException.class)
    public void missingSinglePathParamFailsCheck() throws Exception {
        Object[] params = new Object[] {};
        Service.check(FakeService.class, "getWithOnePathParam", params);
    }

    @Test(expectedExceptions = ServiceException.class)
    public void nullSinglePathParamFailsCheck() throws Exception {
        Object[] params = new Object[] { null };
        Service.check(FakeService.class, "getWithOnePathParam", params);
    }

    @Test(expectedExceptions = ServiceException.class)
    public void invalidTypeForSinglePathParamFailsCheck() throws Exception {
        Object[] params = new Object[] { new ArrayList<Integer>() };
        Service.check(FakeService.class, "getWithOnePathParam", params);
    }

    @Test
    public void queryParamListPassesCheck() throws Exception {
        Object[] params = new Object[] { new ArrayList<String >() };
        Service.check(FakeService.class, "getWithQueryParamList", params);
    }
}
