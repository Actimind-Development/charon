package org.wso2.charon3.core.utils.codeutils;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.wso2.charon3.core.exceptions.BadRequestException;
import org.wso2.charon3.core.schema.AttributeSchema;
import org.wso2.charon3.core.schema.SCIMAttributeSchema;
import org.wso2.charon3.core.schema.SCIMDefinitions;
import org.wso2.charon3.core.schema.SCIMResourceTypeSchema;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

import static org.junit.Assert.*;

@SuppressWarnings("checkstyle:javadoctype")
public class FilterTreeManagerTest {

    private SCIMResourceTypeSchema schema;

    @Before
    public void setup() {
        schema = SCIMResourceTypeSchema.createSCIMResourceSchema(
                Arrays.asList("mySchema"),
                SCIMAttributeSchema.createSCIMAttributeSchema(
                        "mySchema:test", "test", SCIMDefinitions.DataType.STRING,
                        false, "", true, false, SCIMDefinitions.Mutability.READ_ONLY,
                        SCIMDefinitions.Returned.ALWAYS, SCIMDefinitions.Uniqueness.NONE,
                        new ArrayList<String>(), new ArrayList<SCIMDefinitions.ReferenceType>()
                        , new ArrayList<SCIMAttributeSchema>()
                ),
                SCIMAttributeSchema.createSCIMAttributeSchema(
                        "mySchema:nr", "nr", SCIMDefinitions.DataType.INTEGER,
                        false, "", true, false, SCIMDefinitions.Mutability.READ_ONLY,
                        SCIMDefinitions.Returned.ALWAYS, SCIMDefinitions.Uniqueness.NONE,
                        new ArrayList<String>(), new ArrayList<SCIMDefinitions.ReferenceType>()
                        , new ArrayList<SCIMAttributeSchema>()
                )

        );
    }

    @Test
    public void testEqStringWithoutQuotes() throws IOException, BadRequestException {
        Node node = new FilterTreeManager("test eq a", schema).buildTree();
        assertExpression(node, "mySchema:test", "eq", "a");
    }

    @Test
    public void testEqStringWithQuotes() throws IOException, BadRequestException {
        Node node = new FilterTreeManager("test eq \"a\"", schema).buildTree();
        assertExpression(node, "mySchema:test", "eq", "a");
    }

    @Test
    public void testEqNrWithoutQuotes() throws IOException, BadRequestException {
        Node node = new FilterTreeManager("nr eq 5", schema).buildTree();
        assertExpression(node, "mySchema:nr", "eq", "5");
    }

    @Test
    public void testEqNrWithQuotes() throws IOException, BadRequestException {
        Node node = new FilterTreeManager("nr eq \"3\"", schema).buildTree();
        assertExpression(node, "mySchema:nr", "eq", "3");
    }

    private void assertExpression(Node node, String attrName, String op, String arg)
    {
        ExpressionNode enode = (ExpressionNode)node;
        assertEquals(enode.getAttributeValue() + " " + enode.getOperation() + " " + enode.getValue(), attrName + " " + op + " " + arg);
    }
}
