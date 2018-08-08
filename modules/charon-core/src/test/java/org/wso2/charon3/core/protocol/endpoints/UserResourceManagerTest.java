package org.wso2.charon3.core.protocol.endpoints;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.wso2.charon3.core.attributes.Attribute;
import org.wso2.charon3.core.attributes.ComplexAttribute;
import org.wso2.charon3.core.attributes.DefaultAttributeFactory;
import org.wso2.charon3.core.attributes.SimpleAttribute;
import org.wso2.charon3.core.exceptions.*;
import org.wso2.charon3.core.extensions.UserManager;
import org.wso2.charon3.core.objects.User;
import org.wso2.charon3.core.protocol.SCIMResponse;
import org.wso2.charon3.core.schema.SCIMAttributeSchema;
import org.wso2.charon3.core.schema.SCIMConstants;
import org.wso2.charon3.core.schema.SCIMDefinitions;
import org.wso2.charon3.core.schema.SCIMResourceTypeSchema;
import org.wso2.charon3.core.utils.AttributeUtil;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.wso2.charon3.core.utils.AttributeUtil.*;

public class UserResourceManagerTest {

    static final SCIMAttributeSchema ATTR_ID = SCIMAttributeSchema.createSCIMAttributeSchema(
            "uzar:id", "id", SCIMDefinitions.DataType.STRING, false, "", false, false, SCIMDefinitions.Mutability.READ_ONLY, SCIMDefinitions.Returned.ALWAYS,
            SCIMDefinitions.Uniqueness.SERVER, new ArrayList<String>(), new ArrayList<SCIMDefinitions.ReferenceType>(), new ArrayList<SCIMAttributeSchema>()
    );
    static final SCIMAttributeSchema ATTR_NAME = SCIMAttributeSchema.createSCIMAttributeSchema(
            "uzar:name", "name", SCIMDefinitions.DataType.STRING, false, "", true, false, SCIMDefinitions.Mutability.READ_WRITE, SCIMDefinitions.Returned.DEFAULT,
            SCIMDefinitions.Uniqueness.SERVER, new ArrayList<String>(), new ArrayList<SCIMDefinitions.ReferenceType>(), new ArrayList<SCIMAttributeSchema>()
    );
    static final SCIMAttributeSchema ATTR_ALIVE = SCIMAttributeSchema.createSCIMAttributeSchema(
            "uzar:alive", "alive", SCIMDefinitions.DataType.BOOLEAN, false, "", false, false, SCIMDefinitions.Mutability.READ_WRITE, SCIMDefinitions.Returned.REQUEST,
            SCIMDefinitions.Uniqueness.NONE, new ArrayList<String>(), new ArrayList<SCIMDefinitions.ReferenceType>(), new ArrayList<SCIMAttributeSchema>()
    );
    static final SCIMAttributeSchema ATTR_LUCKY_NUMBERS = SCIMAttributeSchema.createSCIMAttributeSchema(
            "uzar:lucky", "lucky", SCIMDefinitions.DataType.INTEGER, true, "", false, false, SCIMDefinitions.Mutability.READ_WRITE, SCIMDefinitions.Returned.DEFAULT,
            SCIMDefinitions.Uniqueness.NONE, new ArrayList<String>(), new ArrayList<SCIMDefinitions.ReferenceType>(), new ArrayList<SCIMAttributeSchema>()
    );

    static final SCIMAttributeSchema ATTR_DATE = SCIMAttributeSchema.createSCIMAttributeSchema(
            "uzar:death_dates:date", "date", SCIMDefinitions.DataType.DATE_TIME, false, "", false, false, SCIMDefinitions.Mutability.READ_WRITE,
            SCIMDefinitions.Returned.REQUEST, SCIMDefinitions.Uniqueness.NONE, new ArrayList<String>(),
            new ArrayList<SCIMDefinitions.ReferenceType>(), null
    );

    static final SCIMAttributeSchema ATTR_DEATH_DATES = SCIMAttributeSchema.createSCIMAttributeSchema(
            "uzar:death_dates", "death_dates", SCIMDefinitions.DataType.COMPLEX, true, "", false, false, SCIMDefinitions.Mutability.READ_WRITE, SCIMDefinitions.Returned.REQUEST,
            SCIMDefinitions.Uniqueness.NONE, new ArrayList<String>(), new ArrayList<SCIMDefinitions.ReferenceType>(),
            new ArrayList<SCIMAttributeSchema>(Arrays.asList(ATTR_DATE))
    );
    public static final DateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US);

    private SCIMResourceTypeSchema schema;
    private UserResourceManager manager;

    @Before
    public void setupSchema() {
        schema = SCIMResourceTypeSchema.createSCIMResourceSchema(
                Arrays.asList("uzar"),
                ATTR_ID,
                ATTR_NAME,
                ATTR_ALIVE,
                ATTR_LUCKY_NUMBERS,
                ATTR_DEATH_DATES
        );
        manager = new UserResourceManager() {
            @Override
            protected SCIMResponse encodeSCIMException(AbstractCharonException exception) {
                for (StackTraceElement el : exception.getStackTrace()) {
                    System.out.println(el);
                }
                return super.encodeSCIMException(exception);
            }
        };
        manager.setSchema(schema);
        manager.registerEndpoint(SCIMConstants.USER_ENDPOINT, "/scim/Users");
    }

    @Test
    public void testGet() throws BadRequestException, CharonException {
        final User toReturn = new User();
        toReturn.setSchema("uzar");
        toReturn.setAttribute(simple(ATTR_ID, "3"));
        toReturn.setAttribute(simple(ATTR_NAME, "a"));
        StubUserManager mgr = new StubUserManager() {
            @Override
            public User getUser(String id, Map<String, Boolean> requiredAttributes) throws CharonException, BadRequestException, NotFoundException {
                return toReturn;
            }
        };
        SCIMResponse response = manager.get("3", mgr, "", "");

        assertOk(response, "{'schemas':['uzar'],'id':'3'}");
        assertLocation(response, "/scim/Users/3");
    }

    @Test
    public void testGet_attributeName() throws BadRequestException, CharonException {
        final User toReturn = new User();
        toReturn.setSchema("uzar");
        toReturn.setAttribute(simple(ATTR_ID, "3"));
        toReturn.setAttribute(simple(ATTR_NAME, "a"));
        StubUserManager mgr = new StubUserManager() {
            @Override
            public User getUser(String id, Map<String, Boolean> requiredAttributes) throws CharonException, BadRequestException, NotFoundException {
                return toReturn;
            }
        };
        SCIMResponse response = manager.get("3", mgr, "name", "");
        assertOk(response, "{'schemas':['uzar'],'name':'a','id':'3'}");
    }

    @Test
    public void testCreate() throws BadRequestException, CharonException {
        final User afterCreate = new User();
        afterCreate.setSchema("uzar");
        afterCreate.setAttribute(simple(ATTR_ID, "4"));
        afterCreate.setAttribute(simple(ATTR_NAME, "b"));
        StubUserManager mgr = new StubUserManager() {
            @Override
            public User createUser(User user, Map<String, Boolean> requiredAttributes) throws CharonException, ConflictException, BadRequestException {
                assertEquals("b", simpleString(user, ATTR_NAME));
                assertNull(simpleString(user, ATTR_ID));
                return afterCreate;
            }
        };
        SCIMResponse response = manager.create("{'schemas':['uzar'],'name':'b'}", mgr, "name", "");
        assertOk(response, 201, "{'schemas':['uzar'],'name':'b','id':'4'}");
    }

    @Test
    public void testPatch_replace_differentTypes() throws Exception {
        final User original = new User();
        original.setSchema("uzar");
        original.setAttribute(simple(ATTR_ID, "1"));
        original.setAttribute(simple(ATTR_NAME, "bob"));
        original.setAttribute(simple(ATTR_ALIVE, true));
        original.setAttribute(ints(ATTR_LUCKY_NUMBERS, Arrays.asList(3, 7)));
        AtomicReference<User> patched = new AtomicReference<>();
        StubUserManager mgr = getAndUpdate(original, patched);

        SCIMResponse response = manager.updateWithPATCH("1", "{'schemas':['urn:ietf:params:scim:api:messages:2.0:PatchOp'],\n" +
                "'Operations': [\n" +
                "\t{'op': 'replace', 'path': 'alive', 'value': false},\n" +
                "\t{'op': 'replace', 'path': 'name', 'value': 'deadbob'},\n" +
                "\t{'op': 'replace', 'path': 'lucky', 'value': [3]}\n" +
                "]}", mgr, "", "");
        assertOk(response);
        assertNotNull(patched.get());
        assertEquals(
                Arrays.asList("1", "deadbob", false, Arrays.asList(3)),
                Arrays.asList(
                        simpleString(patched.get(), ATTR_ID),
                        simpleString(patched.get(), ATTR_NAME),
                        simpleBool(patched.get(), ATTR_ALIVE),
                        AttributeUtil.<Integer>multi(patched.get(), ATTR_LUCKY_NUMBERS)
                )
        );
    }

    @Test
    public void testPatch_add_remove_int() throws Exception {
        final User original = new User();
        original.setSchema("uzar");
        original.setAttribute(simple(ATTR_ID, "1"));
        original.setAttribute(simple(ATTR_NAME, "ab"));
        original.setAttribute(simple(ATTR_ALIVE, true));
        original.setAttribute(ints(ATTR_LUCKY_NUMBERS, Arrays.asList(3, 7)));
        AtomicReference<User> patched = new AtomicReference<>();
        StubUserManager mgr = getAndUpdate(original, patched);

        SCIMResponse response = manager.updateWithPATCH("1", "{'schemas':['urn:ietf:params:scim:api:messages:2.0:PatchOp'],\n" +
                "'Operations': [\n" +
                "\t{'op': 'add', 'path': 'lucky', 'value': 9},\n" +
                "\t{'op': 'remove', 'path': 'lucky[value eq 3]'}\n" +
                "]}", mgr, "", "");
        assertOk(response);
        assertNotNull(patched.get());
        assertEquals(
                Arrays.asList(7, 9),
                AttributeUtil.<Integer>multi(patched.get(), ATTR_LUCKY_NUMBERS)
        );
    }

    @Test
    public void testPatch_add_remove_complex() throws Exception {
        final User original = new User();
        original.setSchema("uzar");
        original.setAttribute(simple(ATTR_ID, "1"));
        original.setAttribute(simple(ATTR_NAME, "ab"));
        original.setAttribute(simple(ATTR_ALIVE, true));
        original.setAttribute(multi(ATTR_DEATH_DATES, Arrays.asList(
                complex(ATTR_DEATH_DATES, Arrays.<Attribute>asList(
                        simple(ATTR_DATE, DATE_FORMAT.parse("2018-01-01T00:00:00Z"))
                )),
                complex(ATTR_DEATH_DATES, Arrays.<Attribute>asList(
                        simple(ATTR_DATE, DATE_FORMAT.parse("2018-04-01T00:00:00Z"))
                        )
                )
        ), SCIMDefinitions.DataType.COMPLEX));
        AtomicReference<User> patched = new AtomicReference<>();
        StubUserManager mgr = getAndUpdate(original, patched);

        SCIMResponse response = manager.updateWithPATCH("1", "{'schemas':['urn:ietf:params:scim:api:messages:2.0:PatchOp'],\n" +
                "'Operations': [\n" +
                "\t{'op': 'add', 'path': 'death_dates', 'value': [{'date': '2018-05-15T00:00:00Z'}]},\n" +
                "\t{'op': 'remove', 'path': 'death_dates[date eq \"2018-04-01T00:00:00Z\"]'}\n" +
                "]}", mgr, "", "");
        assertOk(response);
        assertNotNull(patched.get());
        List<ComplexAttribute> dates = AttributeUtil.<ComplexAttribute>multi(patched.get(), ATTR_DEATH_DATES);
        List<String> datesAsStr = new ArrayList<>();
        for (ComplexAttribute c : dates) {
            datesAsStr.add(DATE_FORMAT.format(simpleDate(c, ATTR_DATE)));
        }
        assertEquals(
                Arrays.asList("2018-01-01T00:00:00Z", "2018-05-15T00:00:00Z"),
                datesAsStr
        );
    }

    @Test
    public void testAdd_withoutPath() throws Exception {
        //If omitted, the target location is assumed to be the resource
        //      itself.  The "value" parameter contains a set of attributes to be
        //      added to the resource.
        final User original = new User();
        original.setSchema("uzar");
        original.setAttribute(simple(ATTR_ID, "1"));
        original.setAttribute(simple(ATTR_NAME, "ab"));
        original.setAttribute(simple(ATTR_ALIVE, true));
        original.setAttribute(ints(ATTR_LUCKY_NUMBERS, Arrays.asList(3, 7)));
        original.setAttribute(multi(ATTR_DEATH_DATES, Arrays.asList(
                complex(ATTR_DEATH_DATES, Arrays.<Attribute>asList(
                        simple(ATTR_DATE, DATE_FORMAT.parse("2018-01-01T00:00:00Z"))
                ))), SCIMDefinitions.DataType.COMPLEX));
        AtomicReference<User> patched = new AtomicReference<>();
        StubUserManager mgr = getAndUpdate(original, patched);
        SCIMResponse response = manager.updateWithPATCH("1", "{'schemas':['urn:ietf:params:scim:api:messages:2.0:PatchOp'],\n" +
                "'Operations': [\n" +
                "\t{'op': 'add', 'value': [{'alive': false, 'lucky': [3,4], 'death_dates': [{'date': '2018-05-15T00:00:00Z'}]}]}\n" +
                "]}", mgr, "", "");
        assertOk(response);
        assertNotNull(patched.get());
        List<ComplexAttribute> dates = AttributeUtil.<ComplexAttribute>multi(patched.get(), ATTR_DEATH_DATES);
        List<String> datesAsStr = new ArrayList<>();
        for (ComplexAttribute c : dates) {
            datesAsStr.add(DATE_FORMAT.format(simpleDate(c, ATTR_DATE)));
        }

        assertEquals(
                Arrays.asList("1", "ab", false, Arrays.asList(3, 7, 4), Arrays.asList("2018-01-01T00:00:00Z", "2018-05-15T00:00:00Z")),
                Arrays.asList(
                        simpleString(patched.get(), ATTR_ID),
                        simpleString(patched.get(), ATTR_NAME),
                        simpleBool(patched.get(), ATTR_ALIVE),
                        AttributeUtil.<Integer>multi(patched.get(), ATTR_LUCKY_NUMBERS),
                        datesAsStr
                )
        );
    }

    //todo: test with user/group schemas

    private StubUserManager getAndUpdate(final User original, final AtomicReference<User> patched) {
        return new StubUserManager() {
            @Override
            public User getUser(String id, Map<String, Boolean> requiredAttributes) throws CharonException, BadRequestException, NotFoundException {
                return original;
            }

            @Override
            public User updateUser(User updatedUser, Map<String, Boolean> requiredAttributes) throws NotImplementedException, CharonException, BadRequestException, NotFoundException {
                patched.set(updatedUser);
                return updatedUser;
            }
        };
    }

    void assertOk(SCIMResponse response) {
        if (response.getResponseStatus() >= 400) {
            Assert.fail(response.getResponseMessage());
        }
    }

    void assertOk(SCIMResponse response, String expectedJson) {
        assertOk(response, 200, expectedJson);
    }

    void assertOk(SCIMResponse response, int code, String expectedJson) {
        assertEquals(
                code + "\n" + expectedJson.replaceAll("'", "\""),
                response.getResponseStatus() + "\n" + response.getResponseMessage()
        );
    }

    void assertLocation(SCIMResponse response, String location) {
        assertEquals(location, response.getHeaderParamMap().get("Location"));
    }

}
